/**
 * 
 */
package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

/**
 * @author fangtong
 *
 */
public class VerticleWorker extends AbstractVerticle {

	
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		Router router = Router.router(vertx);
		
		
		Thread.currentThread().sleep(1000);
		startFuture.complete();
	}
}
