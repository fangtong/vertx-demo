package org.ft.test.vertx.jdbc;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by fangtong on 2017/5/27.
 */
public class JdbcVerticle extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new JdbcVerticle());
    }

    volatile int cnt = 0;

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

        Future.<SQLConnection>future(f -> {
            shared.getConnection(f.completer());
        }).compose(connection -> {
            Future<SQLConnectionHold<SQLRowStream>> f = Future.future();
            connection.queryStream("select mobileNumber from db_sdk_location.t_phone_province where mobileCity is null", conv(f, connection));
            return f;
        }).compose(result -> {
            SQLRowStream stream = result.body;
            stream.handler(row -> {
                Integer head = row.getInteger(0);
                mobileNumbers.add(head);
            });

            return Future.succeededFuture(result.connect);
        })
                .setHandler(result -> {
                    if (result.succeeded()) {
                        result.result().close();
                    } else {
                        System.out.println(result.cause().getMessage());
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
                    System.out.println(head + "/" + rps.result() + (++cnt));
                });
            }
        });

        super.start(startFuture);
    }

    public void query(String sql,Handler<AsyncResult<ResultSet>> asyncResultHandler){
        Future.<SQLConnection>future(f -> {
            shared.getConnection(f.completer());
        }).compose(connection -> {
            Future<SQLConnectionHold<ResultSet>> f = Future.future();
            connection.query(sql, conv(f, connection));
            return f;
        }).setHandler(result -> {
            if (result.succeeded()) {
                result.result().connect.close();
            }
            asyncResultHandler.handle(conv(result));
        });
    }

    <T> AsyncResult<T> conv(AsyncResult<SQLConnectionHold<T>> result){
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return result.result().body;
            }

            @Override
            public Throwable cause() {
                return result.cause();
            }

            @Override
            public boolean succeeded() {
                return result.succeeded();
            }

            @Override
            public boolean failed() {
                return result.failed();
            }
        };
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
