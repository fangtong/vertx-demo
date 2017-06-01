package org.ft.test.vertx.aliyun.oss;

import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.BucketList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.ListBucketsRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * 异步客户端接口
 * Created by fangtong on 2017/6/1.
 */
public interface AsyncOSSClient {

    void createBucket(CreateBucketRequest request, Handler<AsyncResult<Bucket>> handler);

    void deleteBucket(String bucketName, Handler<AsyncResult<Void>> handler);

    void listBuckets(ListBucketsRequest request, Handler<AsyncResult<BucketList>> handler);


    void deleteObject(String bucketName, String key, Handler<AsyncResult<Object>> handler);
}
