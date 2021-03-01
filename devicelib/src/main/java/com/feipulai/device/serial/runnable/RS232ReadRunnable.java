package com.feipulai.device.serial.runnable;

import android.os.Message;
import android.util.Log;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.device.serial.SerialPorter;
import com.feipulai.device.serial.beans.ConverterVersion;
import com.feipulai.device.serial.beans.RS232Result;
import com.feipulai.device.serial.beans.Radio868Result;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.parser.DistanceParser;
import com.feipulai.device.serial.parser.GPSParser;
import com.feipulai.device.serial.parser.HWParser;
import com.feipulai.device.serial.parser.PushUpParser;
import com.feipulai.device.serial.parser.RS232Parser;
import com.feipulai.device.serial.parser.RunTimerParser;
import com.feipulai.device.serial.parser.VCParser;
import com.feipulai.device.serial.parser.VolleyBallParser;
import com.orhanobut.logger.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pengjf on 2018/11/6.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RS232ReadRunnable extends SerialReadRunnable {

    public RS232ReadRunnable(InputStream inputStream, SerialPorter.OnDataArrivedListener listener) {
        super(inputStream, listener);
    }

    @Override
    public void convert(Message msg) {
        try {
            if (null == mInputStream) {
                return;
            }
            RS232Parser parser = null;
            if (MachineCode.machineCode == -1) {
                LogUtils.all("当前测试项目代码为-1,指令过滤");
                return;
            }
            switch (MachineCode.machineCode) {

                case ItemDefault.CODE_FHL:
                    parser = new VCParser();
                    break;

                case ItemDefault.CODE_HW:
                    parser = new HWParser();
                    break;
                case ItemDefault.CODE_ZFP:
                case ItemDefault.CODE_LQYQ:
                case ItemDefault.CODE_SHOOT:
                    parser = new RunTimerParser();
                    break;
                // 立定跳远
                case ItemDefault.CODE_LDTY:
                    // 红外实心球
                case ItemDefault.CODE_HWSXQ:
                case ItemDefault.CODE_MG:
                    // 坐位体前屈
                case ItemDefault.CODE_ZWTQQ:
                    parser = new DistanceParser();
                    break;

                case ItemDefault.CODE_PQ:
                    parser = new VolleyBallParser();
                    break;
                case ItemDefault.CODE_FWC:
                    parser = new PushUpParser();
                    break;

                case ItemDefault.CODE_GPS:
                    parser = new GPSParser();
                    break;
            }
            RS232Result result = null;
            if (parser != null) {
                if (SerialParams.RS232.getVersions() == 1) {
                    result = parser.parse(mInputStream);
                } else {
                    byte[] data = readV2();
                    if (data != null) {
                        result = new RS232Result(data);
                    }
                }

                if (result != null) {
                    msg.what = result.getType();
                    msg.obj = result.getResult();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 串口配置V2是转接板 需要进行协议A5 5A解析,与868相同
     *
     * @return
     */
    private byte[] readV2() {
        try {
            while (mInputStream.available() < 1) {
                Thread.sleep(10);
            }
            //找协议头
            //可能存在协议头冲突:如果协议头冲突，协议尾会出错，最多丢失两帧
            byte[] readLength = new byte[4];
            mInputStream.read(readLength, 0, 1);
            if ((readLength[0] & 0xff) != 0xa5) {
                //Log.e("dddd","read wrong packet head: readLength[0]= " + readLength[0]);
                LogUtils.all(readLength.length + "---" + StringUtility.bytesToHexString(readLength) + "-232--协议头0错误,已过滤");
                return null;
            }

            while (mInputStream.available() < 1) {
                Thread.sleep(10);
            }

            mInputStream.read(readLength, 1, 1);
            if ((readLength[1] & 0xff) != 0x5a) {
                //Log.e("dddd","read wrong packet head: readLength[0]= " + readLength[1]);
                LogUtils.all(readLength.length + "---" + StringUtility.bytesToHexString(readLength) + "-232--协议头1错误,已过滤");
                return null;
            }

            //读包长度和命令字
            while (mInputStream.available() < 2) {
                Thread.sleep(10);
            }
            mInputStream.read(readLength, 2, 2);

            int dataLength = readLength[3] & 0xff;
            while (mInputStream.available() < dataLength + 3) {
                Thread.sleep(10);
            }
            //read data according to data length
            byte[] data = new byte[dataLength];
            mInputStream.read(data);

            //read checksum
            byte[] checksum = new byte[1];
            mInputStream.read(checksum);

            //read packet end
            byte[] packetEnd = new byte[2];
            mInputStream.read(packetEnd);

            //check packet end
            if ((packetEnd[0] & 0xff) != 0xaa || (packetEnd[1] & 0xff) != 0x55) {
                //如果这之前有数据写入,会被丢弃,这里直接返回即可
                //直接返回的话,就可能存在无法找到正确的协议头,导致连续丢帧
//				throwData();
                LogUtils.all(readLength.length + "---" + StringUtility.bytesToHexString(packetEnd) + "-232--协议尾错误,已过滤");

                return null;
            }
            LogUtils.all("-232--协议数据"+ StringUtility.bytesToHexString(data));
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }
}
