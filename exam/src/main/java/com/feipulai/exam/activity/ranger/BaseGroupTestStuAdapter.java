package com.feipulai.exam.activity.ranger;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;

import java.util.List;

/**
 * 分组学生适配器
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseGroupTestStuAdapter extends BaseQuickAdapter<BaseStuPair, BaseViewHolder> {
    /**
     * 测试位
     */
    private int testPosition = -1;

    public void setTestPosition(int testPosition) {
        this.testPosition = testPosition;
    }

    public int getTestPosition() {
        return testPosition;
    }

    public BaseGroupTestStuAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.item_group_test_stu, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BaseStuPair pair) {
        helper.setText(R.id.item_txt_stu_code, pair.getStudent().getStudentCode());
        helper.setText(R.id.item_txt_stu_name, pair.getStudent().getStudentName());
//        CheckBox cbDeviceState = helper.getView(R.id.item_cb_device_state);
        helper.setText(R.id.item_trackno, pair.getTrackNo() + "");
        ImageView imgPortrait = helper.getView(R.id.item_img_portrait);
        if (testPosition == helper.getLayoutPosition()) {
            helper.setBackgroundRes(R.id.view_content, R.drawable.group_select_bg);
        } else {
            helper.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.white));
        }
//        if (TextUtils.isEmpty(pair.getStudent().getPortrait())) {
//            helper.setImageResource(R.id.item_img_portrait, R.mipmap.icon_head_photo);
//        } else {
//            helper.setImageBitmap(R.id.item_img_portrait, pair.getStudent().getBitmapPortrait());
//        }
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + pair.getStudent().getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
//        helper.addOnClickListener(R.id.view_check);
//        if (testPosition == helper.getLayoutPosition()) {
//            cbDeviceState.setVisibility(View.VISIBLE);
//            if (pair.getBaseDevice().getResultState() != BaseDeviceState.STATE_ERROR) {
//                cbDeviceState.setChecked(true);
//                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LDTY) {
//                    cbDeviceState.setVisibility(View.INVISIBLE);
//                }
//            } else {
//                cbDeviceState.setChecked(false);
//            }
//        } else {
//            cbDeviceState.setVisibility(View.INVISIBLE);
//        }


//        RecyclerView rvResult = helper.getView(R.id.item_rv_result);
//        rvResult.setLayoutManager(new LinearLayoutManager(mContext));
//        rvResult.setAdapter(new BaseGroupTestResultAdapter(Arrays.asList(pair.getTimeResult())));
    }
}
