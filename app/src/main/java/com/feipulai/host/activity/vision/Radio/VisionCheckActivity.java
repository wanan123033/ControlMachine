package com.feipulai.host.activity.vision.Radio;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseCheckActivity;
import com.feipulai.host.entity.Student;

/**
 * Created by zzs on  2020/9/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionCheckActivity extends BaseCheckActivity {


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_person_test;
    }

    @Override
    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }
    @Override
    public void onCheckIn(Student student) {

    }
}
