package org.ft.test.vertx.metrics;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * Created by fangtong on 2017/5/18.
 */
public class MetricsDashboardVerticle extends AbstractVerticle {

    @Override
    public void start() {


            MetricsService service = MetricsService.create(vertx);

            Router router = Router.router(vertx);

            // Allow outbound traffic to the news-feed address

            BridgeOptions options = new BridgeOptions().
                    addOutboundPermitted(
                            new PermittedOptions().
                                    setAddress("metrics")
                    );

            router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options));

            // Serve the static resources
            router.route().handler(StaticHandler.create());

            HttpServer httpServer = vertx.createHttpServer();
            httpServer.requestHandler(router::accept).listen(8080);

            // Send a metrics events every second
            vertx.setPeriodic(1000, t -> {
                JsonObject metrics = service.getMetricsSnapshot(vertx.eventBus());
                vertx.eventBus().publish("metrics", metrics);
            });



    }

}
