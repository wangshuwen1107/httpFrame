package com.personal.lib.exception;

/**
 * 错误信息的实体
 */
public class HttpError extends RuntimeException {

    private int code;

    public HttpError(int code) {
        this.code = code;
    }

    public HttpError(String message) {
        super(message);
    }

    public HttpError(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return super.getMessage();
    }

    @Override
    public String toString() {
        return "HttpError{" +
                "code=" + code +
                " msg=" + super.getMessage() +
                '}';
    }

}
