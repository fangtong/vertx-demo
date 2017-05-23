/**
 * 
 */
package org.ft.test.vertx.netty;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

/**
 * @author fangtong
 *
 */
public class NettyServerVerticle extends AbstractVerticle {

	NetServer netServer;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		NetServerOptions option = new NetServerOptions();
		netServer = vertx.createNetServer(option)
		.connectHandler(serverConnectHandle())
		.listen(1000, result->{
			if(result.succeeded()){
				System.out.println("suc");
				startFuture.complete();
			}else{
				System.out.println(result.cause());
				startFuture.fail(result.cause());
			}
			
		});


		vertx.setTimer(5000,time->{
			vertx.sharedData().getLock("close_server",result->{
				if(result.succeeded()){
					netServer.close();
					result.result().release();
				}
			});
		});
	}
	
	/**
	 * 
	 * @return
	 */
	private Handler<NetSocket> serverConnectHandle(){
		return netsocket->{
			netsocket.handler(netsocketHandler(netsocket))
			.closeHandler(netsocketCloseHandler())
			.exceptionHandler(netsocketExceptionHandler());
		};
	}
	
	/**
	 * 
	 * @return
	 */
	private Handler<Buffer> netsocketHandler(NetSocket netsocket){
		return buffer->{
			System.out.println("server receive:"+buffer.toString());
			String msg = "buffer"+buffer.toString()+"/"+Thread.currentThread().getName();
			//vertx.eventBus().send(netsocket.writeHandlerID(),Buffer.buffer(msg));
			netsocket.write(msg);
		};
	}
	

	/**
	 * 
	 * @return
	 */
	private Handler<Void> netsocketCloseHandler(){
		return v->{
			System.out.println("The socket has been closed");
		};
	}
	

	/**
	 * 
	 * @return
	 */
	private Handler<Throwable> netsocketExceptionHandler(){
		return throwable->{
			throwable.printStackTrace();
		};
	}
	
}
