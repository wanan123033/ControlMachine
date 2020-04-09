package com.feipulai.device.spputils;

import java.util.ArrayList;
import java.util.List;

public abstract class OnDataReceivedListener implements SppUtils.OnDataReceivedListener{
    List<Byte> byteData = new ArrayList<>();
    @Override
    public void onDataReceived(byte[] data, String message) {
        byteData.add(data[0]);
        if (byteData.size() > 2){
            if (byteData.get(byteData.size() - 1) == 0x0a && byteData.get(byteData.size() - 2) == 0x0d){
                byte[] datas = new byte[byteData.size()];
                for (int i = 0 ; i < byteData.size() ; i++){
                    datas[i] = byteData.get(i);
                }
                onResult(datas);
                byteData.clear();
            }
        }
    }

    protected abstract void onResult(byte[] datas);
}
