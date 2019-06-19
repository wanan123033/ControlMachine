package com.feipulai.host.activity.data;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.feipulai.common.db.ClearDataProcess;
import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.exl.ResultExlWriter;
import com.feipulai.host.exl.StuItemExLReader;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;
import com.feipulai.host.utils.FileUtil;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.DBDataCleaner;
import com.feipulai.host.view.OperateProgressBar;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 数据管理
 */
public class DataManageActivity extends BaseActivity implements ExlListener, ClearDataProcess.OnProcessFinishedListener {

    private static final int REQUEST_CODE_RESTORE = 1;
    private static final int REQUEST_CODE_BACKUP = 2;
    private static final int REQUEST_CODE_IMPORT = 3;

    private static final int REQUEST_CODE_EXPORT = 4;

    @BindView(R.id.btn_data_retrieve)
    Button mBtnDataRetrieve;
    @BindView(R.id.btn_data_download)
    Button mBtnDataDownload;
    @BindView(R.id.btn_data_clear)
    Button mBtnDataClear;
    @BindView(R.id.btn_data_import)
    Button mBtnDataImport;
    @BindView(R.id.btn_data_export)
    Button mBtnDataExport;
    @BindView(R.id.btn_data_backup)
    Button mBtnDataBackup;
    @BindView(R.id.btn_data_restore)
    Button mBtnDataRestore;
    @BindView(R.id.donut_progress)
    DonutProgress donutProgress;

    public BackupManager backupManager;
    private AlertDialog nameFileDialog;
    private EditText mEditText;
    private boolean isProcessingData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_manage);
        ButterKnife.bind(this);
        String machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
        setTitle("数据管理 - " + machineName);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        donutProgress.setText(FileUtil.getPercentRemainStorage() + "%");
        donutProgress.setDonut_progress(FileUtil.getPercentRemainStorage() + "");

        backupManager = new BackupManager(this, DBManager.DB_NAME, BackupManager.TYPE_BODY_TEST);

        // if (TextUtils.isEmpty(MyApplication.TOKEN)) {
        //     UserSubscriber subscriber = new UserSubscriber();
        //     subscriber.takeBind(hostId, this);
        // }
    }

    @OnClick({R.id.btn_data_retrieve, R.id.btn_data_download, R.id.btn_result_delete, R.id.btn_data_clear, R.id.btn_data_import,
            R.id.btn_data_export, R.id.btn_data_backup, R.id.btn_data_restore, R.id.btn_portrait_delete, R.id.btn_portrait_download,
            R.id.btn_portrait_import, R.id.btn_scoring_import, R.id.btn_scoring_delete, R.id.btn_scoring_retrieve})
    public void onViewClicked(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {

            case R.id.btn_data_download: //名单下载
                showDownLoadDialog();
                break;

            case R.id.btn_data_import: //数据导入
                //选择文件,增量导入学生和项目信息
                intent.setClass(this, FileSelectActivity.class);
                intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                startActivityForResult(intent, REQUEST_CODE_IMPORT);
                break;

            case R.id.btn_data_export://数据导出
                //选择文件夹并命名文件导出文件
                intent.setClass(this, FileSelectActivity.class);
                intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                startActivityForResult(intent, REQUEST_CODE_EXPORT);
                break;

            case R.id.btn_data_backup: //数据库备份
                //选择备份到的文件夹
                intent.setClass(this, FileSelectActivity.class);
                intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                ToastUtils.showShort("请长按选择需备份文件的文件夹");
                startActivityForResult(intent, REQUEST_CODE_BACKUP);
                break;

            case R.id.btn_data_restore://数据恢复
                new DBDataCleaner(this, ClearDataProcess.CLEAR_FOR_RESTORE, this).process();
                break;

            case R.id.btn_data_retrieve: //数据查询
                intent.setClass(this, DataRetrieveActivity.class);
                startActivity(intent);
                break;

            // case R.id.btn_result_delete://删除成绩
            //     new DBDataCleaner(this, ClearDataProcess.CLEAR_RESULTS, this).process();
            //     break;

            case R.id.btn_data_clear: //数据清空
                new DBDataCleaner(this, ClearDataProcess.CLEAR_DATABASE, this).process();
                break;

            // 功能暂未实现
            case R.id.btn_portrait_delete://删除头像
            case R.id.btn_portrait_download://头像下载
            case R.id.btn_portrait_import://头像导入
            case R.id.btn_scoring_import://导入评分标准
            case R.id.btn_scoring_delete://评分标准删除
            case R.id.btn_scoring_retrieve://评分标准查看
                ToastUtils.showShort("功能未开放，敬请期待");
                break;

        }
    }

    private void showDownLoadDialog() {
        final String[] lastDownLoadTime = new String[1];
        String item[] = new String[]{"下载全部", "下载更新"};
        new AlertDialog.Builder(this)
                .setTitle("名单下载")
                .setSingleChoiceItems(item, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            lastDownLoadTime[0] = null;
                        } else {
                            lastDownLoadTime[0] = SharedPrefsUtil.getValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ItemSubscriber itemSubscriber = new ItemSubscriber();
                        itemSubscriber.getItemAll(MyApplication.TOKEN, DataManageActivity.this, lastDownLoadTime[0], null);
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_RESTORE:
                DBManager.getInstance().close();
                boolean restoreSuccess = backupManager.restore(FileSelectActivity.sSelectedFile);
                ToastUtils.showShort(restoreSuccess ? "数据库恢复成功" : "数据库恢复失败,请检查文件格式");
                Logger.i(restoreSuccess ? ("数据库恢复成功,文件路径:" + FileSelectActivity.sSelectedFile.getName())
                        : "数据库恢复失败");
                com.feipulai.common.utils.SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                DBManager.getInstance().initDB();
                TestConfigs.init(this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                break;

            case REQUEST_CODE_BACKUP:
                showBackupFileNameDialog();
                break;

            case REQUEST_CODE_IMPORT:
                OperateProgressBar.showLoadingUi(this, "正在读取exel文件...");
                //导入学生信息和学生项目信息
                isProcessingData = true;
                Logger.i("exel数据导入,文件名:" + FileSelectActivity.sSelectedFile.getName());
                new StuItemExLReader(this).readExlData(FileSelectActivity.sSelectedFile);
                break;

            case REQUEST_CODE_EXPORT:
                showExportFileNameDialog();
                break;

        }

    }

    @Override
    public void onBackPressed() {
        if (isProcessingData) {
            ToastUtils.showShort("正在处理数据,请勿退出!");
            return;
        }
        super.onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExlResponse(final int responseCode, final String reason) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (responseCode) {

                    case ExlListener.EXEL_READ_SUCCESS:
                        OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                        ToastUtils.showShort(reason);
                        isProcessingData = false;
                        Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
                        sendBroadcast(intent);
                        break;

                    case ExlListener.EXEL_READ_FAIL:
                        OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                        ToastUtils.showShort(reason);
                        isProcessingData = false;
                        break;

                    case ExlListener.EXEL_WRITE_SUCCESS:
                        OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                        ToastUtils.showShort(reason);
                        isProcessingData = false;
                        break;

                    case ExlListener.EXEL_WRITE_FAILED:
                        OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                        Toast.makeText(DataManageActivity.this, reason, Toast.LENGTH_SHORT).show();
                        ToastUtils.showShort(reason);
                        isProcessingData = false;
                        break;
                }
            }

        });

    }

    private void createFileNameDialog(DialogInterface.OnClickListener confirmListener) {
        mEditText = new EditText(this);
        mEditText.setSingleLine();
        mEditText.setBackgroundColor(0xffcccccc);
        mEditText.setText("");
        nameFileDialog = new AlertDialog.Builder(this)
                .setTitle("文件名")
                .setMessage("请输入文件名")
                .setView(mEditText)
                .setPositiveButton("确定", confirmListener)
                .setNegativeButton("取消", null)
                .create();
        nameFileDialog.show();
    }

    private void showExportFileNameDialog() {
        createFileNameDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = mEditText.getText().toString().trim();
                UsbFile targetFile;
                try {
                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".xls");
                    OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在导出xel文件...");
                    //导入学生信息和学生项目信息
                    isProcessingData = true;
                    new ResultExlWriter(DataManageActivity.this).writeExelData(targetFile);

                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("文件创建失败,请检查后再试");
                    Logger.i("文件创建失败,Exel导出失败");
                }
            }
        });
    }

    private void showBackupFileNameDialog() {
        createFileNameDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = mEditText.getText().toString().trim();
                UsbFile targetFile;
                try {
                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".db");
                    boolean backupSuccess = backupManager.backup(targetFile);
                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + text + "delete.db");
                    deleteFile.delete();
                    ToastUtils.showShort(backupSuccess ? "数据库备份成功" : "数据库备份失败");
                    Logger.i(backupSuccess ? ("数据库备份成功,备份文件名:" +
                            FileSelectActivity.sSelectedFile.getName() + "/" + targetFile.getName())
                            : "数据库备份失败");
                    FileSelectActivity.sSelectedFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("文件创建失败,请检查后再试");
                    Logger.i("文件创建失败,数据库备份失败");
                }
            }
        });
    }

    public void chooseFile() {
        Intent intent = new Intent();
        intent.setClass(this, FileSelectActivity.class);
        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
        startActivityForResult(intent, REQUEST_CODE_RESTORE);
        ToastUtils.showShort("请选择备份文件");
    }

    @Override
    public void onRestoreConfirmed() {
        chooseFile();
        ToastUtils.showShort("请选择需要恢复的数据库文件");
    }

    @Override
    public void onClearDBConfirmed() {
        DBManager.getInstance().clear();
        com.feipulai.common.utils.SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
        DBManager.getInstance().initDB();
        TestConfigs.init(this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
        Logger.i("数据清空完成");
        ToastUtils.showShort("数据清空完成");
    }

    // @Override
    // public void onClearResultsConfirmed() {
    //     DBManager.getInstance().deleteItemResult();
    //     ToastUtils.showShort("清空本地学生成绩信息完成");
    //     Logger.i("清空本地学生成绩信息完成");
    // }

}
