package org.ft.test.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Created by fangtong on 2017/5/19.
 */
@RunWith(VertxUnitRunner.class)
public class NetServerTest {

    Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        // Register the context exception handler
        vertx.exceptionHandler(context.exceptionHandler());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

}
