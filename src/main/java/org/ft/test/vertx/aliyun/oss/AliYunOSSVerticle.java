package org.ft.test.vertx.aliyun.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阿里云oss verticle
 * Created by fangtong on 2017/5/31.
 */
public abstract  class AliYunOSSVerticle extends AbstractVerticle implements AsyncOSSClient{

    /**
     * oss客户端
     */
    private OSSClient ossClient;
    /**
     * 异步接口映射
     */
    private static Map<Method,Method> ossClientMethodMap = new ConcurrentHashMap<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        String endpoint = config().getString("endpoint");
        String accessKeyId = config().getString("accessKeyId");
        String accessKeySecret = config().getString("accessKeySecret");
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(accessKeyId);
        Objects.requireNonNull(accessKeySecret);

        ClientConfiguration cfg = new ClientConfiguration();
        DefaultCredentialProvider cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        ossClient = new OSSClient(endpoint, cp, cfg);
        super.start(startFuture);
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        ossClient.shutdown();
        super.stop(stopFuture);
    }


    /**
     * 映射接口获取
     * @param method
     * @return
     * @throws NoSuchMethodException
     */
    static Method getOssClientMethodByVerticleMethod(Method method) throws NoSuchMethodException{
        if(!ossClientMethodMap.containsKey(method)) {
            Class[] paramterTypes = Arrays.copyOf(method.getParameterTypes(), method.getParameterTypes().length - 1);
            Method clientMethod = OSSClient.class.getMethod(method.getName(), paramterTypes);
            ossClientMethodMap.put(method, clientMethod);
        }
        return ossClientMethodMap.get(method);
    }


    /**
     * 获取 {@link AliYunOSSVerticle } 通过cglib 实现的代理
     * 异步接口必须符合 最后一个入参为 {@link Handler<AsyncResult<?>>}
     * @return
     */
    static AliYunOSSVerticle generateProxyVerticle(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(AliYunOSSVerticle.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if(method.getDeclaringClass().equals(AsyncOSSClient.class)) {
                AliYunOSSVerticle real = (AliYunOSSVerticle)obj;
                Handler<AsyncResult<Object>> handler = (Handler<AsyncResult<Object>>)args[args.length-1];
                Object[] execArgs = Arrays.copyOf(args,args.length-1);
                real.vertx.executeBlocking(f -> {
                    try {
                        Method clientMethod = getOssClientMethodByVerticleMethod(method);
                        Object invoke = clientMethod.invoke(real.ossClient, execArgs);
                        f.complete(invoke);
                    }catch (Throwable cause){
                        f.fail(cause);
                    }
                }, false, handler);
                return null;
            } else {
                return proxy.invokeSuper(obj, args);
            }
        });
        return (AliYunOSSVerticle) enhancer.create();
    }

}
