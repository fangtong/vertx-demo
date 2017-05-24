package org.ft.test.vertx.netty;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

/**
 * Created by fangtong on 2017/5/24.
 */
public class ActorVerticle extends AbstractVerticle{

    private String bindNetSocketId;

    @Override
    public void start() throws Exception {
        //绑定socket write id 用于发送数据
        bindNetSocketId = config().getString("socket_id");
        //bindNetSocketId 用于接收网络请求
        vertx.eventBus().consumer("actor_"+bindNetSocketId,reciveBufferHandle());

        //绑定deploymentID 用于actor接收数据
        vertx.eventBus().consumer(deploymentID(),reciveHandle());
        super.start();
    }

    public Handler<Message<Buffer>> reciveBufferHandle(){
        return message->{
            System.out.println(message.body().toString());
        };
    }

    public Handler<Message<String>> reciveHandle(){
        return message->{
            vertx.eventBus().send(bindNetSocketId,Buffer.buffer(message.body()));
        };
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
