package com.feipulai.host.netUtils;

import android.app.ProgressDialog;
import android.content.Context;

import com.orhanobut.logger.Logger;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

/**
 * Created by pengjf on 2018/3/27.
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 */

public class RequestSub<T> extends DisposableObserver<HttpResult<T>>
        implements ProgressCancelListener {
    /**
     * 是否需要显示默认Loading
     */
    private boolean showProgress = true;
    private OnResultListener mOnResultListener;
    private Context context;
    private ProgressDialog progressDialog;

    /**
     * @param mOnResultListener 成功回调监听
     */
    public RequestSub(OnResultListener mOnResultListener) {
        this.mOnResultListener = mOnResultListener;
    }

    /**
     * @param mOnResultListener 成功回调监听
     * @param context           上下文
     */
    public RequestSub(OnResultListener mOnResultListener, Context context) {
        this.mOnResultListener = mOnResultListener;
        if (context == null) {
            return;
        }
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在加载中，请稍等......");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * @param mOnResultListener 成功回调监听
     * @param context           上下文
     * @param showProgress      是否需要显示默认Loading
     */
    public RequestSub(OnResultListener mOnResultListener, Context context, boolean showProgress) {
        this.mOnResultListener = mOnResultListener;
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在连接中...");
        this.showProgress = showProgress;
    }


    private void showProgressDialog() {
        if (showProgress && null != progressDialog) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (showProgress && null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onStart() {
        showProgressDialog();
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onComplete() {
        dismissProgressDialog();
        progressDialog = null;
    }

    @Override
    public void onNext(HttpResult<T> result) {
        switch (result.getState()) {
        
            case 0:
                mOnResultListener.onSuccess(result.getBody());
                break;
        
            case -1:
                mOnResultListener.onFault("设备未激活，请联系管理激活设备");
                break;
            case -2:
                mOnResultListener.onFault("设备未绑定,请手动绑定设备");
                break;
            case -6:
                mOnResultListener.onFault("成功获取该项目学生信息");
                break;
            default:
                mOnResultListener.onFault(result.getMsg() + "\n错误代码:" + result.getState());
                break;
        }
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     */
    @Override
    public void onError(Throwable e) {
        try {
            String msg = "";
            if (e instanceof SocketTimeoutException) {//请求超时
                msg = "网络连接超时,请检查网络";
            } else if (e instanceof ConnectException) {//网络连接超时
                msg = "网络连接错误,请检查网络";
            } else if (e instanceof SSLHandshakeException) {//安全证书异常
                msg = "安全证书异常";
            } else if (e instanceof HttpException) {//请求的地址不存在
                int code = ((HttpException) e).code();
                msg = "请求失败";
            } else if (e instanceof UnknownHostException) {//域名解析失败
                msg =  "域名解析失败,请检查服务器地址设置";
            }
            mOnResultListener.onFault(msg + "\t" + e.getMessage());
        } finally {
            Logger.e("error:" + e.getMessage());
            dismissProgressDialog();
            progressDialog = null;
        }
    }

    /**
     * 当result等于0回调给调用者，否则自动显示错误信息，若错误信息为401跳转登录页面。
     * ResponseBody  body = response.body();//获取响应体
     * InputStream inputStream = body.byteStream();//获取输入流
     * byte[] bytes = body.bytes();//获取字节数组
     * String str = body.string();//获取字符串数据
     */
//    @Override
//    public void onNext(ResponseBody body) {
//        try {
//            final String result = CompressUtils.decompress(body.byteStream());
//            Log.e("body", result);
//            JSONObject jsonObject = new JSONObject(result);
//            int resultCode = jsonObject.getInt("ErrorCode");
//            if (resultCode == 1) {
//                mOnResultListener.onSuccess(result);
//            } else {
//                String errorMsg = jsonObject.getString("ErrorMessage");
//                mOnResultListener.onFault(errorMsg);
//                Log.e("RequestSub", "errorMsg: " + errorMsg);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    @Override
    public void onCancelProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (!this.isDisposed()) {
            this.dispose();
        }
    }
    
}
