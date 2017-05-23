package org.ft.test.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fangtong on 2017/5/19.
 */
@RunWith(VertxUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetricsTest {

    Vertx vertx;

    @Before
    public void before(TestContext context) {
        VertxOptions DROPWIZARD_OPTIONS = new VertxOptions().
                setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
        vertx = Vertx.vertx(DROPWIZARD_OPTIONS);

        // Register the context exception handler
        vertx.exceptionHandler(context.exceptionHandler());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public  void test1MetricsDashboard(TestContext context){
        Async async = context.async();
        vertx.deployVerticle(MetricsDashboardVerticle.class.getName(),result->{
            assertThat(result.succeeded()).isEqualTo(true);
            vertx.eventBus().<JsonObject>consumer("metrics", msg->{
                System.out.println(msg.body().toString());
                async.complete();
            });

        });
    }

    @Test
    public void test2MetricsTest(TestContext context){
        Async async = context.async();
        vertx.deployVerticle(MetricsTestVerticle.class.getName());
        AtomicBoolean complete = new AtomicBoolean(false);
         vertx.eventBus().<String>consumer("whatever", msg -> {
            assertThat(msg.body()).isEqualTo("hello");
            if(!complete.get()){
                complete.set(true);
                async.complete();
            }
        });
    }
}
