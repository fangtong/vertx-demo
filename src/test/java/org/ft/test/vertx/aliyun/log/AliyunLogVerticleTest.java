package org.ft.test.vertx.aliyun.log;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

/**
 * Created by fangtong on 2017/6/2.
 */
@RunWith(VertxUnitRunner.class)
public class AliyunLogVerticleTest {

    Vertx vertx;
    AliyunLogVerticle aliyunLogVerticle;

    @Before
    public void before(TestContext testContext) {
        Async async = testContext.async();

        vertx = Vertx.vertx();
        vertx.exceptionHandler(testContext.exceptionHandler());

        Properties properties = new Properties();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/aliyun.conf");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DeploymentOptions op = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("accessKeyId", properties.getProperty("accessKeyId"))
                        .put("accessKeySecret", properties.getProperty("accessKeySecret"))
                        .put("endpoint", properties.getProperty("log-endpoint"))
                        .put("project",properties.getProperty("log-project"))
                        .put("store",properties.getProperty("log-store"))
                );

        aliyunLogVerticle = new AliyunLogVerticle();
        vertx.deployVerticle(aliyunLogVerticle, op, deployResult -> {
            if(!deployResult.succeeded()){
                System.out.println(deployResult.cause().getMessage());
            }
            System.out.println("deploy:"+deployResult.succeeded());
            testContext.assertTrue(deployResult.succeeded());
            async.countDown();
        });
    }

    @After
    public void after(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }


    @Test
    public void putLog(TestContext testContext){
        Async async = testContext.async();
        JsonObject log = new JsonObject();
        log.put("uid","1")
                .put("key", RandomStringUtils.random(10,"abcdefghijklmnopqrstuvwxyz"));
        aliyunLogVerticle.putLog(log,"login","",r->{
            System.out.println(r.succeeded());
            if(r.succeeded()){
                System.out.println(r.result().GetRequestId());
            }else{
                System.out.println(r.cause().getMessage());
            }
            async.countDown();
        });
    }

    @Test
    public void followFilePutLog(TestContext testContext){
        FileSystem fileSystem = vertx.fileSystem();
        OpenOptions openOptions = new OpenOptions();
        fileSystem.open("",openOptions,result->{
            if(result.succeeded()){
                AsyncFile asyncFile = result.result();
                Buffer buffer = Buffer.buffer(1024);
            }else{

            }
        });
    }
}
