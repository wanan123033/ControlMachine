package com.feipulai.exam.activity.ranger;

import android.content.Context;

import com.feipulai.exam.spputils.SppUtils;

public class BluetoothManager {
    private static SppUtils spp;
    public static synchronized SppUtils getSpp(Context context){
        if (spp == null){
            spp = new SppUtils(context);
        }
        return spp;
    }
}
