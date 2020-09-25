package com.feipulai.host.activity.vision.Radio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.common.utils.GetJsonDataUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.view.StuSearchEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 视力
 * Created by zzs on  2020/9/15
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionFreedomTestActivity extends BaseTitleActivity {

    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.iv_e)
    ImageView ivE;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.txt_left_result)
    TextView txtLeftResult;
    @BindView(R.id.txt_right_result)
    TextView txtRightResult;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.frame_camera)
    FrameLayout frameCamera;
    private VisionBean visionBean;
    private int index;
    private ArrayList<VisionBean> visionBeans;
    private int direction;
    private Random random = new Random();

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = String.format(getString(R.string.host_name), TestConfigs.machineNameMap.get(machineCode) + "(自由测试)", SettingHelper.getSystemSetting().getHostId());
        return builder.setTitle(title).addRightText(R.string.item_setting_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visionBean = visionBeans.get(2);
                index = 0;
                setImageWidth(visionBean.getVisions().get(index));
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_vision_test;
    }

    @Override
    protected void initData() {
        String JsonData = GetJsonDataUtil.getJson(this, "vision.json");//获取assets目录下的json文件数据
        Type type = new TypeToken<List<VisionBean>>() {
        }.getType();
        visionBeans = new Gson().fromJson(JsonData, type);

        visionBean = visionBeans.get(0);


        setImageWidth(visionBean.getVisions().get(index));
    }

    private void setImageWidth(VisionBean.VisionData visionData) {

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ivE.getLayoutParams();
        layoutParams.width = visionData.geteDP();
        layoutParams.height = visionData.geteDP();
        ivE.setLayoutParams(layoutParams);
        ivE.setVisibility(View.VISIBLE);
        direction = random.nextInt(4) + 1;
        LogUtil.logDebugMessage("方向为：" + direction);
        float toDegrees = 0;
        switch (direction) {
            case 1://左
                toDegrees = 0;
                break;
            case 2://下
                toDegrees = 90;
                break;
            case 3://右
                toDegrees = 180;
                break;
            case 4://上
                toDegrees = 270;
                break;
        }
        Animation animation = new RotateAnimation(0, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);//设置为true，动画转化结束后被应用
        ivE.startAnimation(animation);//開始动画
    }


    @OnClick({R.id.img_AFR, R.id.txt_led_setting, R.id.tv_device_pair, R.id.txt_start_test, R.id.txt_stu_skip, R.id.tv_foul})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_AFR:
                if (index != visionBean.getVisions().size() - 1) {
                    index += 1;
                }
                setImageWidth(visionBean.getVisions().get(index));
                break;
            case R.id.txt_led_setting:
                if (index != 0) {
                    index -= 1;
                }
                setImageWidth(visionBean.getVisions().get(index));
                break;
            case R.id.tv_device_pair:
                visionBean = visionBeans.get(1);
                index = 0;
                setImageWidth(visionBean.getVisions().get(index));
                break;
            case R.id.txt_start_test:
                break;
            case R.id.txt_stu_skip:
                break;
            case R.id.tv_foul:
                break;
        }
    }
}
