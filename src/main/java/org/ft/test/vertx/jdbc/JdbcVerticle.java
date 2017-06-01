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
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created by fangtong on 2017/5/27.
 */
public class JdbcVerticle extends AbstractVerticle {

    private JDBCClient shared;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        shared = JDBCClient.createShared(vertx, config());
        super.start(startFuture);
    }


    /**
     * 事务操作
     *
     * @param exec               事务中执行
     * @param asyncResultHandler 事务处理结果
     */
    public void transactions(Function<SQLConnection, Future<?>> exec, Handler<AsyncResult<Boolean>> asyncResultHandler) {
        //要保证连接内 不管是否异常 都要关闭连接
        shared.getConnection(connResult -> {
            if (connResult.succeeded()) {
                SQLConnection connection = connResult.result();
                //开启事务
                Future.<Void>future(
                        handle -> connection.setAutoCommit(false, handle.completer())
                ).compose(hold -> { //执行事务操作
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
     * @param sql                执行语句
     * @param asyncResultHandler 异步结果
     */
    public void query(String sql, Handler<AsyncResult<ResultSet>> asyncResultHandler) {
        String uuid = UUID.randomUUID().toString();
        shared.getConnection(connresult -> {
            if (connresult.succeeded()) {
                //只要连接成功 必须关闭connection
                SQLConnection connection = connresult.result();
                Future.<ResultSet>future(
                        queryAsyncResult -> connection.query(sql, queryAsyncResult.completer())
                ).setHandler(resultSet -> {
                    connection.close();
                    asyncResultHandler.handle(resultSet);
                });

                System.out.println(uuid + " connect suc!");
            } else {
                asyncResultHandler.handle(AsyncResultUtil.transform(connresult, old -> null));
                System.out.println(uuid + " connect fail!");
            }
        });
    }

}
