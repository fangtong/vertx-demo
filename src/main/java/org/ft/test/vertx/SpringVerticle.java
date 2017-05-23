package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Created by fangtong on 2017/5/22.
 */
public class SpringVerticle extends AbstractVerticle {

    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);


        vertx.executeBlocking(future -> {
            context.scan("");
            context.refresh();
        }, false, startFuture.completer());

    }



}
