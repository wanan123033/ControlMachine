package com.feipulai.host.netUtils;

/**
 * 服务器返回的异常
 */
public class ResponseAnalysisException extends RuntimeException {
    public ResponseAnalysisException(int errorCode, String cause) {
//        super("服务器响应失败，错误码："+errorCode+"，错误原因"+cause, new Throwable("Server error"));
        super(cause, new Throwable(errorCode + ""));
    }
}
