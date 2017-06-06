package org.ft.test.vertx.zookeeper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by fangtong on 2017/6/2.
 */
@RunWith(VertxUnitRunner.class)
public class CuratorClientTest {

    Vertx vertx;
    CuratorClient client;

    @Before
    public void before(TestContext testContext) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(testContext.exceptionHandler());

        Async async = testContext.async();
        JsonObject config = new JsonObject()
                .put("listen", "/")
                .put("zookeeperHosts", "10.20.100.108:2181");
        client = CuratorClient.createSharedClient(vertx, config);
        client.start(result -> {
            async.countDown();
        });
    }

    @After
    public void after(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }


    @Test
    public void getNodes(TestContext testContext) {
        client.getNodes().forEach(node -> System.out.println(node));
    }

    @Test
    public void exec(TestContext testContext){
        Async async = testContext.async(5);
        //listener
        client.nodeListener(event->{
            System.out.println(event.getType()+"|"+event.getData().getPath());
            async.countDown();
        });

        //listener 只能知道data change 不能像上面那个细分到add delete等
        CuratorWatcher watcher = event->{
            System.out.println("watch:"+event.getType()+"|"+event.getPath());
        };
        client.exec(curator->{
            try {
                curator.getChildren().usingWatcher(watcher).forPath("/");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        },f->{
            async.countDown();
        });

        //delete
        client.exec(curator->{
            try { curator.delete().forPath("/test1"); } catch (Throwable e) { e.printStackTrace();}
            return null;
        },f->{
            System.out.println("delete complete");
            async.countDown();
        });

        //create
        client.exec(curator->{
            try {
                curator.create().withMode(CreateMode.EPHEMERAL).forPath("/test1");
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        },f->{
            System.out.println("create complete");
            async.countDown();
        });

        //child
        client.exec(curator->{
            List<String> list = null;
            try {
                list =  curator.getChildren().forPath("/");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        },f->{
            f.result().forEach(path-> System.out.print(path));
            System.out.println();
            async.countDown();
        });





    }
}
