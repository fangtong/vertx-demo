package org.ft.test.vertx.zookeeper;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.NodeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;
import java.util.function.Function;

/**
 * Created by fangtong on 2017/6/6.
 */
public interface CuratorClient{


    static CuratorClient createSharedClient(Vertx vertx, JsonObject config){
        return new CuratorClientImpl(vertx,config);
    }

    List<String> getNodes();

    boolean isActive();

    void nodeListener(Handler<PathChildrenCacheEvent> listener);

    void start(Handler<AsyncResult<Void>> resultHandler);

    <T> void exec(Function<CuratorFramework,T> execHandler, Handler<AsyncResult<T>> result);
}
