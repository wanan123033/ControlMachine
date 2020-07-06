package com.ww.fpl.videolibrary.camera;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hcnetsdk.jna.HCNetSDKByJNA;
import com.hcnetsdk.jna.HCNetSDKJNAInstance;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.NET_DVR_TIME;
import com.ww.fpl.videolibrary.StorageUtils;
import com.ww.fpl.videolibrary.play.util.PUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * created by ww on 2019/12/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class HkCameraManager {
    public String PATH = Environment.getExternalStorageDirectory() + "/HKVideo/";
    private static final String TAG = "HkCameraManager";
    private int m_iLogID = -1;
    private int m_iPlayID = -1;
    private String strIP;
    private int nPort;
    private String strUser;
    private String strPsd;
    private int m_iStartChan = 0; // start channel no
    private int m_iChanNum = 0; // channel number
    private Activity activity;
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private String sdRootPath;
//    private CameraListener listener;
//
//    interface CameraListener {
//        void onCamera(String message);
//    }

    public HkCameraManager(Activity activity, String strIP, int nPort, String strUser, String strPsd) {
        this.strIP = strIP;
        this.nPort = nPort;
        this.strUser = strUser;
        this.strPsd = strPsd;
        this.activity = activity;
    }

    /**
     * 第一步激活
     *
     * @return
     */
    public boolean initeSdk() {
        // init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        createFile();
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, PATH + "Log/", true);

        //设置海康sdk，0切片（默认），1不切片
        HCNetSDKByJNA.NET_DVR_LOCAL_GENERAL_CFG cfg = new HCNetSDKByJNA.NET_DVR_LOCAL_GENERAL_CFG();
        cfg.byNotSplitRecordFile = 1;
        boolean cfgFlag = HCNetSDKJNAInstance.getInstance().NET_DVR_SetSDKLocalCfg(HCNetSDKByJNA.NET_SDK_LOCAL_CFG_TYPE.NET_DVR_LOCAL_CFG_TYPE_GENERAL, cfg);
        Log.e("cfgFlag", "------------" + cfgFlag);
        return true;
    }

    private void createFile(){
        ArrayList<StorageUtils.Volume> storys = StorageUtils.getVolume(activity);
        for (StorageUtils.Volume volume : storys
                ) {
            if (volume.isRemovable() && volume.getState().equals("mounted")) {
                PATH = volume.getPath() + "/HKVideo/";
                break;
            }
        }
        if (!PUtil.createFile(PATH)) {
            PATH = Environment.getExternalStorageDirectory() + "/HKVideo/";
        }
        Log.i("PATH", PATH);
    }

    /**
     * 第二步连接海康监控
     *
     * @param user
     * @param password
     */
    public boolean login2HK(String user, String password) {
        try {
            this.strUser = user;
            this.strPsd = password;
            if (m_iLogID < 0) {
                // login on the device
                m_iLogID = loginDevice();
                if (m_iLogID < 0) {
                    Log.e(TAG, "This device logins failed!");
                    return false;
                } else {
                    System.out.println("m_iLogID=" + m_iLogID);
                }
                // get instance of exception callback and set
                ExceptionCallBack oexceptionCbf = getExceptiongCbf();
                if (oexceptionCbf == null) {
                    Log.e(TAG, "ExceptionCallBack object is failed!");
                    return false;
                }
                if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                        oexceptionCbf)) {
                    Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                    return false;
                }
//                Toast.makeText(activity, "注册摄像头成功", Toast.LENGTH_SHORT).show();

                //同步时间
                NET_DVR_TIME net_time = new NET_DVR_TIME();
                Calendar calendar = Calendar.getInstance();  //获取当前时间
                net_time.dwYear = calendar.get(Calendar.YEAR);
                net_time.dwMonth = calendar.get(Calendar.MONTH) + 1;
                net_time.dwDay = calendar.get(Calendar.DAY_OF_MONTH);
                net_time.dwHour = calendar.get(Calendar.HOUR_OF_DAY);
                net_time.dwMinute = calendar.get(Calendar.MINUTE);
                net_time.dwSecond = calendar.get(Calendar.SECOND);
                boolean isTime = HCNetSDK.getInstance().NET_DVR_SetDVRConfig(m_iLogID, HCNetSDK.NET_DVR_SET_TIMECFG, m_iChanNum, net_time);
                Log.i(TAG, "Login sucess ****************************1***************************" + isTime);
                return true;
            }
        } catch (Exception err) {
            Log.e(TAG, "error: " + err.toString());
            return false;
        }
        return false;
    }

    public void loginOut() {
        if (m_iLogID >= 0) {
            // whether we have logout
            if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                Log.e(TAG, " NET_DVR_Logout is failed!");
                return;
            }
            Log.i(TAG, "NET_DVR_Logout is succeed!");
            m_iLogID = -1;
        }
    }

    /**
     * 第三步
     * 或者开始或者停止预览
     */
    public boolean startPreview() {
        try {
//            ((InputMethodManager) MainActivity.activity.getSystemService(Context.INPUT_METHOD_SERVICE))
//                    .hideSoftInputFromWindow(MainActivity.activity.getCurrentFocus().getWindowToken(),
//                            InputMethodManager.HIDE_NOT_ALWAYS);
            if (m_iLogID < 0) {
                Log.e(TAG, "please login on device first");
                Toast.makeText(activity, "摄像头连接失败", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (m_iPlayID < 0) {
                return startSinglePreview();
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return false;
        }
        return false;
    }

    public void stopPreview() {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return;
        }
        if (m_iPlayID >= 0) {
            stopSinglePreview();
        }
    }

    //开始单通道预览
    private boolean startSinglePreview() {
        Log.i(TAG, "m_iStartChan:" + m_iStartChan);
        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0; // main stream
        previewInfo.bBlocked = 1;
        previewInfo.hHwnd = playView[0].getHolder();

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, null);
        if (m_iPlayID < 0) {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError() + "---" + m_iLogID);
            return false;
        }
        Log.i(TAG, "开启预览" + "---" + m_iLogID);
        Toast.makeText(activity, "摄像头已开启", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void stopSinglePreview() {
        if (m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return;
        }

        if (HCNetSDKJNAInstance.getInstance().NET_DVR_CloseSound()) {
            Log.e(TAG, "NET_DVR_CloseSound Succ!");
        }

        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        Log.i(TAG, "停止预览");
        m_iPlayID = -1;
    }

    private int loginDevice() {
        int iLogID = -1;
        iLogID = loginNormalDevice();
        return iLogID;
    }

    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        if (TextUtils.isEmpty(strUser) || TextUtils.isEmpty(strPsd) || TextUtils.isEmpty(strIP)) {
            return -1;
        }
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }

        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }

//        NET_DVR_IPPARACFG_V40 struIP = new NET_DVR_IPPARACFG_V40();
//        HCNetSDK.getInstance().NET_DVR_GetDVRConfig(m_iLogID,
//                HCNetSDK.NET_DVR_GET_IPPARACFG_V40, m_iStartChan, struIP);

//        if (m_iChanNum > 1) {
//            ChangeSingleSurFace(false);
//        } else {
        ChangeSingleSurFace(true);
//        }
        Log.i(TAG, "NET_DVR_Login is Successful!");
        return iLogID;
    }

    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    private PlaySurfaceView[] playView = new PlaySurfaceView[1];

    private void ChangeSingleSurFace(boolean bSingle) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

//        for (int i = 0; i < 4; i++) {
//            if (playView[i] == null) {
//                playView[i] = new PlaySurfaceView(activity);
//                playView[i].setParam(metric.widthPixels / 3, metric.heightPixels / 2);
//                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.WRAP_CONTENT,
//                        FrameLayout.LayoutParams.WRAP_CONTENT);
//                switch (i) {
//                    case 0:
//                        params.bottomMargin = metric.heightPixels / 2;
//                        params.leftMargin = 0;
//                        break;
//                    case 1:
//                        params.bottomMargin = metric.heightPixels / 2;
//                        params.leftMargin = metric.widthPixels / 3;
//                        break;
//                    case 2:
//                        params.bottomMargin = 0;
//                        params.leftMargin = 0;
//                        break;
//                    case 3:
//                        params.bottomMargin = 0;
//                        params.leftMargin = metric.widthPixels / 3;
//                        break;
//                    default:
//                        break;
//                }
//                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
//                activity.addContentView(playView[i], params);
//                playView[i].setVisibility(View.INVISIBLE);
//
//            }
//        }

        if (bSingle) {
            playView[0] = new PlaySurfaceView(activity);
//            for (int i = 0; i < 4; ++i) {
//                playView[i].setVisibility(View.INVISIBLE);
//            }
//            playView[0].setParam(metric.widthPixels/2, metric.heightPixels/2);
//            int widthPx = PUtil.dip2px(activity, 360);
//            int heightPx = widthPx / 360 * 276;
            int widthPx = 360;
            int heightPx = 200;
            Log.i("playView", widthPx + "---" + heightPx);
            playView[0].setParam(widthPx, heightPx);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 0;
            params.leftMargin = PUtil.dip2px(activity, 390);
            params.topMargin = PUtil.dip2px(activity, 50);
            // params.
            params.gravity = Gravity.TOP | Gravity.LEFT;
            playView[0].setLayoutParams(params);
            activity.addContentView(playView[0], params);
            playView[0].setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < 4; ++i) {
                playView[i].setVisibility(View.VISIBLE);
            }
        }

    }

    //更改预览位置
    public void ChangeSurFace_Left() {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return;
        }
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

//        int widthPx = PUtil.dip2px(activity, 360);
//        int heightPx = widthPx / 360 * 276;
        int widthPx = 360;
        int heightPx = 200;
        playView[0].setParam(widthPx, heightPx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 0;
        params.leftMargin = PUtil.dip2px(activity, 10);
        params.topMargin = PUtil.dip2px(activity, 50);
        // params.
        params.gravity = Gravity.TOP | Gravity.LEFT;
        playView[0].setLayoutParams(params);
        playView[0].setVisibility(View.VISIBLE);

    }

    //更改预览位置
    public void ChangeSurFace_Center() {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return;
        }
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);

//        int widthPx = PUtil.dip2px(activity, 360);
//        int heightPx = widthPx / 360 * 276;
        int widthPx = 360;
        int heightPx = 200;
        playView[0].setParam(widthPx, heightPx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 0;
        params.leftMargin = PUtil.dip2px(activity, 390);
        params.topMargin = PUtil.dip2px(activity, 50);
        // params.
        params.gravity = Gravity.TOP | Gravity.LEFT;
        playView[0].setLayoutParams(params);
        playView[0].setVisibility(View.VISIBLE);

    }

    public boolean m_bSaveRealData = false;

    private String oldName = "";

    /**
     * 开始录像
     */
    public void startRecord(long startTime) {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return;
        }
        if (!m_bSaveRealData) {
//            oldName = "/sdcard/" + startTime;
            oldName = PATH + startTime;
            if (!HCNetSDKJNAInstance.getInstance().NET_DVR_SaveRealData_V30(m_iPlayID, 0x1, oldName + ".mp4")) {
                System.out.println("NET_DVR_SaveRealData_V30 failed! error: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                return;
            } else {
                System.out.println("NET_DVR_SaveRealData_V30 succ!");
            }
            m_bSaveRealData = true;
        }
    }

    /**
     * 停止录像
     */
    public void stopRecord(long endTime) {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return;
        }
        if (m_bSaveRealData) {
            if (!HCNetSDK.getInstance().NET_DVR_StopSaveRealData(m_iPlayID)) {
                System.out.println("NET_DVR_StopSaveRealData failed! error: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
            } else {
                System.out.println("NET_DVR_StopSaveRealData succ!");
            }
            m_bSaveRealData = false;
            List<String> paths = PUtil.getFilesAllName(PATH);
            //结束录像之后对录像文件重命名，格式——“开始时间戳_结束时间戳.mp4”
            //海康sdk会默认对大于1G的录像文件切片，以_1,_2...结尾
            for (String pathName : paths
                    ) {
                if ((PATH + pathName).contains(oldName)) {
                    renameFile(PATH + pathName, oldName, oldName + "_" + endTime);
                }
            }
        }
    }

    private void getSDRoot() {
        boolean sdCardExist = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //为真则SD卡已装入，
            sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }
        Log.i("getSDRoot", "--->" + sdCardExist);
        if (sdCardExist) {
            sdRootPath = Environment.getExternalStorageDirectory().toString();
            Log.i("getSDRoot", "--->" + sdRootPath);
        } else {
            sdRootPath = "";
        }
    }

    /**
     * 获取外置SD卡路径以及TF卡的路径
     * <p>
     * 返回的数据：paths.get(0)肯定是外置SD卡的位置，因为它是primary external storage.
     *
     * @return 所有可用于存储的不同的卡的位置，用一个List来保存
     */
    private List<String> getExtSDCardPathList() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                paths.add(mountPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("getSDRoot", "--->" + paths.toString());
        return paths;
    }

    /**
     * 释放sdk资源
     */
    public void clearSdk() {
        HCNetSDK.getInstance().NET_DVR_Cleanup();
        m_iLogID = -1;
        m_iPlayID = -1;
        playView = null;
        m_oNetDvrDeviceInfoV30 = null;
    }

    private void renameFile(String pathName, String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return;
        }

        if (TextUtils.isEmpty(newPath)) {
            return;
        }
        File file = new File(pathName);
        file.renameTo(new File(pathName.replace(oldPath, newPath)));
    }

    public void setStrIP(String strIP) {
        this.strIP = strIP;
    }

    public void setnPort(int nPort) {
        this.nPort = nPort;
    }

    public void setStrUser(String strUser) {
        this.strUser = strUser;
    }

    public void setStrPsd(String strPsd) {
        this.strPsd = strPsd;
    }
}
