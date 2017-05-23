package org.ft.test.vertx.future;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fangtong on 2017/5/23.
 */
public class FutureComposeTestVerticle extends AbstractVerticle{

    public static void main( String[] args )
    {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FutureComposeTestVerticle());
    }

    @Override
    public void start() throws Exception {

        String starttime = System.currentTimeMillis() / 1000 + "";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("appversion", "1");
        map.put("appid", "11000002");
        map.put("order", "s" + starttime + "t" + System.currentTimeMillis());
        map.put("sdkversion", "10");
        map.put("paychannel", "1100");
        map.put("partner", "1100");
        map.put("starttime", starttime);
        map.put("imei", "xxxxx");
        map.put("price", "1000");
        map.put("payType", "ONCE");
        map.put("imsi","xxxxx");
        map.put("chargeid", "1");
        map.put("phone", "xxxx");

        Future.<String>future(f->{
            //1 构建异步查询参数及签名 work线程处理
            vertx.executeBlocking(md5Future->{
                StringBuilder builder1 = new StringBuilder();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    builder1.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                builder1.deleteCharAt(builder1.length() - 1);
                String sign = md5(md5(builder1.toString()) + "UY699OPCCZTR");
                map.put("md5",sign);

                builder1 = new StringBuilder();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    builder1.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                builder1.deleteCharAt(builder1.length() - 1);
                md5Future.complete(builder1.toString());
            },f.completer());
        }).compose(query->{
            //2 进行http 异步查询 work线程处理
            Future<String> f1 = Future.future();
            vertx.executeBlocking(requestFuture->{
                vertx.createHttpClient().get(8888,"hzhongmeng.com","/creq?"+query,result->{
                    if(result.statusCode() != 200){
                        requestFuture.fail("status code != 200");
                    }
                    result.bodyHandler(buffer->{
                        requestFuture.complete(buffer.toString());
                    }) ;
                }).exceptionHandler(error->{
                    requestFuture.fail(error);
                }).end();
            },f1.completer());
            return f1;
        }).compose(response->{
            //3 同步解码 同一线程处理
            return Future.succeededFuture(conv(response));
        }).setHandler(result->{
            //4 输出结果
            if(result.succeeded()){
                System.out.println(result.result());
            }else{
                System.out.println(result.cause().toString());
            }
        });
    }

    private static byte[] m = new byte[65];
    private static byte[] n = new byte[256];


    public static String conv(String paramString)
    {
        if (paramString == null) {
            return "";
        }
        try
        {
            return new String(conv(paramString.getBytes("utf-8")), "utf-8");
        }
        catch (Exception e) {}
        return paramString;
    }

    private static byte[] conv(byte[] params)
    {
        if (params == null) {
            return null;
        }
        int j = params.length / 4;
        if (params.length % 4 != 0) {
            return params;
        }
        if (params.length == 0) {
            return new byte[0];
        }
        byte[] arrayOfByte1 = b(new byte[] { params[(j - 1 << 2)], params[((j - 1 << 2) + 1)], params[((j - 1 << 2) + 2)], params[((j - 1 << 2) + 3)] });
        byte[] arrayOfByte2 = new byte[(j - 1) * 3 + arrayOfByte1.length];
        arrayOfByte1 = c(arrayOfByte1);
        for (int i = 0; i < arrayOfByte1.length; i++) {
            arrayOfByte2[((j - 1) * 3 + i)] = arrayOfByte1[i];
        }
        for (int i = 0; i < j - 1; i++)
        {
            if ((arrayOfByte1 = b(new byte[] { params[(i << 2)], params[((i << 2) + 1)], params[((i << 2) + 2)], params[((i << 2) + 3)] })).length != 3) {
                return params;
            }
            arrayOfByte1 = c(arrayOfByte1);
            for (int k = 0; k < arrayOfByte1.length; k++) {
                arrayOfByte2[(i * 3 + k)] = arrayOfByte1[k];
            }
        }
        return arrayOfByte2;
    }


    private static byte[] b(byte[] paramArrayOfByte)
    {
        int i = convByN(paramArrayOfByte[0]);
        int j = convByN(paramArrayOfByte[1]);
        int k = convByN(paramArrayOfByte[2]);
        int i1 = convByN(paramArrayOfByte[3]);
        i = (byte)((i & 0x3F) << 2 | (j & 0x30) >>> 4);
        j = (byte)((j & 0xF) << 4 | (k & 0x3C) >>> 2);
        k = (byte)((k & 0x3) << 6 | i1 & 0x3F);
        if ((paramArrayOfByte[2] == 61) && (paramArrayOfByte[3] == 61)) {
            return new byte[] { (byte)i };
        }
        if (paramArrayOfByte[3] == 61) {
            return new byte[] { (byte)i, (byte)j };
        }
        return new byte[] { (byte)i, (byte)j, (byte)k };
    }

    private static byte[] c(byte[] paramArrayOfByte)
    {
        byte j;
        byte i;
        byte k;
        if (paramArrayOfByte.length == 3)
        {
            j = paramArrayOfByte[2];
            i = paramArrayOfByte[1];
            byte param = (byte)( paramArrayOfByte[0] ^ 0xBC);
            i = (byte)(i ^ 0x5F);
            j = (byte)(j ^ 0x64);
            k = (byte)((param & 0x8) << 4 | (j & 0xF) << 3 | (i & 0xE0) >>> 5);
            int i1 = (byte)((j & 0x30) << 2 | (i & 0x3) << 4 | (param & 0xF0) >>> 4);
            param = (byte)((i & 0x1C) << 3 | (j & 0xC0) >>> 3 | param & 0x7);
            return new byte[] { k, (byte)i1, param };
        }
        if (paramArrayOfByte.length == 2)
        {
            i = paramArrayOfByte[1];
            byte param = (byte)(paramArrayOfByte[0] ^ 0xBF);
            i = (byte)(i ^ 0x5A);
            j = (byte)((param & 0xF) << 4 | (i & 0xF0) >>> 4);
            k = (byte)((i & 0xF) << 4 | (param & 0xF0) >>> 4);
            return new byte[] { j, k };
        }
        if (paramArrayOfByte.length == 1)
        {
            byte param = paramArrayOfByte[0];
            return new byte[] { (byte)(param ^ 0x5B) };
        }
        return null;
    }

    private static byte convByN(int paramInt)
    {
        if ((paramInt >= 0) && (paramInt < n.length)) {
            return n[paramInt];
        }
        return 0;
    }

    static
    {
        for (int i = 0; i < 10; i++)
        {
            m[i] = ((byte)(i + 48));
            n[(i + 48)] = ((byte)i);
        }
        for (int i = 10; i < 36; i++)
        {
            m[i] = ((byte)(97 + (i - 10)));
            n[(97 + (i - 10))] = ((byte)i);
        }
        for (int i = 36; i < 52; i++)
        {
            m[i] = ((byte)(65 + (i - 36)));
            n[(65 + (i - 36))] = ((byte)i);
        }
        m[52] = 42;
        n[42] = 52;
        m[53] = 35;
        n[35] = 53;
        m[54] = 40;
        n[40] = 54;
        m[55] = 41;
        n[41] = 55;
        m[56] = 38;
        n[38] = 56;
        m[57] = 94;
        n[94] = 57;
        m[58] = 37;
        n[37] = 58;
        m[59] = 36;
        n[36] = 59;
        m[60] = 43;
        n[43] = 60;
        m[61] = 64;
        n[64] = 61;
        m[62] = 33;
        n[33] = 62;
        m[63] = 126;
        n[126] = 63;
        m[64] = 61;
        n[61] = 0;
    }

    public static String md5(String paramString)
    {
        try
        {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(paramString.getBytes("UTF-8"));

            StringBuilder localStringBuilder = new StringBuilder(bytes.length << 1);
            int i = bytes.length;
            for (int j = 0; j < i; j++)
            {
                int k;
                if (((k = bytes[j]) & 0xFF) < 16) {
                    localStringBuilder.append("0");
                }
                localStringBuilder.append(Integer.toHexString(k & 0xFF));
            }
            return localStringBuilder.toString();
        }
        catch (Exception localException)
        {
            return null;
        }
    }
}
