package com.feipulai.host.netUtils.netapi;

import android.content.Context;
import android.text.TextUtils;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.UserBean;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.RequestSub;

import java.util.HashMap;

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

}
