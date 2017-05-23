/**
 * 
 */
package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author fangtong
 *
 */
public class Verticle2 extends AbstractVerticle {

	
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		
		
		vertx.eventBus().addInterceptor(sendCxt->{
			sendCxt.message().headers().add("interceptor", "test");
			sendCxt.next();
		});
		
		vertx.setPeriodic(1000, time->{	
			System.out.println("v2:"+Thread.currentThread().getName());
			System.out.println("v2:send");
			vertx.eventBus().send("test", "send", result->{
				System.out.println("v2:"+Thread.currentThread().getName());
				System.out.println("v2:"+result.result());
			});
		});
		
		startFuture.complete();
	}
	
}
