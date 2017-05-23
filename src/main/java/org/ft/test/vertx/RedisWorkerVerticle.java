/**
 * 
 */
package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * @author fangtong
 *
 */
public class RedisWorkerVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        // Create the redis client
        final RedisClient client = RedisClient.create(vertx,
                new RedisOptions().setHost("127.0.0.1"));

        client.set("key", "value", r -> {
            if (r.succeeded()) {
                System.out.println("key stored");
                client.get("key", s -> {
                    System.out.println("Retrieved value: " + s.result());
                });
            } else {
                System.out.println("Connection or Operation Failed " + r.cause());
            }
        });
    }
}
