package org.ft.test.vertx.utils;

import io.vertx.core.AsyncResult;

/**
 * 异步结果工具类
 * Created by fangtong on 2017/5/31.
 */
public class AsyncResultUtil {


    /**
     * 转变异步结果公用方法
     * @param orgin 原始异步结果
     * @param bodyConv 装换方法
     * @param <T> 转换类型
     * @param <G> 原始类型
     * @return 转换异步结果
     */
    public static <T,G> AsyncResult<T> transform(AsyncResult<G> orgin,BodyConv<T,G> bodyConv){
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return bodyConv.conv(orgin.result());
            }

            @Override
            public Throwable cause() {
                return orgin.cause();
            }

            @Override
            public boolean succeeded() {
                return orgin.succeeded();
            }

            @Override
            public boolean failed() {
                return orgin.failed();
            }
        };
    }

    public interface BodyConv<T,G>{
          T conv(G body);
    }


}
