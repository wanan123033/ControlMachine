package com.feipulai.host.netUtils.netapi;

/**
 * Created by zzs on  2019/12/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface OnRequestEndListener {
    void onSuccess(int bizType);

    void onFault(int bizType);
}
