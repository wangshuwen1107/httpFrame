package com.personal.httprequest.application;

import android.app.Application;

import com.personal.httprequest.MediaRequestAdapter;
import com.personal.lib.HttpRequest;

/**
 * Created by wangshuwen on 2017/8/11.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initRequestAdapter();
    }

    //注册网络适配器
    private void initRequestAdapter() {
        HttpRequest.registerAdapter(new MediaRequestAdapter());
    }
}
