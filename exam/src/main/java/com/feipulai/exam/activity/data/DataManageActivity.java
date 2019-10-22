package com.feipulai.exam.activity.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.util.ImageUtils;
import com.feipulai.common.db.ClearDataProcess;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.EditDialog;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LoginActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.IndexTypeAdapter;
import com.feipulai.exam.bean.TypeListBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.exl.ResultExlWriter;
import com.feipulai.exam.exl.StuItemExLReader;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.StringChineseUtil;
import com.feipulai.exam.view.OperateProgressBar;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.widget.ProgressDialog;
import com.yhy.gvp.listener.OnItemClickListener;
import com.yhy.gvp.widget.GridViewPager;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class DataManageActivity
        extends BaseTitleActivity
        implements ExlListener,
        ClearDataProcess.OnProcessFinishedListener {

    private static final int REQUEST_CODE_RESTORE = 1;
    private static final int REQUEST_CODE_BACKUP = 2;
    private static final int REQUEST_CODE_IMPORT = 3;
    private static final int REQUEST_CODE_EXPORT = 4;
    private static final int REQUEST_CODE_PHOTO = 5;

    @BindView(R.id.grid_viewpager)
    GridViewPager gridViewpager;
    @BindView(R.id.indicator_container)
    MagicIndicator indicatorContainer;
    @BindView(R.id.progress_storage)
    ProgressBar progressStorage;
    @BindView(R.id.txt_storage_info)
    TextView txtStorageInfo;

    //是否在加载数据
    private boolean isProcessingData;
    //是否为分组导入
    private boolean isGroupImport;
    private AlertDialog nameFileDialog;
    //    private EditText mEditText;
    private List<TypeListBean> typeDatas;
    public BackupManager backupManager;
    private ProgressDialog progressDialog;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_manage;
    }

    @Override
    protected void initData() {
        backupManager = new BackupManager(this, DBManager.DB_NAME, BackupManager.TYPE_EXAM);

        File file = Environment.getExternalStorageDirectory();
        long freeSpace = file.getFreeSpace();
        long totalSpace = file.getTotalSpace();
        txtStorageInfo.setText("总容量" + FileUtil.formatFileSize(totalSpace, true) + "剩余" + FileUtil.formatFileSize(freeSpace, true));
        progressStorage.setProgress(FileUtil.getPercentRemainStorage());

        FaceServer.getInstance().init(this);
        progressDialog = new ProgressDialog(this);
        initGridView();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("数据管理");
    }

    private void initGridView() {
        typeDatas = new ArrayList<>();
        String[] typeName = getResources().getStringArray(R.array.data_admin);
        int[] typeRes = new int[]{R.mipmap.icon_data_import, R.mipmap.icon_group_import, R.mipmap.icon_data_down, R.mipmap.icon_position_import, R.mipmap.icon_position_down, R.mipmap.icon_delete_position
                , R.mipmap.icon_data_backup, R.mipmap.icon_data_restore, R.mipmap.icon_data_look, R.mipmap.icon_data_clear, R.mipmap.icon_result_upload,
                R.mipmap.icon_result_import/*, R.mipmap.icon_result_delete*/};
        for (int i = 0; i < typeName.length; i++) {
            TypeListBean bean = new TypeListBean();
            bean.setName(typeName[i]);
            bean.setImageRes(typeRes[i]);
            typeDatas.add(bean);
        }
        IndexTypeAdapter indexTypeAdapter = new IndexTypeAdapter(this, typeDatas);//页面内容适配器
        gridViewpager.setGVPAdapter(indexTypeAdapter);
        CommonNavigator commonNavigator = new CommonNavigator(this);//指示器
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                int num = typeDatas.size() / 6;
                if (typeDatas.size() % 6 > 0) {
                    num++;
                }
                return typeDatas == null ? 0 : num;
            }

            @Override
            public IPagerTitleView getTitleView(Context mContext, final int i) {
                CommonPagerTitleView commonPagerTitleView = new CommonPagerTitleView(DataManageActivity.this);
                View view = View.inflate(DataManageActivity.this, R.layout.single_image_layout, null);
                final ImageView iv_image = view.findViewById(R.id.iv_image);
                commonPagerTitleView.setContentView(view);//指示器引入外部布局，可知指示器内容可根据需求设置，多样化
                commonPagerTitleView.setOnPagerTitleChangeListener(new CommonPagerTitleView.OnPagerTitleChangeListener() {
                    @Override
                    public void onSelected(int i, int i1) {
                        iv_image.setImageResource(R.mipmap.icon_point_selected);
                    }

                    @Override
                    public void onDeselected(int i, int i1) {
                        iv_image.setImageResource(R.mipmap.icon_point_unselected);
                    }

                    @Override
                    public void onLeave(int i, int i1, float v, boolean b) {
                    }

                    @Override
                    public void onEnter(int i, int i1, float v, boolean b) {
                    }
                });
                return commonPagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return null;
            }
        });
        indicatorContainer.setNavigator(commonNavigator);
        ViewPagerHelper.bind(indicatorContainer, gridViewpager);//页面内容与指示器关联

        indexTypeAdapter.setOnItemClickListener(new OnItemClickListener<TypeListBean>() {
            @Override
            public void onItemClick(View view, int position, TypeListBean data) {
                Intent intent = new Intent();
                switch (position) {
                    case 0://数据导入
//                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
//                            toastSpeak("中长跑不允许进行个人名单导入");
//                            return;
//                        }
                        isGroupImport = false;
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT);
                        break;

                    case 1: //分组数据导入
                        isGroupImport = true;
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT);
                        break;

                    case 2: //名单下载
//                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
//                        ServerMessage.downloadData(DataManageActivity.this);
                        showDownloadDataDialog();
                        break;

                    case 6: //数据库备份
                        //选择备份到的文件夹
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_BACKUP);
                        break;

                    case 7://数据恢复
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_FOR_RESTORE, DataManageActivity.this).process();
                        break;
                    case 8: //数据查询
                        intent.setClass(DataManageActivity.this, DataRetrieveActivity.class);
                        startActivity(intent);
                        break;
                    case 9: //数据清空
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                        break;

                    case 10://成绩上传

                        uploadData();
                        break;

                    case 11://数据导出
                        //选择文件夹并命名文件导出文件
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT);
                        break;

                    case 4://头像下载
                        ToastUtils.showShort("功能未开放，敬请期待");
                        break;
                    case 3://头像导入
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
                        break;
                    case 5://删除头像
                        //TODO 测试使用
//                        DBManager.getInstance().roundResultClear();

                        new SweetAlertDialog(DataManageActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE).setTitleText("是否进行删除头像")
                                .setConfirmText("确认").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                FaceServer.getInstance().clearAllFaces(DataManageActivity.this);
                            }
                        }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();

                            }
                        }).show();
                        break;

                }
            }
        });
    }

    public void uploadData() {
        DataBaseExecutor.addTask(new DataBaseTask(this, "成绩上传中，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {
                List<UploadResults> uploadResultsList = new ArrayList<>();
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);

                    for (Item item : itemList) {
                        List<UploadResults> dbResultsList = DBManager.getInstance().getUploadResultsAll(item.getItemCode());
                        if (dbResultsList != null && dbResultsList.size() > 0)
                            uploadResultsList.addAll(dbResultsList);
                    }
                } else {
                    uploadResultsList = DBManager.getInstance().getUploadResultsAll(TestConfigs.getCurrentItemCode());
                }

                return new DataBaseRespon(true, "", uploadResultsList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                ServerMessage.uploadResult(DataManageActivity.this, (List<UploadResults>) respon.getObject());
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.DATA_DOWNLOAD_SUCCEED) {
            OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
            ToastUtils.showShort("数据下载完成");
        } else if (baseEvent.getTagInt() == EventConfigs.DATA_DOWNLOAD_FAULT) {
            OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
        } else if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            startActivity(new Intent(this, LoginActivity.class));
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
               SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
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
                Logger.i(" exel文件导入");
                Logger.i("保存路径：" + FileSelectActivity.sSelectedFile);
                if (isGroupImport) {
                    new StuItemExLReader(1, this).readExlData(FileSelectActivity.sSelectedFile);
                } else {
                    new StuItemExLReader(0, this).readExlData(FileSelectActivity.sSelectedFile);
                }
                break;

            case REQUEST_CODE_EXPORT:
                showExportFileNameDialog();
                break;
            case REQUEST_CODE_PHOTO:
                doRegister(FileSelectActivity.sSelectedFile.getAbsolutePath());
                break;
        }
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void doRegister(final String file) {
        File dir = new File(file);
        if (!dir.exists()) {
            ToastUtils.showShort("path \n" + file + "\n is not exists");
            return;
        }
        if (!dir.isDirectory()) {
            ToastUtils.showShort("path \n" + file + "\n is not a directory");
            return;
        }
        final File[] jpgFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FaceServer.IMG_SUFFIX) || name.endsWith(FaceServer.IMG_SUFFIX2)
                        || name.endsWith(FaceServer.IMG_SUFFIX.toUpperCase()) || name.endsWith(FaceServer.IMG_SUFFIX2.toUpperCase());
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int totalCount = jpgFiles.length;

                int successCount = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMaxProgress(totalCount);
                        progressDialog.show();
                    }
                });
                for (int i = 0; i < totalCount; i++) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) {
                                progressDialog.refreshProgress(finalI);
                            }
                        }
                    });
                    final File jpgFile = jpgFiles[i];
                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
//                        File failedFile = new File(file + File.separator + jpgFile.getName());
//                        if (!failedFile.getParentFile().exists()) {
//                            failedFile.getParentFile().mkdirs();
//                        }
//                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    bitmap = ImageUtils.alignBitmapForBgr24(bitmap);
                    if (bitmap == null) {
//                        File failedFile = new File(file + File.separator + jpgFile.getName());
//                        if (!failedFile.getParentFile().exists()) {
//                            failedFile.getParentFile().mkdirs();
//                        }
//                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    byte[] bgr24 = ImageUtils.bitmapToBgr24(bitmap);
                    boolean success = FaceServer.getInstance().registerBgr24(DataManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
                    if (!success) {
                        Log.e("faceRegister", "人脸注册失败" + jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
//                        File failedFile = new File(file + File.separator + jpgFile.getName());
//                        if (!failedFile.getParentFile().exists()) {
//                            failedFile.getParentFile().mkdirs();
//                        }
//                        jpgFile.renameTo(failedFile);
                    } else {
                        successCount++;
                    }
                }
                final int finalSuccessCount = successCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
                Log.i("DataManageActivity", "run: " + executorService.isShutdown());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        FaceServer.getInstance().unInit();
    }

    @Override
    public void onBackPressed() {
        if (isProcessingData) {
            ToastUtils.showShort("正在处理数据,请勿退出!");
            return;
        }
        super.onBackPressed();
    }

    private void showDownloadDataDialog() {
        String[] exemType = new String[]{"正常", "补考", "缓考"};
        new AlertDialog.Builder(this).setTitle("选择下载考试类型")
                .setItems(exemType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int examType = StudentItem.EXAM_NORMAL;
                        switch (which) {
                            case 0:
                                examType = StudentItem.EXAM_NORMAL;
                                break;
                            case 1:
                                examType = StudentItem.EXAM_MAKE;
                                break;
                            case 2:
                                examType = StudentItem.EXAM_DELAYED;
                                break;
                        }

                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
                        ServerMessage.downloadData(DataManageActivity.this, examType);
                    }
                }).create().show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createFileNameDialog(EditDialog.OnConfirmClickListener confirmListener) {
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        new EditDialog.Builder(this).setTitle("文件名")
                .setCanelable(false)
                .setMessage("输入合法保存文件名")
                .setEditHint("请输入文件名")
                .setEditText(SettingHelper.getSystemSetting().getTestName() +
                        SettingHelper.getSystemSetting().getHostId() + "号机" + df.format(new Date()))
                .setPositiveButton(confirmListener)
                .build().show();
    }

    private void showExportFileNameDialog() {

        createFileNameDialog(new EditDialog.OnConfirmClickListener() {
            @Override
            public void OnClickListener(Dialog dialog, String content) {
                OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在导出学生成绩...");
                String text = content.toString().trim();
                if (StringChineseUtil.patternFileName(text)) {
                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    ToastUtils.showShort("文件创建失败,请确保输入文件名合法(中文、字母、数字和下划线),且不存在已有文件");
                    return;
                }
                UsbFile targetFile;
                try {
                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".xls");
                    new ResultExlWriter(TestConfigs.getMaxTestCount(DataManageActivity.this), DataManageActivity.this)
                            .writeExelData(targetFile);

                } catch (IOException e) {
                    e.printStackTrace();
                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
                    Logger.i("文件创建失败,Exel导出失败");
                }
            }
        });
    }

    private void showBackupFileNameDialog() {

        createFileNameDialog(new EditDialog.OnConfirmClickListener() {
            @Override
            public void OnClickListener(Dialog dialog, String content) {
                String text = content.trim();
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
                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
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
        boolean autoBackup = backupManager.autoBackup();
        Logger.i(autoBackup ? "自动备份成功" : "自动备份失败");
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
