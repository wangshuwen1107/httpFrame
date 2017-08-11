package com.personal.lib.requestcall;

import android.support.annotation.NonNull;

import com.personal.lib.HttpRequest;
import com.personal.lib.callback.HttpCallback;

import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 此类用于将BaseRequest-->okHttp3.request-->call-->httpRequest.enqueue(call)
 */
public class RequestCall {

    private BaseRequest baseRequest;
    private Request request;
    private Call call;

    public RequestCall(@NonNull BaseRequest request) {
        this.baseRequest = request;
        buildCall();
    }

    public Request getRequest() {
        return request;
    }

    public Call getCall() {
        return call;
    }

    public String getAdapterKey() {
        return baseRequest.adapterKey;
    }

    public <T> void enqueue(Type typeOfT, HttpCallback<T> callback) {
        HttpRequest.getInstance().enqueue(this, typeOfT, callback);
    }

    public <T> void enqueue(HttpCallback<T> callback) {
        HttpRequest.getInstance().enqueue(this, null, callback);
    }

    public <T> T execute(Type typeOfT) {
        return HttpRequest.getInstance().execute(this, typeOfT);
    }

    public <T> T execute() {
        return execute(null);
    }

    private void buildCall() {
        request = baseRequest.buildRequest();
        call = HttpRequest.getInstance().getClient().newCall(request);
    }

}
