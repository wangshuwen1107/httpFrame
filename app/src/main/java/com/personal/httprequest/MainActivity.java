package com.personal.httprequest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.personal.httprequest.bean.MediaResponse;
import com.personal.lib.HttpRequest;
import com.personal.lib.callback.HttpCallback;
import com.personal.lib.exception.HttpError;
import com.personal.lib.util.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void postRequest(View view) {
        Logger.i("post Request is called ");
        HttpRequest.post()
                .url("your request url")
                .param("key","value")
                .body("key", "value")
                .build()
                .enqueue(MediaResponse.class, new HttpCallback<MediaResponse>() {
                    @Override
                    public void onSucceed(MediaResponse data) {
                        Logger.d("post request is success");
                    }

                    @Override
                    public void onFailed(HttpError error) {
                        Logger.e(error.toString());
                    }
                });
    }

    public void getRequest(View view) {
        Logger.i("get Request is called ");
        HttpRequest.get()
                .url("your request url")
                .param("key","value")
                .build()
                .enqueue(MediaResponse.class, new HttpCallback<MediaResponse>() {
                    @Override
                    public void onSucceed(MediaResponse data) {
                        Logger.d("get request is success");
                    }

                    @Override
                    public void onFailed(HttpError error) {
                        Logger.e(error.toString());
                    }
                });
    }


}
