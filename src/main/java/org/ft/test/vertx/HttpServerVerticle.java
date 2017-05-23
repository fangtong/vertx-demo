package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;

/**
 * Created by fangtong on 2017/5/12.
 */
public class HttpServerVerticle extends AbstractVerticle{

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServerOptions options = new HttpServerOptions()
                .setLogActivity(true);
        vertx.createHttpServer(options)
                .connectionHandler(connection->{

                })
                .requestHandler(request->{
                    request.response().end("Hello world");
                })
                .listen(1002,result->{
                    if(result.succeeded()){
                        startFuture.complete();
                    }else{
                        startFuture.fail(result.cause());
                    }
                });


    }
}
