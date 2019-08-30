package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.feipulai.exam.activity.MiddleDistanceRace.bean.ServiceTcpBean;
import com.feipulai.exam.activity.MiddleDistanceRace.server.MySocketServer;
import com.feipulai.exam.activity.MiddleDistanceRace.server.OnResponseListener;
import com.feipulai.exam.activity.MiddleDistanceRace.server.WebConfig;
import com.feipulai.exam.entity.Schedule;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ww
 * @time 2019/8/8 11:33
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MyTcpService extends Service {

    private Work work;
    //有可能会内存泄漏,采用虚引用
    private WeakReference<MyTcpService> myService = new WeakReference<>(MyTcpService.this);
    private static final String TAG = "MyTcpService";
    public static final String SERVICE_CONNECT = "service_connect";

    public MyTcpService() {
    }

    private Work getWork() {
        if (work == null) {
            work = new Work();
        }
        return work;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return getWork();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public boolean isWork = false;

    public class Work extends Binder implements OnResponseListener {
        private MySocketServer mySocketServer;

        public void startWork(int port) {
            //开始工作
            isWork = true;
            WebConfig webConfig = new WebConfig();
            webConfig.setPort(port);
            webConfig.setMaxParallels(10);

            mySocketServer = new MySocketServer(webConfig, this);
            mySocketServer.startServerAsync();
        }

        public void stopWork() {
            isWork = false;
            //停止工作
            try {
                mySocketServer.stopServerAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public MyTcpService getMyService() {
            return myService.get();
        }

        ServiceTcpBean serviceTcpBean;

        @Override
        public void onTcpReceiveResult(Schedule schedule, String itemName) {
            serviceTcpBean = new ServiceTcpBean(schedule, itemName);
            Message message = handler.obtainMessage();
            message.obj = serviceTcpBean;
            message.what = 1;
            handler.sendMessage(message);
        }

        @Override
        public void OnTcpServerSuccess(boolean bool, String info) {
            Message message = handler.obtainMessage();
            message.obj = info;
            message.what = 2;
            handler.sendMessage(message);
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ServiceTcpBean message = (ServiceTcpBean) msg.obj;
                    //将信息反馈给接口
                    if (callBacks != null && callBacks.size() > 0) {
                        for (CallBack callBack :
                                callBacks) {
//                            Log.i("handleMessage", "---------" + message.toString());
                            callBack.postMessage(message);
                        }
                    }
                    break;
                case 2:
                    String string = (String) msg.obj;
                    if (callBacks != null && callBacks.size() > 0) {
                        for (CallBack callBack :
                                callBacks) {
                            callBack.postConnectMessage(string);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 提供给activity的接口 因为存在一个服务绑定多个activity的情况 所以监听接口采用list装起来
     */

    public interface CallBack {
        void postMessage(ServiceTcpBean message);

        void postConnectMessage(String info);
    }

    //    private List<CallBack> callBacks = new LinkedList<>();
    private List<CallBack> callBacks = new LinkedList<>();

    //注册接口
    public void registerCallBack(CallBack callBack) {
        if (callBacks != null) {
            callBacks.add(callBack);
        }
    }

    /**
     * 注销接口 false注销失败
     *
     * @param callBack
     * @return
     */
    public boolean unRegisterCallBack(CallBack callBack) {
        if (callBacks != null && callBacks.contains(callBack)) {
            return callBacks.remove(callBack);
        }
        return false;
    }
}
