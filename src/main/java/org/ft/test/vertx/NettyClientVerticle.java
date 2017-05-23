/**
 * 
 */
package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

/**
 * @author fangtong
 *
 */
public class NettyClientVerticle extends AbstractVerticle {

	NetSocket netSocket ;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Future<Void> future = connectServer();
		future.setHandler(result->{
			if(result.succeeded()){
				startFuture.complete();
			}else{
				startFuture.fail(future.cause());
			}
		});
	}

	private Future<Void> connectServer(){
		Future<Void> future = Future.future();
		NetClientOptions options  = new NetClientOptions();
		options.setConnectTimeout(10000)
				.setReconnectAttempts(10)
				.setReconnectInterval(500);
		vertx.createNetClient(options)
				.connect(1000,"localhost", result->{
					if(result.succeeded()){
						init(result.result());
						future.complete();
					}else{
						future.fail(result.cause());
					}

				});
		return future;
	}
	
	private void init(NetSocket netSocket){
		this.netSocket = netSocket;
		netSocket.handler(handler())
		.endHandler(endHandler());
		
		vertx.setPeriodic(1000, t->{
			netSocket.write("12345");
		});
		System.out.println("init socket:" +netSocket.localAddress().toString() +"/"+netSocket.remoteAddress().toString());
	}
	
	private Handler<Buffer> handler(){
		return buffer->{
			System.out.println("client receive:"+buffer.toString());
		};
	}
	
	private Handler<Void> endHandler(){
		return v->{
			System.out.println("socket end");
			connectServer().setHandler(result->{
				System.out.println(result.succeeded());
			});
		};
	}
	
}
