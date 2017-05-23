package org.ft.test.vertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.util.LinkedHashMap;
import java.util.Map;

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
        vertx.eventBus().<String>localConsumer("local",msg->{
            System.out.println(msg.body());
        });
        vertx.eventBus().send("local","123");
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



        String starttime = System.currentTimeMillis() / 1000 + "";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("appversion", "1");
        map.put("appid", "11610002");
        map.put("order", "s" + starttime + "t" + System.currentTimeMillis());
        map.put("sdkversion", "10");
        map.put("paychannel", "1161");
        map.put("partner", "1161");
        map.put("starttime", starttime);
        map.put("imei", "869634023941666");
        map.put("price", "1000");
        map.put("payType", "ONCE");
        map.put("imsi","460027685434623");
        map.put("chargeid", "1");
        map.put("phone", "18768563636");
        //http://hzhongmeng.com:8888/creq?totalMem=1882524&screen=1080x1920&phoneType=Mi-4c&gprsType=3&phone=13681701041&yiyou=0&freeMem=147932&simstate=5&osversion=22&paychannel=1161_null&imei=869634023941665&smssent=0&appversion=1&starttime=1495429832339&chargeid=1&manu=Xiaomi&imsi=460001751861423&order=1728ad1cb93ab651495429857746&iccid=898600680916f7001198&egame=0&steplog=&appid=11610002&sdcid=15010041474e44335201a6def4b81300&payResult=100&callRec=99&uusdk=0&payType=ONCE&price=1000&weixin=1&sdkversion=10&brand=Xiaomi&zyf=0&strategy=0&cell=16887042&yummy=0&partner=1161&lac=4318&md5=5230d755dd4dd049888e798036881cfe

//        map.put("gprsType", "3");
//        map.put("sdcid", "15010041474e44335201a6def4b81300");
//        map.put("lac", "34667");
//        map.put("totalMem", "1882524");
//        map.put("screen", "1080x1920");
//        map.put("phoneType", "Mi-4");
//        map.put("simstate", "5");
//        map.put("osversion", "22");
//        map.put("smssent", "0");
//        map.put("iccid", "898600680916f7001198");



        Future.<String>future(f->{
            //1 构建异步查询参数及签名 work线程处理
            vertx.executeBlocking(md5Future->{
                StringBuilder builder1 = new StringBuilder();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    builder1.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                builder1.deleteCharAt(builder1.length() - 1);
                String sign = test.md5(test.md5(builder1.toString()) + "UY699OPCCZTR");
                map.put("md5",sign);

                builder1 = new StringBuilder();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    builder1.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                builder1.deleteCharAt(builder1.length() - 1);
                md5Future.complete(builder1.toString());
            },f.completer());
        }).compose(query->{
            //2 进行http 异步查询 work线程处理
            Future<String> f1 = Future.future();
            vertx.executeBlocking(requestFuture->{
                vertx.createHttpClient().get(8888,"hzhongmen1g.com","/creq?"+query,result->{
                    if(result.statusCode() != 200){
                        requestFuture.fail("status code != 200");
                    }
                    result.bodyHandler(buffer->{
                        requestFuture.complete(buffer.toString());
                    }) ;
                }).exceptionHandler(error->{
                    requestFuture.fail(error);
                    System.out.println(error.getMessage());
                }).end();
            },f1.completer());
          return f1;
        }).compose(response->{
            //3 同步解码 同一线程处理
           return Future.succeededFuture(test.conv(response));
        }).setHandler(result->{
            //4 输出结果
            if(result.succeeded()){
                System.out.println(result.result());
            }else{
                System.out.println(result.cause());
            }
        });

    }

}
 