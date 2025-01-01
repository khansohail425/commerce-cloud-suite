package com.commerce.cloud.platform.outbound.wso2.client.exception;

public class WSO2ClientException extends RuntimeException {

    public WSO2ClientException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public WSO2ClientException(Throwable throwable) {
        super(throwable);
    }

    public WSO2ClientException(String message) {
        super(message);
    }
}
