package com.feipulai.exam.activity.data;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.EditDialog;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.adapter.AppSoftAdapter;
import com.feipulai.exam.bean.SoftApp;
import com.feipulai.exam.bean.UpdateApp;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.download.DownLoadProgressDialog;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class UpdateAppActivity extends BaseTitleActivity {
    @BindView(R.id.rv_result)
    RecyclerView rvResult;
    private List<SoftApp> appList = new ArrayList<>();
    private AppSoftAdapter adapter;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_update_app;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        appList = (List<SoftApp>) intent.getSerializableExtra("SoftApp");
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppSoftAdapter(appList);
        rvResult.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                showAuthCodeDialog(appList.get(position).getVersion());
            }
        });
    }

    /**授权码*/
    private void showAuthCodeDialog(final String version) {

        new EditDialog.Builder(this).setTitle(getString(com.feipulai.common.R.string.auth_code))
                .setCanelable(false)
                .setEditHint(getString(com.feipulai.common.R.string.auth_code))
                .setPositiveButton(new EditDialog.OnConfirmClickListener() {
                    @Override
                    public void OnClickListener(Dialog dialog, String content) {

                        updateApp(version,content.trim());
                    }
                })
                .build().show();

    }
    private DownloadUtils downloadUtils = new DownloadUtils();
    private DownLoadProgressDialog downLoadProgressDialog;

    /**
     * 获取app url 并下载与安装app
     * @param updateVersion
     * @param auCode
     */
    private void updateApp(String updateVersion,String auCode){
        downLoadProgressDialog = new DownLoadProgressDialog(this);
        //UL39Y5
        final HttpSubscriber subscriber = new HttpSubscriber();
        String version = SystemBrightUtils.getCurrentVersion(this);
        subscriber.updateApp( version, updateVersion, auCode, new OnResultListener<UpdateApp>() {
            @Override
            public void onSuccess(UpdateApp result) {
                Log.i("UpdateApp",result.toString());
                downLoadProgressDialog.showDialog();
                downLoadProgressDialog.setMaxProgress(100);
                downLoadProgressDialog.setProgress(0);
                //https://minio.fairplay.xin/fplcloud-soft/software/1ea85f244b36405987716dff880ac726.apk?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=fpcloud%2F20200903%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20200903T070732Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host&X-Amz-Signature=922e4dc220cb4904502f2a602924477997ac324a07cb10a251a964b67349ea9b
                String name = DateUtil.getCurrentTime()+result.getVersion()+".apk";
                downloadUtils.downloadFile(result.getSoftwareUrl(),name, new DownloadListener() {
                    @Override
                    public void onStart() {
                        Log.i("UpdateApp","onStart");
                    }

                    @Override
                    public void onProgress(final int progress) {
                        Log.i("UpdateApp","progress"+progress);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadProgressDialog.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onFinish(String path) {
                        Log.i("UpdateApp",path);
                        openFile(new File(path));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadProgressDialog.dismissDialog();
                            }
                        });

                    }

                    @Override
                    public void onFailure(String errorInfo) {
                        Log.i("UpdateApp",errorInfo);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadProgressDialog.dismissDialog();
                            }
                        });
                    }
                });
            }

            @Override
            public void onFault(int code, String errorMsg) {
                toastSpeak(errorMsg);
            }
        });
    }

    /**
     * 安装app
     * @param file
     */
    private void openFile(File file) {
        Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("APP更新");
    }
}
