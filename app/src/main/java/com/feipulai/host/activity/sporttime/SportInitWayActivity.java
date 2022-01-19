package com.feipulai.host.activity.sporttime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.sporttime.adapter.InitDeviceAdapter;
import com.feipulai.host.activity.sporttime.adapter.InitRouteAdapter;
import com.feipulai.host.activity.sporttime.bean.InitRoute;
import com.feipulai.host.config.TestConfigs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class SportInitWayActivity extends BaseTitleActivity {
    @BindView(R.id.rv_device_lists)
    RecyclerView rvDeviceLists;
    @BindView(R.id.rv_init_way)
    RecyclerView rvInitRoute;
    SportTimerSetting setting;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.tv_change)
    TextView tvChange;
    @BindView(R.id.tv_remove)
    TextView tvRemove;
    private List<String> devices;
    private InitDeviceAdapter initDeviceAdapter;
    private List<InitRoute> initRoutes;//路线
    private InitRouteAdapter routeAdapter;
    private int selectRoute;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_init_way;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        initRoutes = new ArrayList<>();
        devices = new ArrayList<>();
        for (int i = 0; i < setting.getDeviceCount(); i++) {
            devices.add(String.format(Locale.CHINA, "子机%d", i + 1));
            InitRoute initRoute = new InitRoute();
            initRoute.setIndex(i + 1);
            initRoutes.add(initRoute);
        }
        initRoutes.get(0).setDeviceName(String.format(Locale.CHINA, "子机%d", 1));//路线第一个定死为1号子机，后面的改动
        rvDeviceLists.setLayoutManager(new LinearLayoutManager(this));
        initDeviceAdapter = new InitDeviceAdapter(devices);
        rvDeviceLists.setAdapter(initDeviceAdapter);
        initDeviceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (selectRoute == 0 && TextUtils.isEmpty(initRoutes.get(selectRoute).getDeviceName())){
                    ToastUtils.showShort("路线1只能是1号子机");
                    return;
                }
                selectRoute++;
                if (selectRoute >= initRoutes.size()) {
                    InitRoute initRoute = new InitRoute();
                    initRoute.setIndex(selectRoute + 1);
                    initRoutes.add(initRoute);
                }
                initRoutes.get(selectRoute-1).setIndex(position+1);
                routeAdapter.setSelectPosition(selectRoute);
                initRoutes.get(selectRoute - 1).setDeviceName(devices.get(position));
                routeAdapter.notifyDataSetChanged();
            }
        });

        String route = setting.getInitRoute();
        if (!TextUtils.isEmpty(route)) {
            Gson gson = new Gson();
            initRoutes = gson.fromJson(route, new TypeToken<List<InitRoute>>() {
            }.getType());
        }else {
            if (initRoutes.size() > 1) {
                selectRoute = 1;
            } else {
                InitRoute initRoute = new InitRoute();
                initRoute.setIndex(2);
                initRoutes.add(initRoute);
            }
        }
        routeAdapter = new InitRouteAdapter(initRoutes);
        rvInitRoute.setAdapter(routeAdapter);
        rvInitRoute.setLayoutManager(new GridLayoutManager(this, 4));
        routeAdapter.setSelectPosition(selectRoute);
        routeAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == 0){
                    ToastUtils.showShort("路线1只能是1号子机");
                    return;
                }
                selectRoute = position;
                routeAdapter.setSelectPosition(selectRoute);
                routeAdapter.notifyDataSetChanged();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title);
    }


    @OnClick({R.id.tv_confirm, R.id.tv_change, R.id.tv_remove})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                if (TextUtils.isEmpty(initRoutes.get(selectRoute).getDeviceName())){
                    initRoutes.remove(selectRoute);
                    selectRoute--;
//                    if (selectRoute == (initRoutes.size()-1)){
//                        selectRoute--;
//                    }
                    routeAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.tv_change:
                if (selectRoute > 0){
                    initRoutes.get(selectRoute).setDeviceName("");
                    tvRemove.setEnabled(false);
                    tvConfirm.setEnabled(false);
                    routeAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.tv_remove:
                if (selectRoute>1 ){
                    initRoutes.remove(selectRoute);
                    selectRoute--;
                }
                routeAdapter.notifyDataSetChanged();
                break;
        }
    }



    @Override
    protected void onDestroy() {
        Gson gson = new Gson();
        String jsonStr =gson.toJson(initRoutes);
        setting.setInitRoute(jsonStr);
        SharedPrefsUtil.save(this,setting);
        super.onDestroy();
    }
}
