package org.ft.test.vertx.aliyun.log;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.LogStore;
import com.aliyun.openservices.log.request.GetCursorRequest;
import com.aliyun.openservices.log.request.GetLogsRequest;
import com.aliyun.openservices.log.request.PutLogsRequest;
import com.aliyun.openservices.log.response.GetCursorResponse;
import com.aliyun.openservices.log.response.GetLogStoreResponse;
import com.aliyun.openservices.log.response.PutLogsResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by fangtong on 2017/6/1.
 */
public class AliyunLogVerticle extends AbstractVerticle {

    private Client logClient;
    private String project;
    private String store;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        String endpoint = config().getString("endpoint");
        String accessKeyId = config().getString("accessKeyId");
        String accessKeySecret = config().getString("accessKeySecret");
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(accessKeyId);
        Objects.requireNonNull(accessKeySecret);

        project = config().getString("project");
        store = config().getString("store");
        Objects.requireNonNull(project);
        Objects.requireNonNull(store);

        vertx.executeBlocking(f -> {
            logClient = new Client(endpoint, accessKeyId, accessKeySecret);
            try {
                GetLogStoreResponse response = logClient.GetLogStore(project, store);
                Objects.requireNonNull(response.GetLogStore());
                f.complete();
            } catch (Throwable e) {
                f.fail(e);
            }
        }, startFuture.completer());
        vertx.eventBus().<JsonObject>consumer("log-" + project + "-" + store, msg -> {
            putLog(msg.body(),null);
        });
    }

    public void putLog(JsonObject body, Handler<AsyncResult<PutLogsResponse>> handler) {
        putLog(body,"","",handler);
    }

    public void putLog(JsonObject body,String topic,String source, Handler<AsyncResult<PutLogsResponse>> handler) {
        vertx.executeBlocking(f->{
            List<LogItem> list = new ArrayList<>();
            LogItem item = new LogItem((int) (new Date().getTime() / 1000));
            body.stream().forEach(
                    stringObjectEntry ->
                            item.PushBack(stringObjectEntry.getKey(), stringObjectEntry.getValue().toString()
                            ));
            list.add(item);
            PutLogsRequest request = new PutLogsRequest(project, store, topic, source, list);
            try {
                PutLogsResponse response = logClient.PutLogs(request);
                f.complete(response);
            }catch(Throwable e){
                f.fail(e);
            }
        },false,handler);
    }

    public void GetCursor(JsonObject body,int shardId,long time, Handler<AsyncResult<GetCursorResponse>> handler) {
        vertx.executeBlocking(f->{
            GetCursorRequest request = new GetCursorRequest(project, store,shardId, time);
            try {
                GetCursorResponse response = logClient.GetCursor(request);
                f.complete(response);
            }catch(Throwable e){
                f.fail(e);
            }
        },false,handler);
    }
}
