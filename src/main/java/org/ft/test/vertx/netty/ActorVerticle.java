package org.ft.test.vertx.netty;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.parsetools.impl.RecordParserImpl;
import io.vertx.core.streams.Pump;

/**
 * Created by fangtong on 2017/5/24.
 */
public class ActorVerticle extends AbstractVerticle{

    private NetSocket bindNetSocket;


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        bindNetSocket = (NetSocket) config().getValue("socket",null);
        if(bindNetSocket == null){
            startFuture.fail(new Exception("socket not null!"));
        }
        //绑定deploymentID 用于actor接收数据
        vertx.eventBus().<Buffer>consumer(deploymentID(),message->{
            sendMessage(message.body());
        });
//        Pump.pump(bindNetSocket,bindNetSocket).start();
        bindNetSocket.handler(buffer->{
            context.runOnContext(act->{
                sendMessage(buffer);
            });
        });
        super.start();
    }


    public void sendMessage(Buffer message){
        if(bindNetSocket != null ){
            bindNetSocket.write(message);
//            vertx.eventBus().send(bindNetSocket.writeHandlerID(),message);
        }
    }



    public void kick(){
        if(bindNetSocket != null){
            bindNetSocket.close();
        }
    }

    @Override
    public void stop() throws Exception {
        bindNetSocket = null;
        super.stop();
    }
}
