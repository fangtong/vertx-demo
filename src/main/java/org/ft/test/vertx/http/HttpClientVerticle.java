package org.ft.test.vertx.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.util.Arrays;

public class HttpClientVerticle extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		HttpClientOptions options = new HttpClientOptions();
		options.setConnectTimeout(1000).setIdleTimeout(3);

		HttpClient httpClient = vertx.createHttpClient(options);

		Future<String> request1 = Future.future();
		Future<String> request2 = Future.future();
		
		
		CompositeFuture.join(Arrays.asList(request1,request2)).setHandler(result->{
			System.out.println("============="+result.succeeded());
			System.out.println(request1.result() + request2.result());
		});

		httpClient.get("www.baidu.com2", "/", response -> {
			if (response.statusCode() == 200) {
				response.bodyHandler(buffer -> {
					request1.complete(buffer.toString());
				});
			} else {
				request1.fail(response.statusCode() + "");
			}

		}).exceptionHandler(throwable->{
			System.out.println("===="+throwable.toString());
			request1.fail(throwable);
		}).setTimeout(1000);

		httpClient.getNow("www.baidu.com", "/", response -> {
			if (response.statusCode() == 200) {
				response.bodyHandler(buffer -> {
					request2.complete(buffer.toString());
				});
			} else {
				request2.fail(response.statusCode() + "");
			}
		});

		startFuture.complete();
	}
}
