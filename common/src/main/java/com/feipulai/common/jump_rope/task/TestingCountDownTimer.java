package com.feipulai.common.jump_rope.task;


import com.feipulai.common.utils.SoundPlayUtils;

/**
 * Created by James on 2018/8/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class TestingCountDownTimer extends PreciseCountDownTimer {

    private final OnTestingCountDownListener listener;
    private int countFinishTime;

    public TestingCountDownTimer(long millisInFuture, long interval, int delay, int countFinishTime, OnTestingCountDownListener listener) {
        super(millisInFuture, interval, delay);
        this.listener = listener;
        this.countFinishTime = countFinishTime;
    }

    @Override
    public void onTick(long tick) {
        if (tick <= 5 && tick <= countFinishTime) {
            SoundPlayUtils.play((int) tick);
        }
        if (SoundPlayUtils.voiceSetting.isTimeBroadcast() && (int) millisInFuture / 1000 != (int) tick) {

            switch ((int) tick) {
                case 30:

                    SoundPlayUtils.play(20);
                    break;
                case 20:
                    SoundPlayUtils.play(19);
                    break;
                case 10:
                    SoundPlayUtils.play(18);
                    break;
            }
        }
        if (listener != null) {
            listener.onTick(tick);
        }
    }

    @Override
    public void onFinish() {
        if (SoundPlayUtils.voiceSetting.getVoiceMode() == 0) {
            SoundPlayUtils.play(12);
        } else if (SoundPlayUtils.voiceSetting.getVoiceMode() == 1) {
            SoundPlayUtils.play(17);
        } else {
            SoundPlayUtils.play(23);
        }

        if (listener != null) {
            listener.onTick(0);
        }

    }

    public interface OnTestingCountDownListener {

        void onTick(long tick);

    }

}
