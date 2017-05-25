/**
 *
 */
package org.ft.test.vertx.netty;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

import java.util.UUID;

/**
 * @author fangtong
 */
public class NettyClientVerticle extends AbstractVerticle {

    NetSocket netSocket;
    ActorVerticle bindActor;
    Handler<String> connectSuccessHandler;

    public NettyClientVerticle connectSuccessHandle(Handler<String> handle){
        connectSuccessHandler = handle;
        return this;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<Void> future = connectServer();
        future.setHandler(result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(future.cause());
            }
        });
    }

    /**
     * 连接服务器
     * @return
     */
    private Future<Void> connectServer() {
        Future<Void> future = Future.future();
        NetClientOptions options = new NetClientOptions();
        options.setConnectTimeout(10000)
                .setReconnectAttempts(10)
                .setReconnectInterval(500);
        vertx.createNetClient(options)
                .connect(1000, "localhost", result -> {
                    if (result.succeeded()) {
                        init(result.result());
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }

                });
        return future;
    }

    /**
     * 初始化socket
     *
     * @param netSocket
     */
    private void init(NetSocket netSocket) {
        this.netSocket = netSocket;
        bindSocket(netSocket)
                .handler(handler())
                //close = end
                .endHandler(endHandler());
    }

    /**
     * 绑定actor
     *
     * @param netSocket
     */
    private NetSocket bindSocket(NetSocket netSocket) {
        JsonObject config = new JsonObject()
                .put("socket_id", netSocket.writeHandlerID());
        DeploymentOptions deployOptions = new DeploymentOptions()
                .setConfig(config);
        final ActorVerticle actorVerticle = new ActorVerticle();
        vertx.deployVerticle(actorVerticle, deployOptions, result -> {
            if (result.succeeded()) {
                bindActor = actorVerticle;
                if(connectSuccessHandler != null){
                    connectSuccessHandler.handle(bindActor.deploymentID());
                }
            } else {
                netSocket.close();
            }
        });
        return netSocket;
    }

    /**
     * 数据接收 直接传递给actor
     * @return
     */
    private Handler<Buffer> handler() {
        return buffer -> {
            bindActor.reciveBufferHandle(buffer);
        };
    }

    /**
     * socket close
     *
     * @return
     */
    private Handler<Void> endHandler() {
        return v -> {
            System.out.println("socket end");
            Future.<Void>future( f-> {
                //undeploy
                if(bindActor != null){
                    String deploymentId = bindActor.deploymentID();
                    bindActor = null;
                    vertx.undeploy(deploymentId,f.completer());
                }
            }).setHandler(asyncresult->{
                //重连
                connectServer().setHandler(result -> {
                    System.out.println(result.succeeded());
                });
            });
        };
    }

}
