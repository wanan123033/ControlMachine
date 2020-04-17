package com.feipulai.exam.utils.bluetooth;

import com.feipulai.exam.MyApplication;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.List;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/**
 * Created by zzs on  2020/4/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ClientManager {

    private static BluetoothClient mClient;

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MyApplication.getInstance());
                }
            }
        }
        return mClient;
    }


    /**
     * 连接
     */
    public static void connectDevice(String mac, BleConnectResponse response) {

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        ClientManager.getClient().connect(mac, options, response);
    }

    /**
     * 获取UUID
     *
     * @param profile
     */
    public static void getGattProfile(BleGattProfile profile) {
        List<BleGattService> services = profile.getServices();
        for (BleGattService service : services) {
            if (BlueToothConfig.SERVICE_UUID.equalsIgnoreCase(service.getUUID().toString().toUpperCase())) {
                List<BleGattCharacter> characters = service.getCharacters();
                BlueToothHelper.getBlueBind().setServerUUID(service.getUUID().toString().toUpperCase());
                for (BleGattCharacter character : characters) {
                    if (BlueToothConfig.READ_NOTIFY_UUID.equalsIgnoreCase(character.getUuid().toString().toUpperCase())) {
                        BlueToothHelper.getBlueBind().setCharacterUUID(character.getUuid().toString().toUpperCase());
                        BlueToothHelper.updateBlueBindCache(BlueToothHelper.getBlueBind());
                        return;
                    }
                }
            }

        }
    }

}
