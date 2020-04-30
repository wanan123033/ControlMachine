package com.feipulai.host.activity.ranger.bluetooth;

import android.content.Context;

import com.feipulai.device.spputils.SppUtils;


public class BluetoothManager {
    public static SppUtils spp;
    public static synchronized SppUtils getSpp(Context context){
        if (spp == null){
            spp = new SppUtils(context);
        }
        return spp;
    }
}
