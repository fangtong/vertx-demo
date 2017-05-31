package org.ft.test.vertx.jdbc;

import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.ft.test.vertx.utils.AsyncResultUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * Created by fangtong on 2017/5/27.
 */
public class JdbcVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new JdbcVerticle());
    }

    volatile int cnt = 0;
    volatile int error = 0;

    BlockingQueue<Integer> mobileNumbers = new LinkedBlockingQueue<>();

    JDBCClient shared;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        shared = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", "jdbc:mysql://172.31.16.9:33061/?useUnicode=true&characterEncoding=UTF-8")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user", "root")
                .put("password", "Xingluo2015~@$")
                .put("max_pool_size", 30)
                .put("row_stream_fetch_size", 10000));

//        Future.<SQLConnection>future(f -> {
//            shared.getConnection(f.completer());
//        }).compose(connection -> {
//            Future<SQLConnectionHold<SQLRowStream>> f = Future.future();
//            connection.queryStream("select mobileNumber from db_sdk_location.t_phone_province where mobileCity is null", conv(f, connection));
//            return f;
//        }).compose(result -> {
//            SQLRowStream stream = result.body;
//            stream.handler(row -> {
//                Integer head = row.getInteger(0);
//                mobileNumbers.add(head);
//            });
//
//            return Future.succeededFuture(result.connect);
//        })
//                .setHandler(result -> {
//                    if (result.succeeded()) {
//                        result.result().close();
//                    } else {
//                        System.out.println(result.cause().getMessage());
//                    }
//                });
        query("select mobileNumber from db_sdk_location.t_phone_province where mobileCity is null",
                result -> {
                    if (result.succeeded()) {
                        List<JsonArray> rows = result.result().getResults();
                        rows.forEach(row -> mobileNumbers.add(row.getInteger(0)));
                    }
                });

        vertx.setPeriodic(1000, tt -> {
            Integer head = mobileNumbers.poll();
            if (head != null) {
                vertx.executeBlocking(handler -> {
                    vertx.createHttpClient().getNow(10002, "172.31.16.13", "/phone?phone=" + head + "0000", r -> {
                        r.bodyHandler(buffer -> handler.complete(buffer.toString()));
                    });
                }, false, rps -> {
                    Map<String, String> result = Json.decodeValue((String) rps.result(), Map.class);
                    if (result.get("city") == null
                            || result.get("city").equals("null")) {
                        ++error;
                    }
                    System.out.println(head + "/" + rps.result() + (++cnt) + "," + error);
                });
            }
        });

        super.start(startFuture);
    }


    /**
     * 事务操作
     * @param exec 事务中执行
     * @param asyncResultHandler
     */
    public void transactions(Function<SQLConnection, Future<Void>> exec, Handler<AsyncResult<Boolean>> asyncResultHandler) {
        //要保证连接内 不管是否异常 都要关闭连接
        shared.getConnection(connResult -> {
            if (connResult.succeeded()) {
                SQLConnection connection = connResult.result();
                //开启事务
                Future.<Void>future(handle -> {
                    connection.setAutoCommit(false, handle.completer());
                }).compose(hold -> { //执行事务操作
                    return exec.apply(connection);
                }).compose(hold -> { //提交及回滚
                    Future<Boolean> f = Future.future();
                    connection.commit(result -> {
                        if (result.failed()) {
                            connection.rollback(rollbackResult -> {
                                if (rollbackResult.succeeded()) {
                                    f.complete(false);
                                } else {
                                    f.fail(rollbackResult.cause());
                                }
                            });
                        } else {
                            f.complete(true);
                        }
                    });
                    return f;
                }).setHandler(result -> {
                    //关闭连接 回调提交结果
                    connection.close();
                    asyncResultHandler.handle(result);
                });
            } else {
                asyncResultHandler.handle(AsyncResultUtil.transform(connResult, old -> null));
            }
        });

    }


    /**
     * 查询
     *
     * @param sql
     * @param asyncResultHandler
     */
    public void query(String sql, Handler<AsyncResult<ResultSet>> asyncResultHandler) {
        shared.getConnection(connresult -> {
            if (connresult.succeeded()) {
                //只要连接成功 必须关闭connection
                SQLConnection connection = connresult.result();
                Future queryFuture = Future.<ResultSet>future(queryAsyncResult -> {
                    connection.close();
                    asyncResultHandler.handle(queryAsyncResult);
                });
                connection.query(sql, queryFuture.completer());
            } else {
                asyncResultHandler.handle(AsyncResultUtil.transform(connresult, old -> null));
            }
        });
    }

    <T> AsyncResult<T> conv(AsyncResult<SQLConnectionHold<T>> result) {
        return AsyncResultUtil.transform(result, hold -> hold.body);
    }


    <T> Handler<AsyncResult<T>> conv(Future<SQLConnectionHold<T>> future, SQLConnection connection) {
        return result -> {
            if (result.succeeded()) {
                future.complete(new SQLConnectionHold<>(connection, result.result()));
            } else {
                future.fail(result.cause());
            }
        };
    }


    class SQLConnectionHold<T> {
        SQLConnection connect;
        T body;

        public SQLConnectionHold(SQLConnection connect, T result) {
            this.connect = connect;
            this.body = result;
        }


    }
}
