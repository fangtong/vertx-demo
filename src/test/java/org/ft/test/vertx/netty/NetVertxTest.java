package org.ft.test.vertx.netty;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * Created by fangtong on 2017/5/19.
 */
@RunWith(VertxUnitRunner.class)
public class NetVertxTest {

    Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        // Register the context exception handler
        vertx.exceptionHandler(context.exceptionHandler());
        Async async = context.async();
        vertx.deployVerticle(new NettyServerVerticle(),new DeploymentOptions(),result->{
            async.complete();
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void startNettyServer(TestContext context){
        Async async = context.async();
        vertx.deployVerticle(new NettyServerVerticle(),new DeploymentOptions(),result->{
            async.complete();
        });
    }

    @Test
    public void startNettyClient(TestContext context){
        Async async = context.async();
        NettyClientVerticle v = new NettyClientVerticle()
                .connectSuccessHandle(actor->{
                    actor.sendMessage(Buffer.buffer("test"));
                    async.complete();
                });
        vertx.deployVerticle(v);

    }
}
