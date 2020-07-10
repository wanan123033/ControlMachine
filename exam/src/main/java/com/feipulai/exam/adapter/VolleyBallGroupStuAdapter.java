package com.feipulai.exam.adapter;

import android.media.Image;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;

import java.util.List;

/**
 * 分组学生适配器
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VolleyBallGroupStuAdapter extends BaseQuickAdapter<StuDevicePair, BaseViewHolder> {

    private /*volatile*/ int testPosition;

    public VolleyBallGroupStuAdapter(@Nullable List<StuDevicePair> data) {
        super(R.layout.item_group_test_stu, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, StuDevicePair pair) {
        helper.setText(R.id.item_txt_stu_code, pair.getStudent().getStudentCode());
        helper.setText(R.id.item_txt_stu_name, pair.getStudent().getStudentName());
        ImageView imgPortrait=helper.getView(R.id.item_img_portrait);
//        if (TextUtils.isEmpty(pair.getStudent().getPortrait())) {
//            helper.setImageResource(R.id.item_img_portrait, R.mipmap.icon_head_photo);
//        } else {
//            helper.setImageBitmap(R.id.item_img_portrait, pair.getStudent().getBitmapPortrait());
//        }
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE +  pair.getStudent().getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
        helper.setText(R.id.item_trackno, TestCache.getInstance().getTrackNoMap().get(pair.getStudent()) + "");
        if (testPosition == helper.getLayoutPosition()) {
            helper.setBackgroundRes(R.id.view_content, R.drawable.group_select_bg);
        } else {
            helper.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.white));
        }
    }

    public void setTestPosition(int testPosition) {
        this.testPosition = testPosition;
    }

    public int getTestPosition() {
        return testPosition;
    }

}
