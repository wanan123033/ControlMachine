package com.feipulai.device;

import com.feipulai.device.ic.utils.ItemDefault;

/**
 * Created by zzs on  2019/9/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AdaptiveConfig {
    public static final int DEFAULT = 0;
    public static final int KEY_B = 4;
    public static final int LIN_NAN_SHI_FAN = 1;
    public static int ADAPTIVE_TYPE = DEFAULT;
    public static int KEY_MODE = DEFAULT;
    public static char[] IC_KEY;
    public static char[] KEY_DEFAULT = {0xff, 0xff, 0xff, 0xff, 0xff, 0xff};

    public static void initIC(int type, int key_mode, char[] key) {
        ADAPTIVE_TYPE = type;
        KEY_MODE = key_mode;
        ItemDefault.keyA = key;
        IC_KEY = key;
    }
}
