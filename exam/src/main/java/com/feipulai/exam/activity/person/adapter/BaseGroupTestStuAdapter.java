package com.feipulai.exam.activity.person.adapter;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.SelectResult;
import com.feipulai.exam.entity.RoundResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 分组学生适配器
 * Created by zzs on 2018/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseGroupTestStuAdapter extends BaseQuickAdapter<BaseStuPair, BaseGroupTestStuAdapter.ViewHolder> {
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

    private int saveLayoutSeletePosition = 0;
    private int saveSeletePosition = 0;

    public int getSaveLayoutSeletePosition() {
        return saveLayoutSeletePosition;
    }

    public void setSaveLayoutSeletePosition(int saveLayoutSeletePosition) {
        this.saveLayoutSeletePosition = saveLayoutSeletePosition;
    }

    public int getSaveSeletePosition() {
        return saveSeletePosition;
    }

    public void setSaveSeletePosition(int saveSeletePosition) {
        this.saveSeletePosition = saveSeletePosition;
    }

    public void setSeletePosition(int layoutPosition, int position) {
        saveLayoutSeletePosition = layoutPosition;
        saveSeletePosition = position;
    }

    public BaseGroupTestStuAdapter(@Nullable List<BaseStuPair> data) {
        super(R.layout.item_group_test_stu2, data);
    }

    @Override
    protected void convert(final ViewHolder helper, BaseStuPair pair) {
        helper.setText(R.id.item_txt_stu_code, pair.getStudent().getStudentCode());
        helper.setText(R.id.item_txt_stu_name, pair.getStudent().getStudentName());
        helper.setText(R.id.item_trackno, pair.getTrackNo() + "");
        ImageView imgPortrait = helper.getView(R.id.item_img_portrait);
        if (testPosition == helper.getLayoutPosition()) {
            helper.setBackgroundRes(R.id.view_content, R.drawable.group_select_blue_bg);
        } else {
            helper.setBackgroundColor(R.id.view_content, ContextCompat.getColor(mContext, R.color.white));
        }

        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + pair.getStudent().getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);
//        List<SelectResult> data;
//        if (pair.getSelectResultList() == null) {
//            data = new ArrayList<>(SelectResult.copeList(helper.list, pair.getTimeResult()));
//        } else {
//            data = new ArrayList<>(SelectResult.copeList(pair.getSelectResultList(), pair.getTimeResult()));
//        }

        helper.list.clear();
        helper.list.addAll(pair.getSelectResultList());
//        pair.setSelectResultList(data);
        if (helper.adapter == null) {
            helper.rvResult.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
            helper.adapter = new BaseGroupTestResultAdapter(helper.list);
            helper.rvResult.setAdapter(helper.adapter);
            helper.adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                    indexStuTestResultSelect(helper.getLayoutPosition(), position);
                    setSeletePosition(helper.getLayoutPosition(), position);
                }
            });
        } else {
            helper.adapter.notifyDataSetChanged();
        }
    }


    public void indexStuTestResult(final int stuIndex, final int positioin) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BaseStuPair> pairList = getData();
                if (pairList.size() > stuIndex &&pairList.get(stuIndex).getSelectResultList() != null) {
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
                    notifyDataSetChanged();
                }

            }
        }, 1000);
        indexStuTestResultSelect(stuIndex, positioin);
    }

    public void indexStuTestResultSelect(final int stuIndex, final int positioin) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<BaseStuPair> pairList = getData();
                if (pairList.size() > stuIndex && pairList.get(stuIndex).getSelectResultList() != null) {
                    for (int i = 0; i < pairList.size(); i++) {
                        if (pairList.get(i).getSelectResultList() != null) {
                            if (i == stuIndex) {
                                for (int j = 0; j < pairList.get(i).getSelectResultList().size(); j++) {
                                    if (j == positioin) {
                                        pairList.get(i).getSelectResultList().get(j).setSelect(true);
                                        setSeletePosition(stuIndex, positioin);
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
                notifyDataSetChanged();
            }
        }, 50);


    }

    static class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_txt_stu_code)
        TextView txt_stu_code;
        @BindView(R.id.item_txt_stu_name)
        TextView txt_stu_name;
        @BindView(R.id.item_trackno)
        TextView txtTrackno;
        @BindView(R.id.item_img_portrait)
        ImageView img_portrait;
        @BindView(R.id.item_rv_result)
        RecyclerView rvResult;
        BaseGroupTestResultAdapter adapter;
        List<SelectResult> list = new ArrayList<>();

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
