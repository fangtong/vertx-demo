/**
 * 
 */
package org.ft.test.vertx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * @author fangtong
 *
 */
public class Verticle1 extends AbstractVerticle {

	long periodicId;
	
	@Override
	public void start() throws Exception {
		
		vertx.eventBus().consumer("test").handler(msg -> {
			System.out.println("v1:"+Thread.currentThread().getName());
			System.out.println("v1:"+msg.body());

			msg.headers().forEach(head -> {
				System.out.println("v1:"+head.getKey() + "/" + head.getValue());
			});

			
			JsonObject obj = new JsonObject();
			obj.put("msg", "reply");
			msg.reply(obj,
					new DeliveryOptions().setHeaders(new CaseInsensitiveHeaders().add("head", "test-head")));
		});
		
		vertx.getOrCreateContext().runOnContext(act->{
			System.out.println("runOnContext:"+Thread.currentThread().getName());
		});
		//1庙后运行
		long timerId = vertx.setTimer(1000, time->{
			System.out.println("time run:"+time);
		});
		
		periodicId = vertx.setPeriodic(1000, time->{
			System.out.println("periodic run:"+time);
			vertx.cancelTimer(periodicId);
		});
		
	}
	
	
	
	
}
