/**
 *
 */
package org.ft.test.vertx.netty;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.Pump;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fangtong
 */
public class NettyClientVerticle extends AbstractVerticle {
    static volatile int a = 0;
    static volatile int b = 0;
    public static void main(String[] args) {
        int delay = 1000, val1 = 10, val2 = 500;
        String host = "10.20.100.115";
        if (args.length > 3) {
            host = args[0];
            delay = Integer.valueOf(args[1]);
            val1 = Integer.valueOf(args[2]);
            val2 = Integer.valueOf(args[3]);
        }
        Vertx vertx = Vertx.vertx();
        DeploymentOptions op = new DeploymentOptions().
                setConfig(new JsonObject().put("host", host));

        List<String> list = new ArrayList<>();
        final int cnt = val1;
        final int max = val2;
        vertx.setPeriodic(delay, t -> {
            if (b> max && max != 0) return;
            for (int i = 0; i < cnt; i++) {
                vertx.deployVerticle(new NettyClientVerticle().connectSuccessHandle(
                        actor -> {
                            //actor.sendMessage(Buffer.buffer("1"));
                            list.add(actor.deploymentID());
                        }
                ), op, result -> {
                    if (result.succeeded()) a++;
                    b++;
                });
            }
            System.out.println("success:" + a+"/"+b);
        });
        vertx.setPeriodic(1000,t->{
            if(b>= max) {
                for(String actorid:list){
                    if(actorid == null) continue;;
                    vertx.eventBus().send(
                            actorid,"1");
                };
                System.out.println("send:"+list.size());
            }
        });
    }

    private NetSocket netSocket;
    private ActorVerticle bindActor;
    private Handler<ActorVerticle> connectSuccessHandler;
    private boolean reConnnect;
    private String host;
    private int port;

    public NettyClientVerticle connectSuccessHandle(Handler<ActorVerticle> handle) {
        connectSuccessHandler = handle;
        return this;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        host = config().getString("host", "localhost");
        port = config().getInteger("port", 1000);
        reConnnect = config().getBoolean("reConnect", false);

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
     *
     * @return
     */
    private Future<Void> connectServer() {
        Future<Void> future = Future.future();
        NetClientOptions options = new NetClientOptions();
        options.setConnectTimeout(10000)
                .setReconnectAttempts(10)
                .setReconnectInterval(500);
        vertx.createNetClient(options)
                .connect(port, host, result -> {
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
                //close = end
                .endHandler(endHandler());
//        System.out.println("client:" + netSocket.writeHandlerID() + " socket connect");
    }

    /**
     * 绑定actor
     *
     * @param netSocket
     */
    private NetSocket bindSocket(NetSocket netSocket) {
        final ActorVerticle actorVerticle = new ActorVerticle();
        final DeploymentOptions op = new DeploymentOptions()
                .setConfig(new JsonObject().put("socket",netSocket));
        vertx.deployVerticle(actorVerticle,op, result -> {
            if (result.succeeded()) {
                bindActor = actorVerticle;
                if (connectSuccessHandler != null) {
                    connectSuccessHandler.handle(bindActor);
                }
            } else {
                netSocket.close();
            }
        });
        return netSocket;
    }

    /**
     * socket close
     *
     * @return
     */
    private Handler<Void> endHandler() {
        return v -> {
            System.out.println("client:" + netSocket.writeHandlerID() + " socket end");
            vertx.<Void>executeBlocking(f -> {
                //undeploy
                if (bindActor != null) {
                    String deploymentId = bindActor.deploymentID();
                    bindActor = null;
                    vertx.undeploy(deploymentId, f.completer());
                }
            }, asyncresult -> {
                //后续操作
                //重连
                if (reConnnect) {
                    connectServer().setHandler(result -> {
                        System.out.println(result.succeeded());
                    });

                }
            });
        };
    }

    public void close() {
        if (netSocket != null) {
            netSocket.close();
        }
    }
}
