package org.ft.test.vertx.file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class FileSystemTest {

    Vertx vertx;

    @Before
    public void before(TestContext testContext) {
        vertx = Vertx.vertx();
        vertx.exceptionHandler(testContext.exceptionHandler());
    }

    @After
    public void after(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }


    @Test
    public void tail(TestContext testContext) {
        Async async = testContext.async(100);

        AtomicLong fileSize = new AtomicLong();

        ByteBuf buf = Unpooled.buffer(1024);
        Handler<AsyncResult<Buffer>> readHandler = ar -> {
            if (ar.succeeded()) {
                if (ar.result().length() > 0) {
                    fileSize.addAndGet(ar.result().length());
                    buf.writeBytes(ar.result().getBytes());
                    byte[] str = new byte[buf.writerIndex()];
                    buf.readBytes(str);
                    String[] lines =new String(str).split("\r\n");
                    if(lines.length!=0){
                        for (int i = 0; i < lines.length; i++) {
                            if (!StringUtils.isEmpty(lines[i])) {
                                if (i == lines.length - 1) {
                                    buf.clear();
                                    buf.writeBytes(lines[i].getBytes());
                                } else {
                                    System.out.println(lines[i]);
                                }
                            }
                        };
                    }
                }
            } else {
                System.out.println(ar.cause().getMessage());
            }
            async.countDown();
        };


        Future.<AsyncFile>future(f -> {
            vertx.fileSystem().open("G://test.jsp", new OpenOptions(), f.completer());
        }).compose(asyncFile -> {
            return Future.<Long>future(ff -> {
                asyncFile.size(ff.completer());
            }).compose(length -> {
                fileSize.set(length);
                vertx.setPeriodic(1000, t -> {
                    Buffer buff = Buffer.buffer(1024);
                    asyncFile.read(buff, 0, fileSize.get(), 1024, readHandler);
                });
                return Future.succeededFuture(asyncFile);
            });
        }).setHandler(f -> {
            if (f.succeeded()) {
                System.out.println(f.result());
            } else {
                System.out.println(f.cause().getMessage());
            }
        });


    }
}
