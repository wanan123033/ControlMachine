package com.feipulai.exam.activity.basketball;

import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.config.TestConfigs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zzs on  2019/11/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GetStateFacade {


    private ExecutorService mExecutor;
    private GetDeviceStatesTask mGetDeviceStatesTask;

    public GetStateFacade(final BasketBallRadioFacade.OnGettingDeviceStatesListener listener) {
        mExecutor = Executors.newFixedThreadPool(2);
        //运行两个线程,分别处理获取设备状态和LED检录显示
        mGetDeviceStatesTask = new GetDeviceStatesTask(new GetDeviceStatesTask.OnGettingDeviceStatesListener() {
            @Override
            public void onGettingState(int position) {
                listener.onGettingState(position);
            }

            @Override
            public void onStateRefreshed() {
                listener.onStateRefreshed();
            }

            @Override
            public int getDeviceCount() {
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
                    return 2;
                }
                return 3;
            }
        });

        // 开始之前先全部不动,等待开始
        pause();
        mExecutor.execute(mGetDeviceStatesTask);
    }

    public void pause() {
        mGetDeviceStatesTask.pause();
    }

    public void resume() {
        mGetDeviceStatesTask.resume();
    }


    public void finish() {
        mGetDeviceStatesTask.finish();
        mExecutor.shutdownNow();
    }
}
