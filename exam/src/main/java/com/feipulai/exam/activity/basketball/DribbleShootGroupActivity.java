package com.feipulai.exam.activity.basketball;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.adapter.BasketBallResultAdapter;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Group;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class DribbleShootGroupActivity extends BaseTitleActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.cb_near)
    CheckBox cbNear;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.tv_punish_add)
    TextView tvPunishAdd;
    @BindView(R.id.tv_punish_subtract)
    TextView tvPunishSubtract;
    @BindView(R.id.tv_foul)
    TextView tvFoul;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.tv_normal)
    TextView tvNormal;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.view_list_head)
    LinearLayout viewListHead;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dribble_shoot_group);
//    }
    private ShootSetting setting;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private DribbleShootResultAdapter resultAdapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_dribble_shoot_group;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        if (setting == null)
            setting = new ShootSetting();

        //分组标题
        group = (Group) TestConfigs.baseGroupMap.get("group");
        String type = "男女混合";
        if (group.getGroupType() == Group.MALE) {
            type = "男子";
        } else if (group.getGroupType() == Group.FEMALE) {
            type = "女子";
        }
        tvGroupName.setText(String.format(Locale.CHINA, "%s第%d组", type, group.getGroupNo()));

        //获取分组学生数据
        TestCache.getInstance().init();
        pairs = CheckUtils.newPairs(((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu")).size());
        LogUtils.operation("篮球获取到分组学生:" + pairs.size() + "---" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        resultAdapter = new DribbleShootResultAdapter(resultList);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                if (!isConfigurableNow()) {
//                    resultAdapter.setSelectPosition(position);
//                    resultAdapter.notifyDataSetChanged();
//                }
            }
        });
    }

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {

    }
}
