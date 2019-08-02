package com.feipulai.exam.activity.MiddleDistanceRace.server;

import com.feipulai.exam.entity.Schedule;

/**
 * created by ww on 2019/7/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface OnResponseListener {
    void onTcpReceiveResult(Schedule schedule, String itemName);

    void OnTcpServerSuccess(boolean bool,String info);
}
