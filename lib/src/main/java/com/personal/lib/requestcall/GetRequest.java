package com.personal.lib.requestcall;

import okhttp3.Request;

/**
 *
 *
 */
public class GetRequest extends BaseRequest<GetRequest> {

    public GetRequest() {
        requestMethod = "GET";
    }

    @Override
    protected Request buildRequest() {
        return builder.get().build();
    }

}
