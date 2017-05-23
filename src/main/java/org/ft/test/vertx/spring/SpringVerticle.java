package org.ft.test.vertx.spring;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
