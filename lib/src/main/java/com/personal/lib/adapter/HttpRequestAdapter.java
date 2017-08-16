package com.personal.lib.adapter;


import com.personal.lib.HttpRequest;
import com.personal.lib.callback.HttpCallback;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;

public abstract class HttpRequestAdapter<T> {

    public abstract List<String> supportHostList();

    public abstract HashMap<String, String> commonHeaders();

    public abstract HashMap<String, String> commonParams();

    public abstract HashMap<String, String> commonBodys();

    //具体解析方法
    public abstract T parseResponseBody(ResponseBody responseBody, Type typeOfT) throws IOException;

    //同步执行response
    public abstract T onResponseSync(T data) throws IOException;

    //异步执行response
    public abstract <D> void onResponseAsync(T data, HttpCallback<D> callback) throws IOException;

    //同步解析
    public final T parseResponseBodySync(ResponseBody responseBody, Type typeOfT) {
        try {
            return onResponseSync(parseResponseBody(responseBody, typeOfT));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != responseBody) {
                responseBody.close();
            }
        }

        return null;
    }

    //异步执行
    public final <D> void parseResponseBodyAsync(ResponseBody responseBody, Type typeOfT, final HttpCallback<D> callback) {
        try {
            final T data = parseResponseBody(responseBody, typeOfT);

            HttpRequest.getInstance().getRespHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        onResponseAsync(data, callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != responseBody) {
                responseBody.close();
            }
        }
    }

}