package org.ft.test.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;

import java.util.Arrays;

/**
 * Created by fangtong on 2017/5/17.
 */
public class MailClientVerticle extends AbstractVerticle{

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        MailConfig mailConfig = new MailConfig()
                .setHostname("smtp.qiye.163.com")
                //.setStarttls(StartTLSOptions.REQUIRED)
                //.setLogin(LoginOption.REQUIRED)
                .setLogin(LoginOption.REQUIRED)
                .setUsername("sdk-notice@xl-game.cn")
                .setPassword("!Q@W3e4r");
        MailClient mailClient = MailClient.createShared(vertx, mailConfig);


        vertx.eventBus().consumer("vertx.mail",message->{
            JsonObject msg = (JsonObject)message.body();
            MailMessage email = new MailMessage()
                    .setFrom("sdk-notice@xl-game.cn")
                    .setTo(Arrays.asList("fangtong@xl-game.cn"))
                    .setSubject(msg.getString("title"))
                    .setHtml(msg.getString("msg"));
            mailClient.sendMail(email, result -> {
                if (result.succeeded()) {
                    System.out.println(result.result());
                    System.out.println("Mail sent");
                } else {
                    System.out.println("got exception");
                    result.cause().printStackTrace();
                }
            });
        });


    }
}
