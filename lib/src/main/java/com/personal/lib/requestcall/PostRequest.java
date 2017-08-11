package com.personal.lib.requestcall;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rokid.mobile.lib.base.util.Logger;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * This class is generated HTTP POST request.
 * <p>
 * Author: xupan.shi
 * Version: V0.1 2017/3/8
 */
public class PostRequest<T extends PostRequest> extends BaseRequest<T> {

    private static final String URL_MEDIA_TYPE_OCTET = "application/octet-stream";

    private static final String JSON_TYPE = "application/json; charset=utf-8";

    private HashMap<String, Object> bodyMap;

    private byte[] bytes;

    private String jsonStr;

    public PostRequest() {
        requestMethod = "POST";
    }

    @Override
    protected Request buildRequest() {
        return builder.post(buildRequestBody()).build();
    }

    protected RequestBody buildRequestBody() {
        // ProtoBuf application/octet-stream
        if (bytes != null) {
            return RequestBody.create(MediaType.parse(URL_MEDIA_TYPE_OCTET), bytes);
        }

        if (!TextUtils.isEmpty(jsonStr)) {
            return RequestBody.create(MediaType.parse(JSON_TYPE), jsonStr);
        }

        FormBody.Builder builder = new FormBody.Builder();
        addBody(builder);
        return builder.build();
    }

    public T body(String key, Object val) {
        if (null == this.bodyMap) {
            this.bodyMap = new HashMap<>();
        }

        this.bodyMap.put(key, val);
        return (T) this;
    }

    public T body(@NonNull HashMap<String, Object> bodys) {
        if (null == this.bodyMap) {
            this.bodyMap = new HashMap<>();
        }

        if (bodys.size() < 1) {
            Logger.w("bodys is empty do nothing");
            return (T) this;
        }

        this.bodyMap.putAll(bodys);
        return (T) this;
    }

    public T protobuf(byte[] bytes) {
        if (bytes == null) {
            Logger.w("postRequest type or bytes ir error");
            throw new RuntimeException();
        }

        this.bytes = bytes;
        return (T) this;
    }

    public T jsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
        return (T) this;
    }

    private void addBody(FormBody.Builder builder) {
        if (this.bodyMap != null && !this.bodyMap.isEmpty()) {
            for (String key : this.bodyMap.keySet()) {
                builder.add(key, String.valueOf(this.bodyMap.get(key)));
            }
        }
    }

}
