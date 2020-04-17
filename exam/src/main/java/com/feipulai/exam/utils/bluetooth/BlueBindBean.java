package com.feipulai.exam.utils.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.inuker.bluetooth.library.search.SearchResult;

import java.io.Serializable;
import java.util.UUID;

/**
 * 蓝牙绑定
 * Created by zzs on  2020/4/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BlueBindBean implements Serializable {


    private static final long serialVersionUID = 8309877837109879047L;
    private String bluetoothMac;
    private String serverUUID;
    private String characterUUID;



    public String getServerUUID() {
        return serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public String getCharacterUUID() {
        return characterUUID;
    }

    public void setCharacterUUID(String characterUUID) {
        this.characterUUID = characterUUID;
    }

    public String getBluetoothMac() {
        return bluetoothMac;
    }

    public void setBluetoothMac(String bluetoothMac) {
        this.bluetoothMac = bluetoothMac;
    }
}
