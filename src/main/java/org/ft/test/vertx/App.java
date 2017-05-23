package org.ft.test.vertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.ft.test.vertx.future.FutureComposeTestVerticle;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        VertxOptions DROPWIZARD_OPTIONS = new VertxOptions().
                setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
        Future<Void> future = Future.future();
        EventBusOptions eventBusOptions = new EventBusOptions().setClustered(true);
//       Vertx.clusteredVertx(DROPWIZARD_OPTIONS.setEventLoopPoolSize(1).setEventBusOptions(eventBusOptions),result->{
        Vertx vertx = Vertx.vertx(DROPWIZARD_OPTIONS);
//           Vertx vertx = result.result();
       vertx.deployVerticle(new FutureComposeTestVerticle());
//        vertx.deployVerticle(new MetricsDashboardVerticle(),new DeploymentOptions(),result->{
//            if(result.succeeded()){
//                vertx.deployVerticle("org.ft.test.vertx.MetricsTestVerticle",new DeploymentOptions().setInstances(
//                        Runtime.getRuntime().availableProcessors()
//                ));
//            }
//        });

//       });


        //vertx.deployVerticle(new Verticle1());
        //vertx.deployVerticle(new Verticle2(), new DeploymentOptions());
        //vertx.deployVerticle(new HttpClientVerticle());
        //vertx.deployVerticle(new NettyClientVerticle());
        //vertx.deployVerticle(NettyServerVerticle.class.getName(),new DeploymentOptions().setInstances(4));
        //vertx.deployVerticle(new MailClientVerticle());
        //vertx.deployVerticle(new MongoClientVerticle());



//        JsonObject config = new JsonObject();
//       vertx.deployVerticle(new VerticleWorker(), new DeploymentOptions().setWorker(true)
//    		   .setConfig(config), asyncresult->{
//    	   if(asyncresult.succeeded()){
//    		   System.out.println("deploy worker success!");
//    	   }
//       });

    }

}
 