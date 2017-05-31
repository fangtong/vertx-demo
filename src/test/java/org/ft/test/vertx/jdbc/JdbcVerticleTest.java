package org.ft.test.vertx.jdbc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.ft.test.vertx.netty.NettyServerVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * JDBC 测试类
 * Created by fangtong on 2017/5/31.
 */
@RunWith(VertxUnitRunner.class)
public class JdbcVerticleTest {


    private Vertx vertx;
    private JdbcVerticle jdbc;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        // Register the context exception handler
        vertx.exceptionHandler(context.exceptionHandler());
        jdbc = new JdbcVerticle();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(new JsonObject()
                .put("url", "jdbc:mysql://172.31.16.9:33061/?useUnicode=true&characterEncoding=UTF-8")
                .put("driver_class", "com.mysql.jdbc.Driver")
                .put("user", "root")
                .put("password", "Xingluo2015~@$")
                .put("max_pool_size", 30)
                .put("row_stream_fetch_size", 10000));

        Async async = context.async();
        vertx.deployVerticle(jdbc, deploymentOptions, r -> async.complete());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void query(TestContext context) {
        Async async = context.async();
        jdbc.query("select mobileNumber from db_sdk_location.t_phone_province where mobileCity is null limit 1",
                result -> {
                    if (result.succeeded()) {
                        List<JsonArray> rows = result.result().getResults();
                        System.out.println(rows.toString());
                    } else {
                        System.out.println("error:" + result.cause());
                    }
                    async.complete();
                });
    }


    @Test
    public void tran(TestContext context){
        Function<SQLConnection, Future<?>> func = connection -> Future.<ResultSet>future(f->{
                connection.query("select * from test.test",f.completer());
        }).compose(result->{
            Future<Void> f = Future.future();
            connection.execute("insert into test.test(id,msg)value(3,'test')",f.completer());
            return f;
        }).compose(result->{
            Future<Void> f = Future.future();
            connection.execute("insert into test.test(id,msg)value(2,'test')",f.completer());
            return f;
        });


        Async async = context.async();
        jdbc.transactions(
                func,
                result->{
                    System.out.println("tran:"+result.succeeded());
                    async.complete();
                }
        );
    }
}
