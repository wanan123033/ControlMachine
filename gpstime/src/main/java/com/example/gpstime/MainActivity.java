package com.example.gpstime;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.IOPower;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.device.serial.beans.GPSTimeResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
		implements SerialDeviceManager.RS232ResiltListener {
	
	private TextView tvReceive;
	private ScrollView scrollReceive;
	private Button btnClear;
	private File storageFile;
	private static final String TITLE = "序号\t\t\t时间\n";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tvReceive = findViewById(R.id.tv_receive);
		scrollReceive = findViewById(R.id.scr_receive);
		tvReceive.setText(TITLE);
		
		btnClear = findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tvReceive.setText(TITLE);
			}
		});
		
		storageFile = new File(Environment.getExternalStorageDirectory().getPath() + "/gps_time.txt");
		IOPower.getInstance().setUhfcommPwr(1);
		// 走 232 的发送方式,但是使用的是 Radio 的口
		SerialParams.RS232.setPath(SerialParams.RADIO.getPath());
		MachineCode.machineCode = ItemDefault.CODE_GPS;
		SerialDeviceManager.getInstance().setRS232ResiltListener(this);
	}
	
	@Override
	public void onRS232Result(Message msg) {
		switch (msg.what) {
			
			case SerialConfigs.GPS_TIME_RESPONSE:
				final GPSTimeResult result = (GPSTimeResult) msg.obj;
				String showString = String.format("%3d\t\t\t%26s\n", result.getSequence(), result.getTime());
				deal(showString);
				break;
			
		}
	}
	
	private void deal(final String showString) {
		try {
			FileOutputStream fos = new FileOutputStream(storageFile, true);
			fos.write(showString.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvReceive.append(showString);
				scrollReceive.fullScroll(View.FOCUS_DOWN);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		SerialDeviceManager.getInstance().close();
		IOPower.getInstance().setUhfcommPwr(0);
	}
	
}
