package com.feipulai.host.netUtils;

/**
 * Created by pengjf on 2018/3/27.
 */
public interface OnResultListener<T> {
    void onSuccess(T result);

    void onFault(int code, String errorMsg);
}
