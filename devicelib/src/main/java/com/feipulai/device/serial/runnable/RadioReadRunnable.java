package com.feipulai.device.serial.runnable;

import android.os.Message;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialPorter;
import com.feipulai.device.serial.beans.ConverterVersion;
import com.feipulai.device.serial.beans.Radio868Result;
import com.feipulai.device.serial.beans.StringUtility;
import com.orhanobut.logger.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pengjf on 2018/11/6.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RadioReadRunnable extends SerialReadRunnable{
	
	public RadioReadRunnable(InputStream inputStream, SerialPorter.OnDataArrivedListener listener){
		super(inputStream,listener);
	}
	
	@Override
	public void convert(Message msg){
		try{
			while(mInputStream.available() < 1){
				Thread.sleep(10);
			}
			//找协议头
			//可能存在协议头冲突:如果协议头冲突，协议尾会出错，最多丢失两帧
			byte[] readLength = new byte[4];
			mInputStream.read(readLength,0,1);
			if((readLength[0] & 0xff) != 0xa5){
				//Log.e("dddd","read wrong packet head: readLength[0]= " + readLength[0]);
				LogUtils.all(readLength.length+"---"+ StringUtility.bytesToHexString(readLength)+"---协议头错误,已过滤");
				return;
			}

			while(mInputStream.available() < 1){
				Thread.sleep(10);
			}

			mInputStream.read(readLength,1,1);
			if((readLength[1] & 0xff) != 0x5a){
				//Log.e("dddd","read wrong packet head: readLength[0]= " + readLength[1]);
				LogUtils.all(readLength.length+"---"+ StringUtility.bytesToHexString(readLength)+"---协议头错误,已过滤");
				return;
			}

			//读包长度和命令字
			while(mInputStream.available() < 2){
				Thread.sleep(10);
			}
			mInputStream.read(readLength,2,2);

			int dataLength = readLength[3] & 0xff;
			while(mInputStream.available() < dataLength + 3){
				Thread.sleep(10);
			}

			//read data according to data length
			byte[] data = new byte[dataLength];
			mInputStream.read(data);

			//read checksum
			byte[] checksum = new byte[1];
			mInputStream.read(checksum);

			// TODO: 2017/12/4 14:19 we should check checksum here

			//read packet end
			byte[] packetEnd = new byte[2];
			mInputStream.read(packetEnd);
			//check packet end
			if((packetEnd[0] & 0xff) != 0xaa || (packetEnd[1] & 0xff) != 0x55){
				//如果这之前有数据写入,会被丢弃,这里直接返回即可
				//直接返回的话,就可能存在无法找到正确的协议头,导致连续丢帧
//				throwData();
				return;
			}
			// 工厂模式
			switch(readLength[2] & 0xff){
				case 0xb5://设置无线频道
					// Log.i("james","length:"+data.length);
					msg.what = SerialConfigs.CONVERTER_RADIO_CHANNEL_SETTING_RESPONSE;
					msg.obj = data[0] ;
					// Log.i("james",msg.obj + "设置无线频道设置OK?\t" + msg.obj);
					break;
				case 0xb1:
					ConverterVersion converterVersion = new ConverterVersion(data);
					msg.what = SerialConfigs.CONVERTER_VERSION_RESPONSE;
					msg.obj = converterVersion;
					break;
				case 0xd1://无线868->安卓
					Radio868Result result = new Radio868Result(data);
					msg.what = result.getType();
					msg.obj = result.getResult();
//					Log.i("radio 868 receive", StringUtility.bytesToHexString(data));
					break;
			}
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		// TODO: 2018/11/20 0020 17:22 for test
		//try{
		//	byte[] readLength = new byte[17];
		//	while(mInputStream.available() < readLength.length){}
		//	mInputStream.read(readLength);
		//	//Log.i("receive",StringUtility.bytesToHexString(readLength));
		//	Log.i("tid:" + Thread.currentThread().getId() + "-----receive",StringUtility.bytesToHexString(readLength));
		//}catch(IOException e){
		//	e.printStackTrace();
		//}
	}
	
}
