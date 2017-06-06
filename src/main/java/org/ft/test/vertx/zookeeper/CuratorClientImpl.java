package org.ft.test.vertx.zookeeper;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.NodeListener;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by fangtong on 2017/6/6.
 */
public class CuratorClientImpl implements CuratorClient, PathChildrenCacheListener {


    private Vertx vertx;
    private CuratorFramework curator;
    private PathChildrenCache clusterNodes;
    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;
    private volatile boolean active;
    private JsonObject conf;
    private List<Handler<PathChildrenCacheEvent>> listeners = new CopyOnWriteArrayList<Handler<PathChildrenCacheEvent>>();;

    public CuratorClientImpl(Vertx vertx, JsonObject config) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(config);
        this.vertx = vertx;
        this.conf = config;
    }

    @Override
    public void nodeListener(Handler<PathChildrenCacheEvent> listener) {
        Context context = vertx.getOrCreateContext();
        listeners.add(event->{
            context.runOnContext(h->listener.handle(event));
        });
    }

    @Override
    public synchronized void start(Handler<AsyncResult<Void>> resultHandler) {
        vertx.executeBlocking(future -> {
            if (!active) {
                active = true;
                if (curator == null) {
                    retryPolicy = new ExponentialBackoffRetry(
                            conf.getJsonObject("retry", new JsonObject()).getInteger("initialSleepTime", 1000),
                            conf.getJsonObject("retry", new JsonObject()).getInteger("maxTimes", 5),
                            conf.getJsonObject("retry", new JsonObject()).getInteger("intervalTimes", 10000));

                    // Read the zookeeper hosts from a system variable
                    String hosts = System.getProperty("vertx.zookeeper.hosts");
                    if (hosts == null) {
                        hosts = conf.getString("zookeeperHosts", "127.0.0.1");
                    }
                    curator = CuratorFrameworkFactory.builder()
                            .connectString(hosts)
                            .namespace(conf.getString("namesapce", ""))
                            .sessionTimeoutMs(conf.getInteger("sessionTimeout", 20000))
                            .connectionTimeoutMs(conf.getInteger("connectTimeout", 3000))
                            .retryPolicy(retryPolicy).build();
                }
                curator.start();

                while (curator.getState() != CuratorFrameworkState.STARTED) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        if (curator.getState() != CuratorFrameworkState.STARTED) {
                            future.fail("zookeeper client being interrupted while starting.");
                        }
                    }
                }
                try {
                    listenPath(conf.getString("listen","/"));
                    future.complete();

                } catch (Exception e) {
                    future.fail(e);
                }
            }
        }, resultHandler);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private void listenPath(String path) throws Exception {
        clusterNodes = new PathChildrenCache(curator, path, true);
        clusterNodes.getListenable().addListener(this);
        clusterNodes.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
    }

    @Override
    public List<String> getNodes() {
        return clusterNodes.getCurrentData().stream().map(e -> new String(e.getPath())).collect(Collectors.toList());
    }

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        if (!active) return;
//        switch (event.getType()) {
//            case CHILD_ADDED:
//                break;
//            case CHILD_REMOVED:
//                break;
//            case CHILD_UPDATED:
//                break;
//            case CONNECTION_SUSPENDED:
//                break;
//            case CONNECTION_LOST:
//                break;
//        }
        listeners.forEach(handle->handle.handle(event));
    }



    public <T> void exec(Function<CuratorFramework,T> execHandler,Handler<AsyncResult<T>> result){
        if(!active){
            result.handle(Future.failedFuture(new Exception("not active")));
        }
        vertx.<T>executeBlocking(f->{
            T res = execHandler.apply(curator);
            f.complete(res);
        },result);
    }
}
