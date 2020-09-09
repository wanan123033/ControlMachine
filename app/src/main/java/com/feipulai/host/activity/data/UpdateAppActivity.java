package com.feipulai.host.activity.data;

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
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.bean.SoftApp;
import com.feipulai.host.bean.UpdateApp;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.download.DownloadListener;
import com.feipulai.host.netUtils.download.DownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Headers;

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
        downLoadProgressDialog.setCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadUtils.stopDown(downLoadProgressDialog.getDownFileName());
                downLoadProgressDialog.dismissDialog();
            }
        });
        //UL39Y5
        final HttpSubscriber subscriber = new HttpSubscriber();
        String version = SystemBrightUtils.getCurrentVersion(this);
        subscriber.updateApp( version, updateVersion, auCode, new OnResultListener<UpdateApp>() {
            @Override
            public void onSuccess(UpdateApp result) {
                downLoadProgressDialog.showDialog();

                downLoadProgressDialog.setMaxProgress(100);
                downLoadProgressDialog.setProgress(0);
                downloadUtils.downloadFile(result.getSoftwareUrl(), DateUtil.getCurrentTime() + "-" + result.getVersion()+".apk", new DownloadListener() {
                    @Override
                    public void onStart(String fileName) {
                        Log.i("updateApp-start",fileName);
                    }

                    @Override
                    public void onResponse(Headers headers) {

                    }

                    @Override
                    public void onProgress(String fileName, final int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadProgressDialog.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onFinish(String fileName) {
                        Log.i("UpdateApp",fileName);
                        openFile(new File(fileName));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downLoadProgressDialog.dismissDialog();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String fileName, String errorInfo) {
                        toastSpeak(errorInfo);
                    }
                });
            }

            @Override
            public void onFault(int code, String errorMsg) {

            }
        } );
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
