package com.feipulai.exam.activity.MiddleDistanceRace;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MiddleDistanceRace.adapter.PagerSettingAdapter;
import com.feipulai.exam.activity.base.BaseTitleActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MiddleRaceSettingActivity extends BaseTitleActivity {

    @BindView(R.id.vp_middle_race_setting)
    ViewPager vpMiddleRaceSetting;
    @BindView(R.id.rg_middle_race_setting)
    RadioGroup rgMiddleRaceSetting;
    @BindView(R.id.rb_basic)
    RadioButton rbBasic;
    @BindView(R.id.rb_chip)
    RadioButton rbChip;
    @BindView(R.id.rb_other)
    RadioButton rbOther;

    private int[] rbs = {R.id.rb_basic, R.id.rb_chip, R.id.rb_other};
    private List<Fragment> mFragments;
    private int schedulePosition;
    private int mItemPosition;
    private int groupStatePosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        schedulePosition = intent.getIntExtra("schedulePosition", 0);
        mItemPosition = intent.getIntExtra("mItemPosition", 0);
        groupStatePosition = intent.getIntExtra("groupStatePosition", 0);
        initListener();
    }

    public boolean isChange = false;

    public boolean isChange() {
        return isChange;
    }

    public void setChange(boolean change) {
        isChange = change;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull final BaseToolbar.Builder builder) {
        return builder.setTitle("设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quiet();
            }
        });
    }

    private void quiet() {
        if (isChange) {
            MiddleDistanceRaceActivity.instance.finish();
            Bundle bundle = new Bundle();
            bundle.putInt("schedulePosition", schedulePosition);
            bundle.putInt("mItemPosition", mItemPosition);
            bundle.putInt("groupStatePosition", groupStatePosition);
            Intent intent = new Intent(MiddleRaceSettingActivity.this, MiddleDistanceRaceActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        quiet();
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_middle_race_setting;
    }

    private void initListener() {
        //radioGroup的点击事件
        rgMiddleRaceSetting.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                for (int i = 0; i < rbs.length; i++) {
                    if (rbs[i] != checkedId) continue;
                    //加载滑动
                    vpMiddleRaceSetting.setCurrentItem(i);
                }
            }
        });
        //ViewPager的点击事件 vp-rg互相监听：vp
        vpMiddleRaceSetting.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                rgMiddleRaceSetting.check(rbs[position]);
            }
        });
        //设置一个默认页
        rgMiddleRaceSetting.check(rbs[0]);
    }


    @Override
    protected void initData() {
        mFragments = new ArrayList<>();
        BaseSettingFragment baseSettingFragment = new BaseSettingFragment();
        ChipSettingFragment chipSettingFragment = new ChipSettingFragment();
        OtherSettingFragment otherSettingFragment = new OtherSettingFragment();
        mFragments.add(baseSettingFragment);
        mFragments.add(chipSettingFragment);
        mFragments.add(otherSettingFragment);

        // 设置填充器
        vpMiddleRaceSetting.setAdapter(new PagerSettingAdapter(getSupportFragmentManager(), mFragments));
        // 设置缓存页面数
        vpMiddleRaceSetting.setOffscreenPageLimit(2);
    }

}
