package com.ww.fpl.videolibrary.play;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.WindowManager;

import com.kk.taurus.playerbase.assist.OnVideoViewEventHandler;
import com.kk.taurus.playerbase.entity.DataSource;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.kk.taurus.playerbase.window.FloatWindowParams;
import com.kk.taurus.playerbase.window.WindowVideoView;
import com.ww.fpl.videolibrary.play.cover.CloseCover;
import com.ww.fpl.videolibrary.play.play.DataInter;
import com.ww.fpl.videolibrary.play.play.ReceiverGroupManager;
import com.ww.fpl.videolibrary.play.util.WindowPermissionCheck;

/**
 * created by ww on 2019/12/23.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VideoPlayWindow {
    private Context mContext;
    public WindowVideoView mWindowVideoView;

    public VideoPlayWindow(Context context) {
        mContext = context;
    }

    /**
     * 初始化窗口播放
     *
     * @param handler
     * @return
     */
    public void initVideoWindow(OnVideoViewEventHandler handler) {
        int SW = mContext.getResources().getDisplayMetrics().widthPixels;
        int width = (int) (SW * 0.7f);
        int height = width * 9 / 16;

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0+
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        mWindowVideoView = new WindowVideoView(mContext,
                new FloatWindowParams()
                        .setWindowType(type)
                        .setX(100)
                        .setY(100)
                        .setWidth(width)
                        .setHeight(height));

        mWindowVideoView.setBackgroundColor(Color.BLACK);

        mWindowVideoView.setEventHandler(handler);

        ReceiverGroup receiverGroup = ReceiverGroupManager.get().getLiteReceiverGroup(mContext);
        receiverGroup.addReceiver(DataInter.ReceiverKey.KEY_CLOSE_COVER, new CloseCover(mContext));
        receiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_NETWORK_RESOURCE, true);
        receiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, false);
        receiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_SCREEN_SWITCH_ENABLE, false);

        mWindowVideoView.setReceiverGroup(receiverGroup);
    }

    public void activeWindowVideoView(Activity activity, DataSource dataSource, int seekTime) {
        if (mWindowVideoView.isWindowShow()) {
            mWindowVideoView.close();
        } else {
            if (WindowPermissionCheck.checkPermission(activity)) {
                mWindowVideoView.setElevationShadow(20);
                mWindowVideoView.show();
                mWindowVideoView.setDataSource(dataSource);
                if (seekTime > 0) {
                    mWindowVideoView.start(seekTime);
                } else {
                    mWindowVideoView.start();
                }
            }
        }
    }

    public void clear() {
        if (mWindowVideoView != null) {
            mWindowVideoView.close();
            mWindowVideoView.stopPlayback();
            mWindowVideoView = null;
        }
    }

}
