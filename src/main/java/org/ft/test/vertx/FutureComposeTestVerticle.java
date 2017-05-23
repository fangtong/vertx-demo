package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fangtong on 2017/5/23.
 */
public class FutureComposeTestVerticle extends AbstractVerticle{

    @Override
    public void start() throws Exception {

        //0 同步构建部分参数
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
