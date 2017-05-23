package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fangtong on 2017/5/19.
 */
public class MetricsTestVerticle extends AbstractVerticle{

    public static volatile int consumer = 0;
    @Override
    public void start() throws Exception {

        // Send some messages
        Random random = new Random();

//        if(consumer ==0){
//            consumer=1;
            vertx.eventBus().<String>consumer("whatever", msg -> {
//            vertx.setTimer(10 + random.nextInt(50), id -> {
//                vertx.eventBus().send("whatever", "hello");
//            });
            });
//        }

//        for (int i = 0; i < 10000; i++) {
//            vertx.eventBus().send("whatever", "hello");
//        }


            vertx.setPeriodic(1000/*+ random.nextInt(10)*/,result ->{for (int i = 0; i < 100000; i++) {vertx.eventBus().send("whatever", "hello");}});

    }
}
