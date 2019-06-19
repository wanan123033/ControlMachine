package com.feipulai.device.udp.parse;

import android.util.Log;

import com.feipulai.device.udp.result.UDPResult;

import java.util.Arrays;

/**
 * created by ww on 2019/6/10.
 */
public class MiddleRaceParser extends UDPParser {
    @Override
    public UDPResult parse(byte[] data) {
        Log.i("wong", "MiddleRaceParser---" + Arrays.toString(data));
        return null;
    }
}
