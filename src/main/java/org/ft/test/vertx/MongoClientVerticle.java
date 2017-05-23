package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.util.Arrays;
import java.util.List;

public class MongoClientVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {


    JsonObject mongoconfig = new JsonObject()
        .put("host", "172.31.32.6")
        .put("port",30000)
        .put("username","xingluo")
        .put("password","xlgame")
        .put("db_name", "xlgame");

    MongoClient mongoClient =  MongoClient.createShared(vertx, mongoconfig);

    mongoClient.findOne("players",new JsonObject(),new JsonObject(),asyncResult->{
        printlnResult(asyncResult);
    });

    JsonObject fields = new JsonObject()
            .put("Pid","1")
            .put("Name","1")
            .put("gmtype","1")
            .put("Coin","1");
    JsonObject sort = new JsonObject()
            .put("Coin",-1);

      FindOptions findOptions = new FindOptions()
             // .setLimit(10)
              .setFields(fields)
              .setSort(sort);
      JsonObject query = new JsonObject()
              .put("Coin",new JsonObject().put("$gt",1000000))
              .put("gmtype",new JsonObject().put("$ne",0));
    mongoClient.findWithOptions("players",query,findOptions,asyncResults->{
        printlnResults(asyncResults);

    });

      mongoClient.count("players",query,cnt->{
          System.out.println(cnt.result());
      });

  }


    private void printlnResults(AsyncResult<List<JsonObject>> asyncResult){
        if(asyncResult.succeeded()){
            String title = "斗地主金币超过100W用户统计："+asyncResult.result().size();
            StringBuilder builder = new StringBuilder();
            builder.append("<table border=3D1 ><tr><th>pid</th><th>名称</th><th>游戏类型</th><th>金币</th></tr>");
            asyncResult.result().forEach(ele->{
                System.out.println(ele);
                builder.append("<tr><td>")
                        .append(ele.getLong("Pid")).append("</td><td>")
                        .append(ele.getString("Name")).append("</td><td>")
                        .append(ele.getLong("gmtype")).append("</td><td>")
                        .append(ele.getLong("Coin")).append("</td></tr>");
            });
            builder.append("</table>");


            vertx.eventBus().send("vertx.mail",new JsonObject().put("msg",builder.toString()).put("title",title));
        }else{
            System.out.println(asyncResult.cause().toString());
        }

    }

  private void printlnResult(AsyncResult<JsonObject> asyncResult){
      if(asyncResult.succeeded()){
          System.out.println(asyncResult.result());
      }else{
          System.out.println(asyncResult.cause().toString());
      }
  }
}
