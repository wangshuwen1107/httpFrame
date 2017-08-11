package com.personal.lib.requestcall;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.personal.lib.HttpRequest;
import com.personal.lib.adapter.HttpRequestAdapter;
import com.personal.lib.exception.HttpRequestException;
import com.personal.lib.util.Logger;
import com.personal.lib.util.MapUtils;

import java.util.HashMap;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.Request;


/**
 * 基类去构建request信息
 */
public abstract class BaseRequest<T extends BaseRequest> {

    protected String requestMethod = "";
    protected String requestTag = "";
    protected String adapterKey = "";

    protected String url;
    protected HashMap<String, String> headerMap;
    protected HashMap<String, String> paramMap;

    protected Request.Builder builder = new Request.Builder();

    public T url(@NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            throw new HttpRequestException("Url can't be null.");
        }

        this.url = url;

        // 设置默认参数
        adapterKey = Uri.parse(url).getHost();
        addCommonConfig();

        return (T) this;
    }

    public T path(@NonNull String key, @NonNull String value) {
        if (TextUtils.isEmpty(url)) {
            throw new HttpRequestException("Url can't be null.");
        }

        if (!url.contains(key)) {
            Logger.w("This url does't contain " + key);
            return (T) this;
        }

        url = url.replace(key, value);
        return (T) this;
    }

    public T header(@NonNull HashMap<String, String> headers) {
        if (this.headerMap == null) {
            this.headerMap = new HashMap<>();
        }

        this.headerMap.putAll(headers);
        return (T) this;
    }

    public T header(@NonNull String key, @NonNull String value) {
        if (this.headerMap == null) {
            this.headerMap = new HashMap<>();
        }

        headerMap.put(key, value);
        return (T) this;
    }

    public T param(@NonNull HashMap<String, String> params) {
        if (null == this.paramMap) {
            this.paramMap = new HashMap<>();
        }

        this.paramMap.putAll(params);
        return (T) this;
    }

    public T param(@NonNull String key, @NonNull String val) {
        if (null == this.paramMap) {
            this.paramMap = new HashMap<>();
        }

        this.paramMap.put(key, val);
        return (T) this;
    }

    public T requestTag(@NonNull String requestTag) {
        this.requestTag = requestTag;
        return (T) this;
    }

    protected abstract Request buildRequest();

    /**
     * 加所有信息加入Request.Builder后 构建一个RequestCall
     */
    public RequestCall build() {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("url can't be empty!!!");
        }

        printLog();
        if (this.paramMap != null && !this.paramMap.isEmpty()) {
            url = appendParams(url, this.paramMap);
            Logger.d("applyUrl: " + url);
        }

        builder.url(url).tag(requestTag);
        appendHeaders();
        return new RequestCall(this);
    }

    private void addCommonConfig() {
        this.header("Host", adapterKey);
        this.header("X-Online-Host", adapterKey);

        // 添加 Adapter 中的配置
        HttpRequestAdapter adapter = HttpRequest.getInstance().getHttpRequestAdapter(adapterKey);
        if (null == adapter) {
            Logger.w("This request can't have a adapter. so use default.");
            return;
        }

        Logger.d("This key: ", adapterKey, " have a adapter: ", adapter.getClass().getSimpleName());

        HashMap<String, String> commonHeaders = adapter.commonHeaders();
        if (!MapUtils.isEmpty(commonHeaders)) {
            Logger.d("This adapter: ", adapter.getClass().getSimpleName(), " have a commonHeaders: ", commonHeaders.toString());

            this.header(commonHeaders);
        }

        HashMap<String, String> commonParams = adapter.commonParams();
        if (!MapUtils.isEmpty(commonParams)) {
            Logger.d("This adapter: ", adapter.getClass().getSimpleName(), " have a commonParams: ", commonParams.toString());

            this.param(commonParams);
        }

        HashMap<String, String> commonBodys = adapter.commonBodys();
        if (!MapUtils.isEmpty(commonBodys)
                && (this instanceof PostRequest || this.getClass().isAssignableFrom(PostRequest.class))) {

            Logger.d("This adapter: ", adapter.getClass().getSimpleName(), " have a commonBodys: ", commonBodys.toString());

            ((PostRequest) this).body(commonBodys);
        }
    }

    /**
     * 添加header信息加入到requestBuild里面
     */
    private void appendHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (this.headerMap == null || this.headerMap.isEmpty()) {
            return;
        }

        for (String key : this.headerMap.keySet()) {
            headerBuilder.add(key, this.headerMap.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    /**
     * @param url    请求地址
     * @param params 请求后面的参数
     *
     * @return 拼接后的url
     */
    private String appendParams(@NonNull String url, HashMap<String, String> params) {
        if (TextUtils.isEmpty(url) || params == null || params.isEmpty()) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            builder.appendQueryParameter(key, params.get(key));
        }
        return builder.build().toString();
    }

    /**
     * When debugging, Print request url, headers and parameters.
     */
    private void printLog() {
        if (!HttpRequest.isDebug()) {
            return;
        }

        Logger.i("[" + requestMethod + "] Tag: " + requestTag + " ; url: " + url);
        if (this.headerMap != null && this.headerMap.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (String key : this.headerMap.keySet()) {
                strBuilder.append(key);
                strBuilder.append(" : ");
                strBuilder.append(this.headerMap.get(key));
                strBuilder.append(", ");
            }
            Logger.i("headers: " + strBuilder.toString());
        }

        if (this.paramMap != null && this.paramMap.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (String key : this.paramMap.keySet()) {
                strBuilder.append(key);
                strBuilder.append(" : ");
                strBuilder.append(this.paramMap.get(key));
                strBuilder.append(", ");
            }
            Logger.i("params: " + strBuilder.toString());
        }
    }


}
