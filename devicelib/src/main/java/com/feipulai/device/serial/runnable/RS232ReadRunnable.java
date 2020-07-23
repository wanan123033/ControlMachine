package com.feipulai.device.serial.runnable;

import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialPorter;
import com.feipulai.device.serial.beans.RS232Result;
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
                result = parser.parse(mInputStream);
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
        // TODO: 2018/11/20 0020 17:22 for test
        //try{
        //	byte[] readLength = new byte[5];
        //	while(mInputStream.available() < readLength.length){}
        //	mInputStream.read(readLength);
        //	//Log.i("receive",StringUtility.bytesToHexString(readLength));
        //	Log.i("tid:" + Thread.currentThread().getId() + "-----receive",StringUtility.bytesToHexString(readLength));
        //}catch(IOException e){
        //	e.printStackTrace();
        //}
    }

}
