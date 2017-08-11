package com.personal.lib.callback;


import com.personal.lib.exception.HttpError;

public abstract class HttpCallback<D> {

    public abstract void onSucceed(D data);

    public abstract void onFailed(HttpError error);

}
