package com.feipulai.host.activity.main;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.main.fragment.FiancoSelectFragment;
import com.feipulai.host.activity.main.fragment.FreedomSelectFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MachineSelectActivity extends BaseTitleActivity {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<>();
    private String[] titles;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_machine_select;
    }

    @Override
    protected void initData() {
        titles = getResources().getStringArray(R.array.test_select);
        fragments.add(new FiancoSelectFragment());
        fragments.add(new FreedomSelectFragment());
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {

                return titles[position];
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.machine_select_title);
    }


}
