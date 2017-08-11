package com.personal.lib;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.personal.lib.adapter.HttpRequestAdapter;
import com.personal.lib.callback.HttpCallback;
import com.personal.lib.exception.HttpError;
import com.personal.lib.requestcall.GetRequest;
import com.personal.lib.requestcall.PostRequest;
import com.personal.lib.requestcall.RequestCall;
import com.personal.lib.util.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HttpRequest {

    private static final int CONNECT_TIMEOUT = 15; // client请求超时时间
    private static final int READ_TIMEOUT = 30; // 读超时
    private static final int WRITE_TIMEOUT = 30; // 写超时
    private static final int PING_TIME = 5;        //ping的时间

    private static volatile HttpRequest instance;
    private Handler respHandler;

    private boolean isDebug = false;
    private OkHttpClient sslClient;
    private OkHttpClient tlsClient;

    private static Map<String, HttpRequestAdapter> httpRequestAdapterMap;

    private HttpRequest() {
        buildSSLClient();

        respHandler = new Handler(Looper.getMainLooper());
        httpRequestAdapterMap = new HashMap<>();
    }

    public static HttpRequest getInstance() {
        if (null == instance) {
            synchronized (HttpRequest.class) {
                if (null == instance) {
                    instance = new HttpRequest();
                }
            }
        }
        return instance;
    }

    public static void initialize() {
        getInstance();
    }

    public static void openDebug() {
        getInstance().isDebug = true;
    }

    public static boolean isDebug() {
        return getInstance().isDebug;
    }

    public static GetRequest get() {
        return new GetRequest();
    }

    public static PostRequest<PostRequest<PostRequest>> post() {
        return new PostRequest<>();
    }

    public OkHttpClient getClient() {
        return sslClient;
    }

    public Handler getRespHandler() {
        return respHandler;
    }

    //在application创建的时候初始化adapter
    public static void registerAdapter(@NonNull HttpRequestAdapter adapter) {
        if (null == httpRequestAdapterMap) {
            httpRequestAdapterMap = new HashMap<>();
        }

        if (null == adapter.supportHostList() || adapter.supportHostList().isEmpty()) {
            Logger.w("The adapter support host is empty, so don't register.");
            return;
        }

        for (Object key : adapter.supportHostList()) {
            Logger.d("Register the adapter by key: " + key + " ; adapter: " + adapter.toString());
            httpRequestAdapterMap.put(String.valueOf(key), adapter);
        }
    }

    public HttpRequestAdapter getHttpRequestAdapter(@NonNull String key) {
        if (!httpRequestAdapterMap.containsKey(key)) {
            Logger.w("This key: " + key + " is not have a adapter.");
            return null;
        }

        return httpRequestAdapterMap.get(key);
    }

    /**
     * 将requestCall加入到okHttp请求队列
     */
    public <T> void enqueue(final RequestCall requestCall, final Type typeOfT, final HttpCallback<T> callback) {
        requestCall.getCall().enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailResult(new HttpError(e.getMessage()), callback);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (call.isCanceled()) {
                    sendFailResult(new HttpError(response.code(), "The request has been cancelled."), callback);
                    return;
                }

                if (!response.isSuccessful()) {
                    String errorMsg = "The request code error";
                    if (null != response.body()) {
                        String tmp = response.body().string();
                        if (!TextUtils.isEmpty(tmp)) {
                            errorMsg = tmp;
                        }
                    }

                    sendFailResult(new HttpError(response.code(), errorMsg), callback);
                    return;
                }

                final String adapterKey = requestCall.getAdapterKey();
                Logger.d("This AdapterKey: " + adapterKey);

                if (TextUtils.isEmpty(adapterKey) || !httpRequestAdapterMap.containsKey(adapterKey)) {
                    Logger.d("Http Request can't have this adapter.");

                    String responseBodyStr = response.body().string();
                    response.body().close();

                    if (TextUtils.isEmpty(responseBodyStr)) {
                        sendFailResult(new HttpError(-1, "The response body is null"), callback);
                        return;
                    }

                    sendDefaultSuccessResult(responseBodyStr, typeOfT, callback);
                } else {
                    httpRequestAdapterMap.get(adapterKey).parseResponseBodyAsync(response.body(), typeOfT, callback);
                }

            }
        });
    }

    public <T> T execute(final RequestCall requestCall, final Type typeOfT) {
        Response response;
        ResponseBody responseBody = null;

        try {
            response = requestCall.getCall().execute();
            responseBody = response.body();
            if (TextUtils.isEmpty(responseBody.toString())) {
                return null;
            }

            String adapterKey = requestCall.getAdapterKey();
            if (TextUtils.isEmpty(adapterKey) || !httpRequestAdapterMap.containsKey(adapterKey)) {
                if (null == typeOfT) {
                    return (T) responseBody.toString();
                } else {
                    return new Gson().fromJson(responseBody.toString(), typeOfT);
                }
            } else {
                return (T) httpRequestAdapterMap.get(adapterKey).parseResponseBodySync(response.body(), typeOfT);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != responseBody) {
                responseBody.close();
            }
        }

        return null;
    }

    private <T> void sendFailResult(final HttpError error, final HttpCallback<T> callback) {
        Logger.d("[HTTP] Fail: ", error.getMsg());

        if (callback == null) {
            Logger.w("[HTTP] ", "The HttpCallBack is null !!!!");
            return;
        }

        respHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailed(error);
            }
        });
    }

    private void sendDefaultSuccessResult(@NonNull final String bodyStr, final Type typeOfT,
                                          final HttpCallback callback) {
        // debug 模式下 json 打印
        Logger.d("[HTTP] Success: ", bodyStr);

        if (callback == null) {
            Logger.w("[HTTP] ", "The HttpCallBack is null !!!!");
            return;
        }

        respHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null == typeOfT || String.class.equals(typeOfT)) {
                    callback.onSucceed(bodyStr);
                } else {
                    callback.onSucceed(new Gson().fromJson(bodyStr, typeOfT));
                }
            }
        });
    }

    /**
     * 取消指定tag的请求
     *
     * @param requestTag 请求的tag
     */
    public void cancel(String requestTag) {
        if (TextUtils.isEmpty(requestTag)) {
            return;
        }
        for (Call call : sslClient.dispatcher().queuedCalls()) {
            if (requestTag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : sslClient.dispatcher().runningCalls()) {
            if (requestTag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        Logger.d("Cancel the Request of the " + requestTag);
    }

    /**
     * 取消所有请求,注意这里会将dispatcher的所有请求全部取消
     */
    public void cancelAll() {
        Logger.d("All request are Canceled!!!");
        sslClient.dispatcher().cancelAll();
    }

    private void buildSSLClient() {
        HttpsCertUtils.SSLParams sslParams = HttpsCertUtils.getSSLParams(HttpsCertUtils.Protocol.SSL, null, null, null);

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//                .dns(HttpDNS.getInstance())
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);

        sslClient = builder.build();

    }
}

