package com.feipulai.common.dbutils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.feipulai.common.R;
import com.feipulai.common.dbutils.receiver.UsbStateChangeReceiver;
import com.feipulai.common.dbutils.receiver.UsbStatusChangeEvent;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.baseToolbar.StatusBarUtil;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.partition.Partition;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileSelectActivity extends Activity
        implements View.OnClickListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    public static final String FILE = "file";
    public static final String BACK_TO_ROOT = "back_to_root";
    public static final String BACK_TO_PARENT = "back_to_parent";
    public static final String USB_STORAGE = "外部存储";
    public static final String SD_STORAGE = "内部存储";

    public static final int CHOOSE_DIR = 0x01;
    public static final int CHOOSE_FILE = 0x02;
    BaseToolbar toolbar;
    TextView mTvAutoBackFile;
    Button mBtnConfirm;
    Button mBtnCanceL;
    ListView listView;
    TextView tvGuide;
    LinearLayout ll;

    public static final String INTENT_ACTION = "intent_action";
    private List<FileItemBean> mFileItemBeans;
    private int mRequestType;
    private Set<UsbMassStorageDevice> usingStorageDeviceSet = new HashSet<>();
    private UsbStateChangeReceiver receiver = new UsbStateChangeReceiver();
    private UsbFile currentFile; // 当前显示的文件夹,如果为null,表示当前显示盘符
    private FileItemBean backToRoot = new FileItemBean(BACK_TO_ROOT, null);
    private FileItemBean backToParent;
    private UsbManager usbManager;
    private boolean isFolderSelecting;
    // 因为UsbFile 本身不可序列化,这里用一个全局变量来传递其值
    public static UsbFile sSelectedFile;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        StatusBarUtil.setImmersiveTransparentStatusBar(this);//设置沉浸式透明状态栏 配合使用
        setContentView(R.layout.layout_files_elect);
        toolbar = findViewById(R.id.toolbar);
        mTvAutoBackFile = findViewById(R.id.tv_auto_back_file);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCanceL = findViewById(R.id.btn_cancel);
        listView = findViewById(R.id.list);
        tvGuide = findViewById(R.id.tv_guide);
        ll = findViewById(R.id.ll);
        mTvAutoBackFile.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        mBtnCanceL.setOnClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        init();
        initToolbar();

    }

    private void init() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mRequestType = getIntent().getIntExtra(INTENT_ACTION, CHOOSE_DIR);
        if (mRequestType == CHOOSE_FILE) {
            //文件选择不需要确定和取消按钮
            mBtnConfirm.setVisibility(View.GONE);
            mBtnCanceL.setVisibility(View.GONE);
            tvGuide.setText("请选择文件");
            toolbar.setTitle("请选择文件");
        } else {
            tvGuide.setText("长按选择文件夹");
            toolbar.setTitle("长按选择文件夹");
        }
        attachUsbs();
        registerUDiskReceiver();
        updateFilesDir();
    }

    private void initToolbar() {

        toolbar.setBackButton(R.drawable.icon_white_goback);
        toolbar.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        toolbar.setToolbarBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        toolbar.setSubTextColor(ContextCompat.getColor(this, R.color.white));
        toolbar.setStatusBarTransparent();
        toolbar.addRightImage(R.drawable.icon_auto_back_file, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAutoBackFile();
            }
        });
        toolbar.addRightText("自动备份", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAutoBackFile();
            }
        });
        toolbar.addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void gotoAutoBackFile() {
        File backupFile = new File(BackupManager.AUTO_BACKUP_DIR);
        if (!backupFile.exists()) {
            backupFile.mkdirs();
        }
        currentFile = new UsbFileAdapter(backupFile);
        updateFilesDir();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void registerUDiskReceiver() {
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction("android.hardware.usb.action.USB_STATE");
        usbDeviceStateFilter.addAction(UsbStateChangeReceiver.ACTION_USB_PERMISSION);
        registerReceiver(receiver, usbDeviceStateFilter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUsbStateChange(UsbStatusChangeEvent event) {
        if (event.isConnected) {
            attachUsbs();
            if (currentFile == null) {
                updateFilesDir();
            }
        } else {
            // 设备拔出,刷新显示
            for (UsbMassStorageDevice device : usingStorageDeviceSet) {
                if (event.usbDevice.equals(device.getUsbDevice())) {
                    usingStorageDeviceSet.remove(device);
                }
                currentFile = null;
                updateFilesDir();
            }
        }
    }

    private void updateFilesDir() {
        mFileItemBeans = new ArrayList<>();
        // 显示SD卡根目录和USB设备根目录
        if (currentFile == null) {
            // sdcard
            UsbFile usbFile = new UsbFileAdapter(Environment.getExternalStorageDirectory());
            mFileItemBeans.add(new FileItemBean(SD_STORAGE, usbFile));
            // usb storage
            for (UsbMassStorageDevice usbDevice : usingStorageDeviceSet) {
                try {
                    usbDevice.init();
                    List<Partition> partitions = usbDevice.getPartitions();
                    if (partitions != null && partitions.size() > 0) {
                        FileSystem currentFs = partitions.get(0).getFileSystem();
                        mFileItemBeans.add(new FileItemBean(currentFs.getVolumeLabel()/*USB_STORAGE + i++*/, currentFs.getRootDirectory()));
                        Logger.i(currentFs.getChunkSize() + "");
                    } else {
                        Toast.makeText(this, "不支持的分区格式,请插入FAT32格式U盘", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Toast.makeText(this, "U盘初始化失败...", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            mBtnConfirm.setEnabled(true);
            mFileItemBeans.add(backToRoot);
            backToParent = new FileItemBean(BACK_TO_PARENT, currentFile.getParent());
            mFileItemBeans.add(backToParent);
            try {
                UsbFile[] files = currentFile.listFiles();
                for (UsbFile file : files) {
                    mFileItemBeans.add(new FileItemBean(file.getName(), file));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileSelectAdapter mFileSelectAdapter = new FileSelectAdapter(this, mFileItemBeans);
        listView.setAdapter(mFileSelectAdapter);
    }

    private void attachUsbs() {
        if (usbManager == null) {
            return;
        }
        UsbMassStorageDevice[] allUSBStorageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        usingStorageDeviceSet.clear();
        // Log.i("james","allUSBStorageDevices.length:" + allUSBStorageDevices.length);

        for (UsbMassStorageDevice device : allUSBStorageDevices) {
            // Log.i("james",device.toString());
            // 对每个设备都要有权限
            if (usbManager.hasPermission(device.getUsbDevice())) {
                usingStorageDeviceSet.add(device);
            } else {
                requestPermission(device.getUsbDevice());
            }
        }
        // Log.i("james","usingStorageDeviceSet.size:" + usingStorageDeviceSet.size());
    }

    private void requestPermission(UsbDevice device) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(UsbStateChangeReceiver.ACTION_USB_PERMISSION), 0);
        // 申请权限,申请结果会在广播中收到
        usbManager.requestPermission(device, pendingIntent);
    }

    @Override
    public void onClick(View v) {
        // Resource IDs are non final in the library projects since SDK tools r14,
        // means that the library code cannot treat these IDs as constants
        if (v.getId() == R.id.tv_auto_back_file) {
            File backupFile = new File(BackupManager.AUTO_BACKUP_DIR);
            if (!backupFile.exists()) {
                backupFile.mkdirs();
            }
            currentFile = new UsbFileAdapter(backupFile);
            updateFilesDir();
        } else if (v.getId() == R.id.btn_confirm) {
            if (mRequestType == CHOOSE_DIR) {
                Intent data = new Intent();
                // 成功标志
                data.putExtra(FILE, true);
                setResult(RESULT_OK, data);
                finish();
            }
        } else if (v.getId() == R.id.btn_cancel) {
            if (isFolderSelecting) {
                updateFilesDir();
                isFolderSelecting = false;
                ll.setVisibility(View.GONE);
            }
        }
    }

    // 短按选择文件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItemBean bean = mFileItemBeans.get(position);
        UsbFile file = bean.getFile();
        if (mRequestType == CHOOSE_DIR) {
            // 点击了被选中的结果,取消选中
            if (sSelectedFile != null && file == sSelectedFile) {
                sSelectedFile = null;
                ll.setVisibility(View.GONE);
                updateFilesDir();
                return;
            }
            ll.setVisibility(View.GONE);
        }
        // 选择了 "返回根目录"  || 或 在盘符根目录选择了"返回上一级目录"
        //Log.i("james",(bean == backToRoot) + "");
        if (bean == backToRoot
                || (bean == backToParent && currentFile != null && currentFile.isRoot())) {
            currentFile = null;
        } else if (file.isDirectory()) {
            currentFile = file;
        } else if (mRequestType == CHOOSE_FILE) {
            // 选择一个文件
            Intent data = new Intent();
            sSelectedFile = file;
            // 成功标志
            data.putExtra(FILE, true);
            setResult(RESULT_OK, data);
            finish();
        }
        updateFilesDir();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileItemBean bean = mFileItemBeans.get(position);
        UsbFile file = bean.getFile();
        if (mRequestType == CHOOSE_DIR) {
            // "返回根目录" 和 "返回上一层" 不能被选中
            if (BACK_TO_ROOT.equals(bean.getName()) || BACK_TO_PARENT.equals(bean.getName())) {
                return true;
            }
            if (file.isDirectory()) {
                view.setBackgroundColor(Color.BLUE);
                ll.setVisibility(View.VISIBLE);
                sSelectedFile = file;
                isFolderSelecting = true;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.getInstance().onDestroy(this);
        unregisterReceiver(receiver);
    }

    // private void deleteFiles(UsbFile file) {
    //     try {
    //         if (file.isDirectory()) {
    //             UsbFile[] files = file.listFiles();
    //             for (UsbFile file1 : files) {
    //                 deleteFiles(file1);
    //             }
    //             file.delete();
    //         } else {
    //             file.delete();
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

}