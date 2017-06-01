package org.ft.test.vertx.aliyun.oss;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.io.InputStream;
import java.util.List;

/**
 * 异步客户端接口
 * {@link com.aliyun.oss.OSS}
 * Created by fangtong on 2017/6/1.
 */
public interface AsyncOSSClient {


    /**
     * 创建bucket
     *
     * @param request
     * @param handler
     */
    void createBucket(CreateBucketRequest request, Handler<AsyncResult<Bucket>> handler);

    /**
     * 删除buclet
     *
     * @param bucketName
     * @param handler
     */
    void deleteBucket(String bucketName, Handler<AsyncResult<Void>> handler);

    /**
     * 获取bucket列表
     *
     * @param request
     * @param handler
     */
    void listBuckets(ListBucketsRequest request, Handler<AsyncResult<BucketList>> handler);

    /**
     * 列出指定{@link Bucket}下的{@link OSSObject}。
     * @param listObjectsRequest
     * @param handler
     */
    void listObjects(ListObjectsRequest listObjectsRequest,Handler<AsyncResult<ObjectListing>> handler);
    /**
     * 上传object
     * @param bucketName
     * @param key
     * @param input
     * @param handler
     */
    void putObject(String bucketName, String key, InputStream input, Handler<AsyncResult<PutObjectResult>> handler);


    /**
     * 可追加object
     * @param appendObjectRequest
     * @param handler
     */
    void  appendObject(AppendObjectRequest appendObjectRequest,Handler<AsyncResult<AppendObjectResult>> handler);

    /**
     * 文件上传
     *
     * 上传的文件分成若干个分片分别上传，最后所有分片都上传 成功后，完成整个文件的上传。
     * 在上传的过程中会记录当前上传的进度信息，记 录在checkpoint文件中。
     * 如果上传过程中某一分片上传失败，再次上传时会从 checkpoint文件中记录的点继续上传。
     * 这要求再次调用时要指定与上次相同的 checkpoint文件。上传完成后checkpoint文件会被删除。
     * 默认一个线程、不开启checkpoint。
     *
     * @param uploadFileRequest uploadFileRequest上传文件请求
     * @param handler
     */
    void  uploadFile(UploadFileRequest uploadFileRequest,Handler<AsyncResult<UploadFileResult>> handler);

    /**
     * 获取指定的{@link OSSObject}的基本元信息。
     * @param request
     * @param handler
     */
    void getSimplifiedObjectMeta(GenericRequest request,Handler<AsyncResult<SimplifiedObjectMeta>> handler);

    /**
     * 删除指定的{@link OSSObject}。
     * @param request
     * @param handler
     */
    void deleteObject(GenericRequest request, Handler<AsyncResult<Object>> handler);

    /**
     * 获取{@link Bucket}的Lifecycle规则列表。
     * @param bucketName
     * @param handler
     */
    void  getBucketLifecycle(String bucketName,Handler<AsyncResult<List<LifecycleRule>>> handler);

    /**
     * 设置{@link Bucket}的Lifecycle规则。
     * @param setBucketLifecycleRequest 请求参数。
     */
    void setBucketLifecycle(SetBucketLifecycleRequest setBucketLifecycleRequest,Handler<AsyncResult<Void>> handler);
}
