package com.feipulai.testandroid.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.MachineCode;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.testandroid.R;
import com.feipulai.testandroid.utils.AudioMngHelper;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.module.fingerprint.FingerprintFactory;
import com.zkteco.android.biometric.module.fingerprint.FingerprintSensor;
import com.zkteco.android.biometric.module.fingerprint.exception.FingerprintSensorException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.nidfpsensor.NIDFPFactory;
import com.zkteco.android.biometric.nidfpsensor.NIDFPSensor;
import com.zkteco.android.biometric.nidfpsensor.exception.NIDFPException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        CheckDeviceOpener.OnCheckDeviceArrived {

    private TextView wireless;
    private TextView tvICFinger;
    private TextView printer;
    private TextView qrCodeInfo;
    private TextView screen;
    private TextView link_868;
    private TextView cardInfo;
    private TextView tvIdCard;

    private static final int VID = 6997;    //Silkid VID always 6997
    private static final int IC_PID = 289;     //Silkid IC_PID always 289
    private static final int ID_PID = 770;     //Silkid IC_PID always 289
    private FingerprintSensor fingerprintSensor = null;
    private UsbManager musbManager = null;
    private final String ACTION_USB_PERMISSION = "com.zkteco.android.biometric.USB_PERMISSION";
    private NIDFPSensor mNIDFPSensor = null;
    private TextView tvIDFinger;
    private TextView btn_finger;
    private AudioMngHelper audioMngHelper;
    private MediaPlayer mediaPlayer;
    private TextView vcCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        wireless = findViewById(R.id.btn_serialPort);
        tvICFinger = findViewById(R.id.tv_ic_finger);
        printer = findViewById(R.id.btn_printer);
        qrCodeInfo = findViewById(R.id.btn_codeInfo);
        screen = findViewById(R.id.btn_screen);
        wireless.setOnClickListener(this);
        printer.setOnClickListener(this);
        screen.setOnClickListener(this);
        link_868 = findViewById(R.id.btn_link);
        link_868.setOnClickListener(this);
        cardInfo = findViewById(R.id.btn_cardInfo);
        tvIdCard = findViewById(R.id.btn_ID_cardInfo);
        tvIDFinger = findViewById(R.id.tv_id_finger);
        btn_finger = findViewById(R.id.btn_finger);
        btn_finger.setOnClickListener(this);

        mediaPlayer = new MediaPlayer();
        try {
            String uri = "android.resource://" + getPackageName() + "/" + R.raw.welcome;
            mediaPlayer.setDataSource(this, Uri.parse(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioMngHelper = new AudioMngHelper(this);
//        SeekBar seekBar = findViewById(R.id.seek);
//        seekBar.setMax(audioMngHelper.getSystemMaxVolume());
//        seekBar.setProgress(audioMngHelper.getSystemCurrentVolume());
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                audioMngHelper.setVoice100(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });

        TextView voice = findViewById(R.id.btn_voice);
        TextView volAdd = findViewById(R.id.btn_volAdd);
        TextView volSub = findViewById(R.id.btn_volSub);

        voice.setOnClickListener(this);
        volAdd.setOnClickListener(this);
        volSub.setOnClickListener(this);
        vcCheck = findViewById(R.id.btn_vc);
        vcCheck.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        RadioManager.getInstance().init();
        CheckDeviceOpener.getInstance().setOnCheckDeviceArrived(this);
        CheckDeviceOpener.getInstance().open(this, true, true, true);
//        requestDevicePermission();
//        IOPower.getInstance().setFingerPwr(1);
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrinterManager.getInstance().close();
        CheckDeviceOpener.getInstance().close();
//        IOPower.getInstance().setFingerPwr(0);
//        unregisterReceiver(mUsbReceiver);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_serialPort:
                startActivity(new Intent(this, SerialPortActivity.class));
                break;

            case R.id.btn_printer:
                PrinterManager.getInstance().print("打印机模块测试通过");
                PrinterManager.getInstance().print("\n\n");
                break;

            case R.id.btn_screen:
                startActivity(new Intent(this, ScreenActivity.class));
                break;

            case R.id.btn_link:
                MachineCode.machineCode = 2;
                LEDManager ledManager = new LEDManager();
                ledManager.link(2, 1);
                ledManager.resetLEDScreen(1, "安卓测试");
                break;
            case R.id.btn_finger:
                startActivity(new Intent(this, FingerprintActivity.class));
                break;
            case R.id.btn_voice:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                } else {
                    mediaPlayer.pause();
                }
                break;
            case R.id.btn_volAdd:
                audioMngHelper.addVoiceSystem();
                break;
            case R.id.btn_volSub:
                audioMngHelper.subVoiceSystem();
                break;
            case R.id.btn_vc:
                startActivity(new Intent(this, VcPairActivity.class));
                break;
        }
    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        if (icCardDealer == null) {
            return;
        }
        final StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardInfo.setText(stuInfo.toString());
            }
        });
    }

    @Override
    public void onIdCardRead(final IDCardInfo idCardInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvIdCard.setText("身份证模块测试通过" + idCardInfo.getId() + idCardInfo.getName() + "-指纹：" + idCardInfo.getFplength() + "民族：" + idCardInfo.getNation());
            }
        });
    }

    @Override
    public void onQrArrived(final String qrCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                qrCodeInfo.setText("扫码模块测试通过" + qrCode);
            }
        });
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {
    }

    private void requestDevicePermission() {
        musbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        for (UsbDevice device : musbManager.getDeviceList().values()) {
            if (device.getVendorId() == VID && (device.getProductId() == IC_PID || device.getProductId() == ID_PID)) {
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                musbManager.requestPermission(device, pendingIntent);
                break;
            }
        }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null || device.getVendorId() != VID) {
                return;
            }
            int pid = device.getProductId();
            if (pid != IC_PID && pid != ID_PID) {
                return;
            }

            switch (action) {
                case ACTION_USB_PERMISSION:
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        startFingerprintSensor(pid);
                    } else {
                        Toast.makeText(getApplicationContext(), "USB未授权", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if (musbManager.hasPermission(device)) {
                        startFingerprintSensor(pid);
                        break;
                    }
                    intent = new Intent(ACTION_USB_PERMISSION);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                    musbManager.requestPermission(device, pendingIntent);
                    break;
            }
        }
    };

    private void startFingerprintSensor(int pid) {
        if (pid == IC_PID) {
            Map fingerprintParams = new HashMap();
            fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
            fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, IC_PID);
            fingerprintSensor = FingerprintFactory.createFingerprintSensor(this, TransportType.USB, fingerprintParams);
            try {
                fingerprintSensor.open(0);
                tvICFinger.setText("IC卡指纹模块测试通过");
            } catch (FingerprintSensorException e1) {
                e1.printStackTrace();
            }
        } else if (pid == ID_PID) {
            Map fingerprintParams = new HashMap();
            fingerprintParams.put(ParameterHelper.PARAM_KEY_VID, VID);
            fingerprintParams.put(ParameterHelper.PARAM_KEY_PID, ID_PID);
            mNIDFPSensor = NIDFPFactory.createNIDFPSensor(this, TransportType.USBSCSI, fingerprintParams);
            try {
                //连接设备
                mNIDFPSensor.open(0);
                tvIDFinger.setText("身份证指纹模块测通过");
            } catch (NIDFPException e) {
                e.printStackTrace();
            }
        }
    }

}
