/**
 *
 */
package org.ft.test.vertx.netty;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.Pump;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.ft.test.vertx.metrics.MetricsDashboardVerticle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fangtong
 */
public class NettyServerVerticle extends AbstractVerticle {

    public static void main(String[] args) {
//        VertxOptions DROPWIZARD_OPTIONS = new VertxOptions().
//                setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
//        Vertx vertx = Vertx.vertx(DROPWIZARD_OPTIONS);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(NettyServerVerticle.class.getName(),
                new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors() * 2),
                result -> {
                    System.out.println(result.succeeded());
                });
//        vertx.deployVerticle(new MetricsDashboardVerticle());

    }

    private NetServer netServer;
    private Map<NetSocket, ActorVerticle> mapping = new ConcurrentHashMap<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        NetServerOptions option = new NetServerOptions();
        option.setIdleTimeout(60000)
                .setTcpKeepAlive(true);
        netServer = vertx.createNetServer(option)
                .connectHandler(serverConnectHandle())
                .listen(1000, result -> {
                    if (result.succeeded()) {
                        startFuture.complete();
                    } else {
                        System.out.println(result.cause());
                        startFuture.fail(result.cause());
                    }

                });
    }

    public void close() {
        netServer.close();
    }

    /**
     * @return
     */
    private Handler<NetSocket> serverConnectHandle() {
        return netsocket -> {
            //create and bind Actor
            DeploymentOptions deploymentOptions = new DeploymentOptions()
                    .setConfig(new JsonObject().put("socket",netsocket));
            ActorVerticle actor = new ActorVerticle();
            vertx.deployVerticle(actor,deploymentOptions, result -> {
                mapping.put(netsocket, actor);
                netsocket
                        .closeHandler(netsocketCloseHandler(netsocket))
                        .exceptionHandler(netsocketExceptionHandler(netsocket));
                //System.out.println("server:"+netsocket.writeHandlerID()+" socket connect");
            });
        };
    }


    /**
     * @return
     */
    private Handler<Void> netsocketCloseHandler(NetSocket netsocket) {
        return v -> {
            if (mapping.containsKey(netsocket)) {
                ActorVerticle actor = mapping.get(netsocket);
                mapping.remove(netsocket);
                vertx.undeploy(actor.deploymentID());
            }
            //System.out.println("server:"+netsocket.writeHandlerID()+" socket end");
        };
    }


    /**
     * @return
     */
    private Handler<Throwable> netsocketExceptionHandler(NetSocket netsocket) {
        return throwable -> {
            //throwable.printStackTrace();
            System.out.println(throwable.getMessage());
            netsocket.close();
        };
    }

    @Override
    public void stop() throws Exception {
        mapping.clear();
        super.stop();
    }
}
