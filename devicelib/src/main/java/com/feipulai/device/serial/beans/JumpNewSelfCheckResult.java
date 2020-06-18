package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * 新增新自检返回协义 处理坏杆没返回问题
 * Created by James on 2017/10/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class JumpNewSelfCheckResult {
    //1 正常 0 异常
    int[] jumpPoleState = new int[4];

    public JumpNewSelfCheckResult(int type, byte[] buf) {
        if (type == 0) {//有线
            jumpPoleState[0] = buf[8];
            jumpPoleState[1] = buf[9];
            jumpPoleState[2] = buf[10];
            jumpPoleState[3] = buf[11];
        } else {//无线
            jumpPoleState[0] = buf[12];
            jumpPoleState[1] = buf[13];
            jumpPoleState[2] = buf[14];
            jumpPoleState[3] = buf[15];
        }

        LogUtils.normal("立定跳远自检新返回数据(解析前):" + buf.length + "---" + StringUtility.bytesToHexString(buf) + "---\n(解析后):" + toString());
    }

    public int[] getJumpPoleState() {
        return jumpPoleState;
    }

    @Override
    public String toString() {
        return "JumpNewSelfCheckResult{" +
                "jumpPoleState=" + Arrays.toString(jumpPoleState) +
                '}';
    }
}