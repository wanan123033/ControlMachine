package com.feipulai.exam.activity.sargent_jump.more_device;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.SelectResult;
import com.feipulai.exam.activity.person.adapter.BaseGroupTestStuAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Group;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BaseGroupMoreOneView extends RelativeLayout {
    @BindView(R.id.rvTestStu)
    RecyclerView rvTestStu;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;
    @BindView(R.id.tv_get_data)
    TextView tvGetData;
    private Context mContext;
    public BaseGroupTestStuAdapter stuAdapter;
    private Group group;
    private List<BaseStuPair> pairList;
    private OnClickListener listener;


    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    private boolean showGetData;

    public void setShowGetData(boolean showGetData) {
        this.showGetData = showGetData;
        if (showGetData) {
            tvGetData.setVisibility(View.VISIBLE);
        } else {
            tvGetData.setVisibility(View.GONE);
        }
    }

    public BaseGroupMoreOneView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public BaseGroupMoreOneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_base_group_device_one, null);
        ButterKnife.bind(this, view);
        addView(view);
        group = (Group) TestConfigs.baseGroupMap.get("group");

        if (group != null) {
            StringBuffer sbName = new StringBuffer();
            sbName.append(group.getGroupType() == Group.MALE ? "男子" :
                    (group.getGroupType() == Group.FEMALE ? "女子" : "男女混合"));
            sbName.append(group.getSortName() + String.format("第%1$d组", group.getGroupNo()));
            txtGroupName.setText(sbName);
        } else {
            txtGroupName.setVisibility(View.GONE);
        }
        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setEnabled(true);
            tvResurvey.setVisibility(View.VISIBLE);
        } else {
            tvResurvey.setVisibility(View.GONE);
        }
    }

    public void setData(List<BaseStuPair> pairList) {
        this.pairList = pairList;
        stuAdapter = new BaseGroupTestStuAdapter(pairList);
        rvTestStu.setLayoutManager(new LinearLayoutManager(mContext));
        rvTestStu.setAdapter(stuAdapter);


    }

    public void setTestPosition(int testPosition) {
        stuAdapter.setTestPosition(testPosition);
    }

    public int getTestPosition() {
        return stuAdapter.getTestPosition();
    }

    public int getSaveLayoutSeletePosition() {
        return stuAdapter.getSaveLayoutSeletePosition();
    }


    public int getSaveSeletePosition() {
        return stuAdapter.getSaveSeletePosition();
    }

//    public void indexStuTestResult(final int stuIndex, final int positioin) {
//        rvTestStu.scrollToPosition(stuIndex);
//        stuAdapter.indexStuTestResult(stuIndex, positioin);
//        indexStuTestResultSelect(stuIndex,positioin);
//    }
//
//    public void indexStuTestResultSelect(final int stuIndex, final int positioin) {
//        stuAdapter.indexStuTestResultSelect(stuIndex, positioin);
//    }

    public void notifyDataSetChanged() {
        stuAdapter.notifyDataSetChanged();
        if (pairList.size() > 0) {
            if (pairList.get(0).getBaseDevice().getState() != BaseDeviceState.STATE_ERROR ||
                    pairList.get(0).getBaseDevice().getState() == 0) {
                cbDeviceState.setChecked(true);
            } else {
                cbDeviceState.setChecked(false);

            }
        }
    }

    public void indexStuTestResult(final int stuIndex, final int positioin) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvTestStu.scrollToPosition(stuIndex);
                if (pairList.get(stuIndex).getSelectResultList() != null) {
                    for (int i = 0; i < pairList.size(); i++) {
                        List<SelectResult> selectResultList = pairList.get(i).getSelectResultList();
                        if (selectResultList != null) {
                            if (i == stuIndex) {
                                for (int j = 0; j < selectResultList.size(); j++) {
                                    if (j == positioin) {
                                        selectResultList.get(j).setIndex(true);
                                    } else {
                                        selectResultList.get(j).setIndex(false);
                                    }
                                }
                            } else {
                                for (SelectResult result : selectResultList) {
                                    result.setIndex(false);
                                }

                            }
                        }

                    }
                    stuAdapter.notifyDataSetChanged();
                }

            }
        }, 1000);
        indexStuTestResultSelect(stuIndex, positioin);

    }

    public void indexStuTestResultSelect(final int stuIndex, final int positioin) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pairList.get(stuIndex).getSelectResultList() != null) {
                    for (int i = 0; i < pairList.size(); i++) {
                        if (pairList.get(i).getSelectResultList() != null) {
                            if (i == stuIndex) {
                                for (int j = 0; j < pairList.get(i).getSelectResultList().size(); j++) {
                                    if (j == positioin) {
                                        pairList.get(i).getSelectResultList().get(j).setSelect(true);
                                        stuAdapter.setSeletePosition(stuIndex, positioin);
                                    } else {
                                        pairList.get(i).getSelectResultList().get(j).setSelect(false);
                                    }
                                }
                            } else {
                                for (SelectResult result : pairList.get(i).getSelectResultList()) {
                                    result.setSelect(false);
                                }

                            }
                        }

                    }
                }
                stuAdapter.notifyDataSetChanged();
            }
        }, 50);


    }

    @OnClick({R.id.tv_foul, R.id.tv_normal, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_resurvey, R.id.txt_start_test,
            R.id.txt_stu_skip, R.id.tv_get_data})
    public void onViewClicked(View view) {
        if (listener != null) {
            listener.onClick(view);
        }

    }

}
