package com.personal.httprequest;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.personal.httprequest.bean.MediaResponse;
import com.personal.lib.adapter.HttpRequestAdapter;
import com.personal.lib.callback.HttpCallback;
import com.personal.lib.exception.HttpError;
import com.personal.lib.util.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * Created by wangshuwen on 2017/8/11.
 */

public class MediaRequestAdapter extends HttpRequestAdapter<MediaResponse> {

    @Override
    public List<String> supportHostList() {
        List<String> hostList = new ArrayList<>();
        hostList.add("rokid.oss-cn-qingdao.aliyuncs.com");
        return hostList;
    }

    @Override
    public HashMap<String, String> commonHeaders() {
        return null;
    }

    @Override
    public HashMap<String, String> commonParams() {
        return null;
    }

    @Override
    public HashMap<String, String> commonBodys() {
        return null;
    }


    @Override
    public MediaResponse parseResponseBody(ResponseBody responseBody, Type typeOfT) throws IOException {
        String responseStr = responseBody.string();
        if (TextUtils.isEmpty(responseStr)) {
            Logger.e("ResponseBody  is null");
            return null;
        }
        Gson gson = new Gson();
        Type t = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(
                null,
                MediaResponse.class,
                typeOfT);
        return gson.fromJson(responseStr, t);
    }

    @Override
    public MediaResponse onResponseSync(MediaResponse data) throws IOException {
        return data;
    }

    @Override
    public <D> void onResponseAsync(MediaResponse data, HttpCallback<D> callback) throws IOException {
        if (null == data) {
            callback.onFailed(new HttpError(-1000, "data is null"));
        }
        if (!data.isSuccess()) {
            callback.onFailed(new HttpError(-1001, "response is not success"));
            return;
        }
        callback.onSucceed((D)data.getData());
    }

}
