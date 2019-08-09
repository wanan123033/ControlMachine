package com.feipulai.host.netUtils.netapi;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.RequestSub;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class UserSubscriber {

    public void bind(Object object, DisposableObserver subscriber){
        Observable<HttpResult<String>> observable = HttpManager.getInstance().getHttpApi().bind(CommonUtils.query(object));
        HttpManager.getInstance().toSubscribe(observable, subscriber);
    }

    public void takeBind(int hostId, Context context){
        HashMap<String,String> parameData = new HashMap<>();
        parameData.put("deviceNo",CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("MachineNumber",hostId + "");
        parameData.put("MachineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
        bind(parameData,new RequestSub(new OnResultListener<String>(){
            @Override
            public void onSuccess(String result){
                ToastUtils.showShort("设备绑定成功");
                MyApplication.TOKEN = result;
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.TOKEN,result);
            }

            @Override
            public void onFault(String errorMsg){
                ToastUtils.showShort(errorMsg);
            }
        },context));
    }
    
}
