package com.feipulai.host.activity.jump_rope.check;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.config.TestConfigs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2019/2/12 0012.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class CheckUtils {

    public static List<StuDevicePair> newPairs(int size) {
        List<StuDevicePair> pairs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StuDevicePair pair = new StuDevicePair();
            BaseDeviceState state;
            switch (TestConfigs.sCurrentItem.getMachineCode()) {

                case ItemDefault.CODE_TS:
                    state = new JumpDeviceState();
                    break;

                case ItemDefault.CODE_YWQZ:
                case ItemDefault.CODE_YTXS:
                case ItemDefault.CODE_FHL:
                case ItemDefault.CODE_WLJ:
                case ItemDefault.CODE_HWSXQ:
                case ItemDefault.CODE_LDTY:
                    state = new BaseDeviceState();
                    break;
                default:
                    throw new IllegalArgumentException("machine code not supported");

            }
            state.setState(BaseDeviceState.STATE_DISCONNECT);
            state.setDeviceId(i + 1);
            pair.setBaseDevice(state);
            pairs.add(pair);
        }
        return pairs;
    }

    public static void stopUse(List<StuDevicePair> pairs, int position) {
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_STOP_USE);
    }

    public static void resumeUse(List<StuDevicePair> pairs, int position) {
        pairs.get(position).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
    }

}
