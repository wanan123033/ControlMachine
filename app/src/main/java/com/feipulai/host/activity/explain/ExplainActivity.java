package com.feipulai.host.activity.explain;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 说明文档列表
 * Created by zzs on  2019/11/21
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ExplainActivity extends BaseTitleActivity {

    @BindView(R.id.rv_explain)
    RecyclerView rvExplain;
    private List<File> fileList = new ArrayList<>();
    private ExplainAdapter adapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_explain;
    }

    @Override
    protected void initData() {

        File file = new File(MyApplication.PATH_SPECIFICATION);
        File[] files = file.listFiles();
        for (File itemFile : files) {
            if (itemFile.getName().contains(".pdf")) {
                fileList.add(itemFile);
            }
        }

        adapter = new ExplainAdapter(fileList);
        rvExplain.setLayoutManager(new LinearLayoutManager(this));
        rvExplain.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvExplain.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                try {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    //若有，则在该Task上创建Activity；若没有则新建具有该Activity属性的Task，并在该新建的Task上创建Activity。
                    intent.addCategory(Intent.CATEGORY_DEFAULT);//按照普通Activity的执行方式执行
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    LogUtil.logDebugMessage(getPackageName());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //  此处注意替换包名，
                        Uri contentUri = FileProvider.getUriForFile(ExplainActivity.this, "com.feipulai.host.provider", fileList.get(position));
                        intent.setDataAndType(contentUri, "application/pdf");
                    } else {
                        intent.setDataAndType(Uri.fromFile(fileList.get(position)), "application/pdf");
                    }
                    startActivity(intent);
                    Intent.createChooser(intent, "请选择对应的软件打开该附件！");
                } catch (ActivityNotFoundException e) {
                    ToastUtils.showShort("sorry附件不能打开，请下载相关软件！");
                }
            }
        });
    }


    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("说明文档");
    }


}
