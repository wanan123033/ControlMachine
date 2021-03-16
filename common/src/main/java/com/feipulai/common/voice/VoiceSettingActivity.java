package com.feipulai.common.voice;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.feipulai.common.R;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;

/**
 * Created by zzs on  2021/2/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VoiceSettingActivity extends Activity {
    private TextView tvBack;
    private RadioGroup rgType;
    private RadioButton rbWomen;
    private RadioButton rbMan;
    private RadioGroup rgMode;
    private RadioButton rbMode1;
    private RadioButton rbMode2;
    private Switch swBroadcast;

    private VoiceSetting voiceSetting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_setting);
        tvBack = findViewById(R.id.txt_back);
        rgType = findViewById(R.id.rg_select_voice);
        rbWomen = findViewById(R.id.rb_voice_women);
        rbMan = findViewById(R.id.rb_voice_men);
        rgMode = findViewById(R.id.rg_mode);
        rbMode1 = findViewById(R.id.rb_mode1);
        rbMode2 = findViewById(R.id.rb_mode2);
        swBroadcast = findViewById(R.id.sw_broadcast);

        voiceSetting = SharedPrefsUtil.loadFormSource(this, VoiceSetting.class);
        if (voiceSetting == null) {
            voiceSetting = new VoiceSetting();
        }

        rgType.check(voiceSetting.getVoiceType() == 0 ? R.id.rb_voice_women : R.id.rb_voice_men);
        rgMode.check(voiceSetting.getVoiceMode() == 0 ? R.id.rb_mode1 : R.id.rb_mode2);
        swBroadcast.setChecked(voiceSetting.isTimeBroadcast());
        setEvent();
    }

    private void setEvent() {
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_voice_women) {
                    voiceSetting.setVoiceType(0);
                } else {
                    voiceSetting.setVoiceType(1);
                }
                SharedPrefsUtil.save(VoiceSettingActivity.this, voiceSetting);
                SoundPlayUtils.releaseSoundPool();
                SoundPlayUtils.init(VoiceSettingActivity.this);
                SoundPlayUtils.play(1);
            }
        });
        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_mode1) {
                    voiceSetting.setVoiceMode(0);
                    SoundPlayUtils.voiceSetting.setVoiceMode(0);
                    SoundPlayUtils.play(11);
                } else {
                    voiceSetting.setVoiceMode(1);
                    SoundPlayUtils.voiceSetting.setVoiceMode(1);
                    SoundPlayUtils.play(17);
                }

            }
        });
        swBroadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                voiceSetting.setTimeBroadcast(isChecked);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPrefsUtil.save(this, voiceSetting);
    }
}
