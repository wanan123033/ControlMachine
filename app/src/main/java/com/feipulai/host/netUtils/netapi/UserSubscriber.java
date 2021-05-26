package com.feipulai.host.netUtils.netapi;

import android.content.Context;
import android.text.TextUtils;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.ActivateBean;
import com.feipulai.host.bean.UserBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.RequestSub;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Created by pengjf on 2018/10/9.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class UserSubscriber {


    public void takeBind(int hostId, Context context) {
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("deviceNo", CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("MachineNumber", hostId + "");
        parameData.put("MachineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().bind("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("1000", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserBean>(new OnResultListener<UserBean>() {

            @Override
            public void onSuccess(UserBean result) {
                ToastUtils.showShort("设备绑定成功");
                MyApplication.TOKEN = result.getToken();
                if (!TextUtils.isEmpty(result.getExamName())) {
                    SettingHelper.getSystemSetting().setTestName(result.getExamName());
                }
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, result.getToken());
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
            }
        }, context));


    }

    /**
     * 激活
     *
     * @param currentRunTime
     * @param listener
     */
    public void activate(long currentRunTime, OnResultListener listener) {
        //减少连接时长
        HttpManager.DEFAULT_CONNECT_TIMEOUT = 5;
        HttpManager.DEFAULT_READ_TIMEOUT = 5;
        HttpManager.DEFAULT_WRITE_TIMEOUT = 5;
        HttpManager.resetManager();
        Map<String, String> parameData = new HashMap<>();
        parameData.put("deviceIdentify", CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("currentRunTime", currentRunTime + "");
        parameData.put("softwareCode", MyApplication.SOFTWAREUUID);
        parameData.put("softwareName", CommonUtils.getAppName(MyApplication.getInstance()));

        Observable<HttpResult<ActivateBean>> observable = HttpManager.getInstance().getHttpApi().activate(CommonUtils.encryptQuery("300021100", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<ActivateBean>(listener));
    }

    /**
     * 日志上传
     *
     * @param crashMsg
     */
    public void uploadLog(String crashMsg) {
        Map<String, String> parameData = new HashMap<>();
        parameData.put("logLevel", "ERROR");
        parameData.put("errorText", crashMsg);
        parameData.put("deviceIdentify", CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("operateName", "错误日志");
        parameData.put("softwareCode", MyApplication.SOFTWAREUUID);
        parameData.put("softwareName", CommonUtils.getAppName(MyApplication.getInstance()));
        Observable<HttpResult<String>> observable = HttpManager.getInstance().getHttpApi().uploadLog(CommonUtils.encryptQuery("300021300", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<String>(new OnResultListener() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("zzs==============>日志上传成功");
            }

            @Override
            public void onFault(int code, String errorMsg) {
                System.out.println("zzs==============>日志上传失败");
            }


        }));
    }
}
