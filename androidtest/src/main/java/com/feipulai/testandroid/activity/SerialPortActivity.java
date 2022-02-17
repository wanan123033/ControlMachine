package com.feipulai.testandroid.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.testandroid.R;
import com.feipulai.testandroid.comn.Device;
import com.feipulai.testandroid.comn.SerialPortManager;
import com.feipulai.testandroid.utils.AllCapTransformationMethod;
import com.feipulai.testandroid.utils.ByteUtil;
import com.feipulai.testandroid.utils.ToastUtil;


public class SerialPortActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    Spinner mSpinnerDevices;
    Spinner mSpinnerBaudrate;
    Button mBtnOpenDevice;
    EditText mEtData;
    Button mBtnSendData;
    private Device mDevice;

    private int mDeviceIndex;
    private int mBaudrateIndex;

    private String[] mDevices;
    private String[] mBaudrates;

    private boolean mOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        PrefHelper.initDefault(this);
        initView();
        initDevice();
        initSpinners();
        updateViewState(mOpened);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_serial_port;
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.instance().close();
        super.onDestroy();
    }

    @Override
    protected boolean hasActionBar() {
        return false;
    }

    /**
     * 初始化设备列表
     */
    private void initDevice() {

//        SerialPortFinder serialPortFinder = new SerialPortFinder();

        // 设备
//        mDevices = serialPortFinder.getAllDevicesPath();
        mDevices = new String[]{"/dev/ttysWK3", "/dev/ttysWK0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4"};
        if (mDevices.length == 0) {
            mDevices = new String[]{
                    getString(R.string.no_serial_device)
            };
        }
        // 波特率
//        mBaudrates = getResources().getStringArray(R.array.baudrates);
        mBaudrates = new String[]{"4800","9600", "115200"};

//        mDeviceIndex = PrefHelper.getDefault().getInt(PreferenceKeys.SERIAL_PORT_DEVICES, 0);
//        mDeviceIndex = mDeviceIndex >= mDevices.length ? mDevices.length - 1 : mDeviceIndex;
//        mBaudrateIndex = PrefHelper.getDefault().getInt(PreferenceKeys.BAUD_RATE, 0);
        mDeviceIndex = 2;
        mBaudrateIndex = 1;
        mDevice = new Device(mDevices[mDeviceIndex], mBaudrates[mBaudrateIndex]);
    }

    private void initView() {
        mSpinnerDevices = findViewById(R.id.spinner_devices);
        mSpinnerBaudrate = findViewById(R.id.spinner_baudrate);
        mBtnOpenDevice = findViewById(R.id.btn_open_device);
        mEtData = findViewById(R.id.et_data);
        mBtnSendData = findViewById(R.id.btn_send_data);

        mBtnSendData.setOnClickListener(this);
        mBtnOpenDevice.setOnClickListener(this);
    }

    /**
     * 初始化下拉选项
     */
    private void initSpinners() {
        mEtData.setTransformationMethod(new AllCapTransformationMethod(true));
        ArrayAdapter<String> deviceAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mDevices);
        deviceAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerDevices.setAdapter(deviceAdapter);
        mSpinnerDevices.setOnItemSelectedListener(this);

        ArrayAdapter<String> baudrateAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mBaudrates);
        baudrateAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerBaudrate.setAdapter(baudrateAdapter);
        mSpinnerBaudrate.setOnItemSelectedListener(this);

        mSpinnerDevices.setSelection(mDeviceIndex);
        mSpinnerBaudrate.setSelection(mBaudrateIndex);
    }


    private void sendData() {

        String text = mEtData.getText().toString().trim();
        if (TextUtils.isEmpty(text) || text.length() % 2 != 0) {
            ToastUtil.showOne(this, "无效数据");
            return;
        }
        byte[] bytes = new byte[]{0x5A, 0x33, 0x34, 0x30, 0x39, 0x33, 0x03, 0x0d, 0x0a};
        SerialPortManager.instance().sendCommand(ByteUtil.bytes2HexStr(bytes));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bytes = new byte[]{ 0x43, 0x30, 0x36, 0x37, 0x03, 0x0d, 0x0a};
        SerialPortManager.instance().sendCommand(ByteUtil.bytes2HexStr(bytes));

//        SerialPortManager.instance().sendCommand(text);
    }

    /**
     * 打开或关闭串口
     */
    private void switchSerialPort() {
        if (mOpened) {
            SerialPortManager.instance().close();
            mOpened = false;
        } else {

            // 保存配置
//            PrefHelper.getDefault().saveInt(PreferenceKeys.SERIAL_PORT_DEVICES, mDeviceIndex);
//            PrefHelper.getDefault().saveInt(PreferenceKeys.BAUD_RATE, mBaudrateIndex);

            mOpened = SerialPortManager.instance().open(mDevice) != null;
            if (mOpened) {
                ToastUtil.showOne(this, "成功打开串口");

                byte[] cmdBaudRate = new byte[]{(byte) 0XA5, 0X5A, (byte) 0XB2, 4, 0x04, 8, 0, 1, (byte) 0XFF, (byte) 0XAA, 0X55};
                SerialPortManager.instance().sendCommand(ByteUtil.bytes2HexStr(cmdBaudRate));
            } else {
                ToastUtil.showOne(this, "打开串口失败");
            }
        }
        updateViewState(mOpened);
    }

    /**
     * 更新视图状态
     *
     * @param isSerialPortOpened
     */
    private void updateViewState(boolean isSerialPortOpened) {

        int stringRes = isSerialPortOpened ? R.string.close_serial_port : R.string.open_serial_port;

        mBtnOpenDevice.setText(stringRes);

        mSpinnerDevices.setEnabled(!isSerialPortOpened);
        mSpinnerBaudrate.setEnabled(!isSerialPortOpened);
        mBtnSendData.setEnabled(isSerialPortOpened);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // Spinner 选择监听
        switch (parent.getId()) {
            case R.id.spinner_devices:
                mDeviceIndex = position;
                mDevice.setPath(mDevices[mDeviceIndex]);
                break;
            case R.id.spinner_baudrate:
                mBaudrateIndex = position;
                mDevice.setBaudrate(mBaudrates[mBaudrateIndex]);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 空实现
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_device:
                switchSerialPort();
                break;
            case R.id.btn_send_data:
                sendData();
                break;
        }
    }
}
