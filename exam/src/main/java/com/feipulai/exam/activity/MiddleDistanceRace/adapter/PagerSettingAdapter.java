package com.feipulai.exam.activity.MiddleDistanceRace.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * created by ww on 2019/6/24.
 */
public class PagerSettingAdapter extends FragmentPagerAdapter {
    private final List<Fragment> frags;

    public PagerSettingAdapter(FragmentManager fm, List<Fragment> frags) {
        super(fm);
        this.frags = frags;
    }

    @Override
    public Fragment getItem(int position) {
        return frags.get(position);
    }

    @Override
    public int getCount() {
        return frags.size();
    }
}
