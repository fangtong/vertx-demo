package org.ft.test.vertx.aliyun.oss;

import com.aliyun.oss.model.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by fangtong on 2017/6/1.
 */
@RunWith(VertxUnitRunner.class)
public class AliyunOSSVerticleTest {

    Vertx vertx;
    AliYunOSSVerticle aliYunOSSVerticle;
    String bucketName = "test-ft";

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
                        .put("endpoint", properties.getProperty("endpoint")));

        aliYunOSSVerticle = AliYunOSSVerticle.generateProxyVerticle();
        vertx.deployVerticle(aliYunOSSVerticle, op, deployResult -> {
            testContext.assertTrue(deployResult.succeeded());
            async.countDown();
        });
    }

    @After
    public void after(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void createBucket(TestContext testContext) {
        Async async = testContext.async();
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        aliYunOSSVerticle.createBucket(createBucketRequest, result -> {
            System.out.println(result.succeeded());
            if (result.succeeded()) {
                System.out.println(result.result().getName());
                testContext.assertEquals(result.result().getName(), bucketName);
                async.countDown();
            }
        });
    }

    @Test
    public void deleteBucket(TestContext testContext) {
        Async async = testContext.async();
        aliYunOSSVerticle.deleteBucket(bucketName, deleteResult -> {
            System.out.println("delete " + deleteResult.succeeded());
            async.countDown();
        });
    }

    @Test
    public void listBuckets(TestContext testContext) {
        Async async = testContext.async();
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
        aliYunOSSVerticle.listBuckets(listBucketsRequest, result -> {
            System.out.println(result.succeeded());
            if (result.succeeded()) {
                BucketList list = result.result();
                list.getBucketList().forEach(bucket -> System.out.println(Json.encode(bucket)));
            }
            async.countDown();
        });
    }

    @Test
    public void putObject(TestContext testContext) {
        Async async = testContext.async();

        aliYunOSSVerticle.putObject(
                bucketName, "String-Key",
                new ByteArrayInputStream("String-Val".getBytes()),
                result -> {
                    System.out.println("put " + result.succeeded());
                    if (result.succeeded()) {
                        System.out.println(Json.encode(result.result()));
                    }
                    async.countDown();
                });
    }

    @Test
    public void appendObject(TestContext testContext) {
        Async async = testContext.async();
        AppendObjectRequest request = new AppendObjectRequest(
                bucketName, "Append-Key", new ByteArrayInputStream("append\n".getBytes()));
        request.setPosition(0L);
        aliYunOSSVerticle.appendObject(
                request,
                result -> {
                    System.out.println("put " + result.succeeded());
                    if (result.succeeded()) {
                        System.out.println(Json.encode(result.result()));
                    }
                    async.countDown();
                });
    }

    @Test
    public void getSimplifiedObjectMeta(TestContext testContext) {
        Async async = testContext.async();
        GenericRequest request = new GenericRequest(bucketName, "String-Key");
        aliYunOSSVerticle.getSimplifiedObjectMeta(
                request,
                result -> {
                    System.out.println("put " + result.succeeded());
                    if (result.succeeded()) {
                        System.out.println(Json.encode(result.result()));
                    }
                    async.countDown();
                });
    }


    @Test
    public void listObjects(TestContext testContext) {
        Async async = testContext.async();
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        //request.setDelimiter("/");
        aliYunOSSVerticle.listObjects(request,
                result -> {
                    System.out.println("list objects " + result.succeeded());
                    if (result.succeeded()) {
                        result.result().getObjectSummaries().forEach(ossObjectSummary -> System.out.println(Json.encode(ossObjectSummary)));
                        result.result().getCommonPrefixes().forEach(prefixe -> System.out.println(prefixe));
                    } else {
                        System.out.println(result.cause().getMessage());
                    }
                    async.countDown();
                });
    }

    @Test
    public void deleteObject(TestContext testContext) {
        Async async = testContext.async();
        GenericRequest request = new GenericRequest(bucketName, "String-Key");
        aliYunOSSVerticle.deleteObject(
                request,
                result -> {
                    System.out.println("delete " + result.succeeded());
                    async.countDown();
                });
    }

    @Test
    public void getBucketLifecycle(TestContext testContext) {
        Async async = testContext.async();
        aliYunOSSVerticle.getBucketLifecycle(bucketName,
                result -> {
                    System.out.println("list objects " + result.succeeded());
                    if (result.succeeded()) {
                        result.result().forEach(lifecycleRule -> System.out.println(Json.encode(lifecycleRule)));
                    } else {
                        System.out.println(result.cause().getMessage());
                    }
                    async.countDown();
                });
    }

    @Test
    public void setBucketLifecycle(TestContext testContext) {
        Async async = testContext.async();
        SetBucketLifecycleRequest setBucketLifecycleRequest = new SetBucketLifecycleRequest(bucketName);
        LifecycleRule temp = new LifecycleRule("temp-3days", "temp-", LifecycleRule.RuleStatus.Enabled, 3);
        setBucketLifecycleRequest.AddLifecycleRule(temp);
        aliYunOSSVerticle.setBucketLifecycle(setBucketLifecycleRequest,
                result -> {
                    System.out.println("set bucket lifecycle " + result.succeeded());
                    if (!result.succeeded()) {
                        System.out.println(result.cause().getMessage());
                    }
                    async.countDown();
                });
    }

    @Test
    public void uploadFile(TestContext testContext) {
        Async async = testContext.async();
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, "Upload-Key");
        uploadFileRequest.setUploadFile("G://test.jsp");
        // 指定上传并发线程数
        uploadFileRequest.setTaskNum(5);
        // 指定上传的分片大小
        uploadFileRequest.setPartSize(1 * 1024 * 1024);
        // 开启断点续传
        uploadFileRequest.setEnableCheckpoint(true);
        aliYunOSSVerticle.uploadFile(
                uploadFileRequest,
                result -> {
                    System.out.println("uploadFile " + result.succeeded());
                    if (result.succeeded()) {
                        System.out.println(Json.encode(result.result().getMultipartUploadResult()));
                    }
                    async.countDown();
                });
    }
}
