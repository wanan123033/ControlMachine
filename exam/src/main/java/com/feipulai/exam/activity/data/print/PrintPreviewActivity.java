package com.feipulai.exam.activity.data.print;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.print.PrintA4Util;
import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.common.utils.print.ViewImageUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.utils.HpPrintManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on  2020/8/14
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintPreviewActivity extends BaseTitleActivity {

    @BindView(R.id.view_content)
    LinearLayout viewContent;
    private PrintA4Util printUtil;
    String fileName;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_print_preview;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("打印预览");
    }

    @Override
    protected void initData() {


        PrintBean printBean = new PrintBean();
        printBean.setTitle("2020年深圳初中毕业体育考试");
        printBean.setPrintHand("男子100米第一组");
        printBean.setPrintHandRight("开始时间：" + DateUtil.getCurrentTime("yyyy/MM/dd  HH:mm:ss"));
        printBean.setCodeData("123456789");

        List<PrintBean.PrintDataBean> printDataBeans = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
//            printDataBeans.add(new PrintBean.PrintDataBean((i + 1) + "", "20001001010" + i, "学生姓名" + i, "学校" + i, "成绩" + i));

        }
        printBean.setPrintDataBeans(printDataBeans);
        fileName = DateUtil.getCurrentTime() + "";
        printUtil = new PrintA4Util(this);
        printUtil.createPrintFile(printBean, MyApplication.PATH_PDF_IMAGE, fileName);

//        for (Bitmap bitmap : printUtil.getPrintImgList()) {
//            ImageView imageView = new ImageView(this);
//            imageView.setImageBitmap(bitmap);
//            imageView.setScaleType(ImageView.ScaleType.FIT_START);
////            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////            params.setMargins(0, 0, 0, 30);
//            viewContent.addView(imageView);
//        }
        HpPrintManager.getInstance(this).print(MyApplication.PATH_PDF_IMAGE + fileName + ".pdf");
    }


    @OnClick(R.id.print_preview_print_btn)
    public void onViewClicked() {

        HpPrintManager.getInstance(this).print(MyApplication.PATH_PDF_IMAGE + fileName + ".pdf");
    }
}
