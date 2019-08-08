package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.server.MySocketServer;
import com.feipulai.exam.activity.MiddleDistanceRace.server.OnResponseListener;
import com.feipulai.exam.activity.MiddleDistanceRace.server.WebConfig;
import com.feipulai.exam.entity.Schedule;

import java.io.IOException;

public class TcpService extends Service {

    public static final String TAG = "MyService";

    private MyBinder mBinder = new MyBinder();
    private MySocketServer mySocketServer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() executed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {

        public void startServer(int port, OnResponseListener listener) {
//            this.listener = listener;
            WebConfig webConfig = new WebConfig();
            webConfig.setPort(port);
            webConfig.setMaxParallels(10);
            if (listener == null) {
                listener = MiddleDistanceRaceActivity.instance;
            }
            mySocketServer = new MySocketServer(webConfig, listener);
            mySocketServer.startServerAsync();
        }

        public void stopServer() {
            if (mySocketServer != null) {
                try {
                    mySocketServer.listener = null;
                    mySocketServer.stopServerAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
