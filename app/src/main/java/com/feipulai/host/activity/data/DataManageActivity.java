package com.feipulai.host.activity.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.feipulai.common.db.ClearDataProcess;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.common.dbutils.FileSelectActivity;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.ImageUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.StringChineseUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.utils.archiver.IArchiverListener;
import com.feipulai.common.utils.archiver.ZipArchiver;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.EditDialog;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.SoftApp;
import com.feipulai.host.bean.UploadResults;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.exl.ResultExlWriter;
import com.feipulai.host.exl.StuItemExLReader;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpSubscriber;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.UploadResultUtil;
import com.feipulai.host.netUtils.download.DownService;
import com.feipulai.host.netUtils.download.DownloadHelper;
import com.feipulai.host.netUtils.download.DownloadListener;
import com.feipulai.host.netUtils.download.DownloadUtils;
import com.feipulai.host.netUtils.netapi.ServerIml;
import com.feipulai.host.view.DBDataCleaner;
import com.feipulai.host.view.OperateProgressBar;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.widget.ProgressDialog;
import com.yhy.gvp.listener.OnItemClickListener;
import com.yhy.gvp.widget.GridViewPager;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
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
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import okhttp3.Headers;

/**
 * 数据管理
 */
public class DataManageActivity extends BaseTitleActivity implements ExlListener, ClearDataProcess.OnProcessFinishedListener {

    private static final int REQUEST_CODE_RESTORE = 1;
    private static final int REQUEST_CODE_BACKUP = 2;
    private static final int REQUEST_CODE_IMPORT = 3;
    private static final int REQUEST_CODE_EXPORT = 4;
    private static final int REQUEST_CODE_PHOTO = 5;
    private static final int REQUEST_CODE_EXPORT_TEMPLATE = 6;
    @BindView(R.id.grid_viewpager)
    GridViewPager gridViewpager;
    @BindView(R.id.indicator_container)
    MagicIndicator indicatorContainer;
    @BindView(R.id.progress_storage)
    ProgressBar progressStorage;
    @BindView(R.id.txt_storage_info)
    TextView txtStorageInfo;
    @BindView(R.id.txt_afr_count)
    TextView txtAfrCount;
    public BackupManager backupManager;
    private AlertDialog nameFileDialog;
    //    private EditText mEditText;
    private boolean isProcessingData;
    private List<TypeListBean> typeDatas;
    private ProgressDialog progressDialog;
    private MyHandler myHandler = new MyHandler(this);

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
        txtStorageInfo.setText(String.format(getString(R.string.sdcard_capacity), FileUtil.formatFileSize(totalSpace, true)
                , FileUtil.formatFileSize(freeSpace, true)));
        progressStorage.setProgress(100 - FileUtil.getPercentRemainStorage());

        initGridView();

        progressDialog = new ProgressDialog(this);
        initAfrCount();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.data_message_title);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.DATA_DOWNLOAD_SUCCEED) {
            initAfrCount();
        }
    }

    private void initAfrCount() {

        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                int stuCount = DBManager.getInstance().getItemStudent(-1, 0).size();
                int afrCount = DBManager.getInstance().queryByItemStudentFeatures().size();

                return new DataBaseRespon(true, stuCount + "", afrCount);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                txtAfrCount.setText(String.format(getString(R.string.afr_count_hint), Integer.valueOf(respon.getInfo()), (Integer) respon.getObject()));
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private void initGridView() {
        typeDatas = new ArrayList<>();
        String[] typeName = getResources().getStringArray(R.array.data_admin);
        int[] typeRes = new int[]{R.mipmap.icon_data_import, R.mipmap.icon_data_down
                , R.mipmap.icon_data_backup, R.mipmap.icon_data_restore, R.mipmap.icon_data_look, R.mipmap.icon_data_clear, R.mipmap.icon_result_upload,
                R.mipmap.icon_result_import, R.mipmap.icon_template_export, R.mipmap.icon_position_import, R.mipmap.icon_position_down, R.mipmap.icon_position_down, R.mipmap.icon_position_down};
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
                        //选择文件,增量导入学生和项目信息
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT);
                        break;


                    case 1: //名单下载
                        showDownLoadDialog();
                        break;

                    case 2: //数据库备份
                        //选择备份到的文件夹
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_BACKUP);

                        break;

                    case 3://数据恢复
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_FOR_RESTORE, DataManageActivity.this).process();
                        break;
                    case 4: //数据查询
                        intent.setClass(DataManageActivity.this, DataRetrieveActivity.class);
                        startActivity(intent);
                        break;
                    case 5: //数据清空
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                        break;

                    case 6://成绩上传
//                        List<RoundResult> resultList = DBManager.getInstance().getResultsAll();
                        List<String> resultList = DBManager.getInstance().getResultsStudentByItem(TestConfigs.getCurrentItemCode());
                        if (resultList.size() == 0) {
                            ToastUtils.showShort(getString(R.string.item_result_already_upload));
                        } else {
//                            List<String> studList = resultList.subList(10 * 100, (10 + 1) * 100);
                            //上传数据前先进行项目信息校验
                            uploadData(resultList);
                        }
                        break;

                    case 7://数据导出
                        //选择文件夹并命名文件导出文件
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT);

                        break;

//                    case 4://头像下载
//                    case 3://头像导入
//                    case 5://删除头像
                    //TODO 测试使用
//                        DBManager.getInstance().roundResultClear();
//                        ToastUtils.showShort("功能未开放，敬请期待");
//                        break;
                    case 8://模版导出
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT_TEMPLATE);
                        break;
                    case 9://头像导入
                        LogUtils.operation("用户点击了头像导入...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
                        break;
                    case 10://头像下载
                        LogUtils.operation("用户点击了头像下载...");
//                        uploadPortrait();
//                        ToastUtils.showShort("功能未开放，敬请期待");
                        showDownLoadPhotoDialog();
                        break;
                    case 11://人脸特征
                        LogUtils.operation("用户点击了人脸特征检入...");
                        uploadFace();
                        break;
                    case 12://软件更新
                        LogUtils.operation("用户点击了软件更新...");
                        getAPPS();
                        break;
                }
            }
        });
    }

    private void getAPPS() {
//        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在获取软件列表...");
        final HttpSubscriber subscriber = new HttpSubscriber();
        String version = SystemBrightUtils.getCurrentVersion(this);
        subscriber.getApps(this, version, new OnResultListener<List<SoftApp>>() {

            @Override
            public void onSuccess(List<SoftApp> result) {

                if (result.size() > 0) {
                    showAppDataDialog(result);
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                toastSpeak(errorMsg);
            }
        });
    }

    /**
     * app版本选择
     */
    private void showAppDataDialog(final List<SoftApp> result) {
        Intent intent = new Intent(this, UpdateAppActivity.class);
        intent.putExtra("SoftApp", (Serializable) result);
        startActivity(intent);
    }

    private Headers saveHeaders;
    private DownLoadPhotoHeaders photoHeaders;
    private DownLoadProgressDialog downLoadProgressDialog;
    private DownloadUtils downloadUtils = new DownloadUtils();

    private void uploadPhotos(final int batch, final String uploadTime) {
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("batch", batch + "");
        parameData.put("uploadTime", uploadTime);
        parameData.put("itemcode", TestConfigs.getCurrentItemCode());
        downloadUtils.downloadFile(DownloadHelper.getInstance().buildRetrofit(CommonUtils.getIp()).createService(DownService.class)
                        .downloadFile("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("10001", uploadTime, parameData)),
                MyApplication.PATH_IMAGE, DateUtil.getCurrentTime() + ".zip", new DownloadListener() {
                    @Override
                    public void onStart(String fileName) {
                        LogUtils.operation("下载开始===>" + batch);
                    }

                    @Override
                    public void onResponse(Headers headers) {
                        LogUtils.operation("下载请求成功===>" + batch);
                        saveHeaders = headers;
                        LogUtils.operation("saveHeaders=" + saveHeaders.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                                downLoadProgressDialog.showDialog();
                                if (!TextUtils.isEmpty(saveHeaders.get("BatchTotal"))) {
                                    downLoadProgressDialog.setMaxProgress(Integer.valueOf(saveHeaders.get("BatchTotal")));
                                    downLoadProgressDialog.setProgress(Integer.valueOf(saveHeaders.get("PageNo")) - 1);
                                }

                            }
                        });

                    }

                    @Override
                    public void onProgress(String fileName, int progress) {

                    }

                    @Override
                    public void onFinish(String fileName) {
                        LogUtils.operation("下载结束===>" + batch);
                        if (!new File(MyApplication.PATH_IMAGE + fileName).exists()) {
                            return;
                        }

                        if (photoHeaders == null) {
                            photoHeaders = SharedPrefsUtil.loadFormSource(DataManageActivity.this, DownLoadPhotoHeaders.class);
                        }
                        if (saveHeaders != null && !TextUtils.isEmpty(saveHeaders.get("BatchTotal"))) {
                            photoHeaders.setInit(Integer.valueOf(saveHeaders.get("PageNo")), Integer.valueOf(saveHeaders.get("BatchTotal")), saveHeaders.get("UploadTime"));
                            SharedPrefsUtil.save(DataManageActivity.this, photoHeaders);
                            if (photoHeaders.getPageNo() != photoHeaders.getBatchTotal()) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    downLoadProgressDialog.dismissDialog();
//                                }
//                            });

//                        } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        downLoadProgressDialog.setMaxProgress(Integer.valueOf(saveHeaders.get("BatchTotal")));
                                        downLoadProgressDialog.setProgress(Integer.valueOf(saveHeaders.get("PageNo")));
                                    }
                                });
                                uploadPhotos(photoHeaders.getPageNo() + 1, uploadTime);
                            }
                            int isDismiss = photoHeaders.getPageNo() == photoHeaders.getBatchTotal() ? 1 : 0;
                            HandlerUtil.sendMessage(myHandler, 0, isDismiss, fileName);
                        } else {
                            HandlerUtil.sendMessage(myHandler, 1, 1, "");
                        }

                    }

                    @Override
                    public void onFailure(String fileName, String errorInfo) {
                        HandlerUtil.sendMessage(myHandler, 1, 1, errorInfo);
                    }
                });
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (TextUtils.isEmpty(msg.obj.toString()) && msg.what == 1) {
            ToastUtils.showShort("服务访问失败");
            downLoadProgressDialog.dismissDialog();
            OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
        } else if (msg.what == 1) {
            ToastUtils.showShort(msg.obj.toString());
            downLoadProgressDialog.dismissDialog();
            OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
        } else {

            fileZipArchiver((String) msg.obj, msg.arg1 == 1);
        }
    }


    private void fileZipArchiver(final String fileName, final boolean isDismissDialog) {

        new ZipArchiver().doUnArchiver(MyApplication.PATH_IMAGE + fileName, MyApplication.PATH_IMAGE, "", new IArchiverListener() {
            @Override
            public void onStartArchiver() {

            }

            @Override
            public void onProgressArchiver(int current, int total) {
                if (current == total) {
                    //解压完成删除文件
                    new File(MyApplication.PATH_IMAGE + fileName).delete();
                    if (isDismissDialog) {
                        downLoadProgressDialog.dismissDialog();

                    }

                }
            }

            @Override
            public void onEndArchiver() {

            }
        });

    }

    int selectWhich = 0;

    private void showDownLoadPhotoDialog() {
        selectWhich = 0;
        downLoadProgressDialog = new DownLoadProgressDialog(this);
        downLoadProgressDialog.setCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadUtils.stopDown(downLoadProgressDialog.getDownFileName());
                downLoadProgressDialog.dismissDialog();
            }
        });
        final DownLoadPhotoHeaders photoHeaders = SharedPrefsUtil.loadFormSource(this, DownLoadPhotoHeaders.class);
        List<String> itemList = new ArrayList<>();
        if (photoHeaders == null || photoHeaders.getPageNo() == 0) {
            OperateProgressBar.showLoadingUi(DataManageActivity.this, getString(R.string.download_position_hint));
            uploadPhotos(1, "");
            return;
        } else {
            if (photoHeaders.getBatchTotal() != photoHeaders.getPageNo()) {
                itemList.add(String.format(getString(R.string.download_photo_select), photoHeaders.getPageNo(), photoHeaders.getBatchTotal()));
            }
            itemList.add("更新下载");
            itemList.add("全部下载");
        }
        String[] item = itemList.toArray(new String[itemList.size()]);

        new AlertDialog.Builder(this)
                .setTitle("头像下载")
                .setSingleChoiceItems(item, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectWhich = which;
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OperateProgressBar.showLoadingUi(DataManageActivity.this, getString(R.string.download_position_hint));
                        switch (selectWhich) {
                            case 0:
                                if (photoHeaders.getBatchTotal() != photoHeaders.getPageNo()) {
                                    uploadPhotos(photoHeaders.getPageNo() + 1, "");
                                } else {
                                    uploadPhotos(1, photoHeaders.getUploadTime());
                                }
                                break;
                            case 1:
                                if (photoHeaders.getBatchTotal() != photoHeaders.getPageNo()) {
                                    uploadPhotos(1, photoHeaders.getUploadTime());
                                } else {
                                    uploadPhotos(1, "");
                                }
                                break;
                            case 2:
                                uploadPhotos(1, "");
                                break;
                        }


                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showDownLoadDialog() {
        final String[] lastDownLoadTime = new String[1];
        String item[] = getResources().getStringArray(R.array.download_select);
        new AlertDialog.Builder(this)
                .setTitle(R.string.download_title)
                .setSingleChoiceItems(item, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            lastDownLoadTime[0] = "";
                        } else {
                            lastDownLoadTime[0] = SharedPrefsUtil.getValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
                        }
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ServerIml.downloadData(DataManageActivity.this, lastDownLoadTime[0]);

                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
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
                ToastUtils.showShort(restoreSuccess ?
                        R.string.recover_db_succeed
                        : R.string.recover_db_error);
                Logger.i(restoreSuccess ? ("数据库恢复成功,文件路径:" + FileSelectActivity.sSelectedFile.getName())
                        : "数据库恢复失败");
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                DBManager.getInstance().initDB();
                TestConfigs.init(this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                initAfrCount();
                break;
            case REQUEST_CODE_EXPORT_TEMPLATE:

                boolean copySucceed;
                try {
                    UsbFile targetFile = FileSelectActivity.sSelectedFile.createFile("智能主机体测模板.xls");
                    copySucceed = FileUtil.copyFromAssets(getResources().getAssets(), "智能主机体测模板.xls", targetFile);
                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile(".智能主机体测模板delete.xls");
                    deleteFile.delete();
                    ToastUtils.showShort(copySucceed ? "模版导出成功" : "模版导出失败");

                    FileSelectActivity.sSelectedFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
                    Logger.i("文件创建失败,模板备份失败");
                }
                break;
            case REQUEST_CODE_BACKUP:
                showBackupFileNameDialog();
                break;

            case REQUEST_CODE_IMPORT:
                OperateProgressBar.showLoadingUi(this, getString(R.string.read_exl_hint));
                //导入学生信息和学生项目信息
                isProcessingData = true;
                Logger.i("exel数据导入,文件名:" + FileSelectActivity.sSelectedFile.getName());
                new StuItemExLReader(this).readExlData(FileSelectActivity.sSelectedFile);
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
                return name.endsWith(FaceServer.IMG_SUFFIX_JPG) || name.endsWith(FaceServer.IMG_SUFFIX_PNG)
                        || name.endsWith(FaceServer.IMG_SUFFIX_JPG.toUpperCase()) || name.endsWith(FaceServer.IMG_SUFFIX_PNG.toUpperCase());
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
//                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    //路径获取图片。并对图片做缩小处理
                    Bitmap bitmap = ImageUtil.getSmallBitmap(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
                        continue;
                    }
                    Student student = DBManager.getInstance().queryStudentByStuCode(jpgFile.getName().substring(0, jpgFile.getName().indexOf(".")));
                    if (student != null) {
                        ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, student.getStudentCode() + ".jpg", bitmap);

                    }
//                    bitmap = ImageUtils.alignBitmapForBgr24(bitmap);
                    bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
                    if (bitmap == null) {
                        continue;
                    }

                    byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
                    int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        continue;
                    }
                    String studentCode = jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf("."));
                    byte[] success = FaceServer.getInstance().registerBgr24Byte(DataManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            studentCode);
                    if (student != null) {
                        student.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
                        DBManager.getInstance().updateStudent(student);
                    }
                    if (success == null) {
                        Log.e("faceRegister", "人脸注册失败" + studentCode);

                    } else {
                        successCount++;
                    }
                }
                final int finalSuccessCount = successCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initAfrCount();
                        progressDialog.dismiss();
                    }
                });
                Log.i("DataManageActivity", "run: " + executorService.isShutdown());
            }
        });
    }

//    public void uploadPortrait() {
//        final List<Student> studentList = DBManager.getInstance().getStudentByPortrait();
//        if (studentList.size() == 0) {
//            ToastUtils.showShort("当前所有考生无头像信息，请先进行名单下载");
//            return;
//        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                final int totalCount = studentList.size();
//
//                int successCount = 0;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressDialog.setMaxProgress(totalCount);
//                        progressDialog.show();
//                    }
//                });
//                for (int i = 0; i < totalCount; i++) {
//                    final int finalI = i;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (progressDialog != null) {
//                                progressDialog.refreshProgress(finalI);
//                            }
//                        }
//                    });
//                    Bitmap bitmap = studentList.get(i).getBitmapPortrait();
//                    if (bitmap == null) {
//                        continue;
//                    }
//                    bitmap = ImageUtils.alignBitmapForBgr24(bitmap);
//                    if (bitmap == null) {
//                        continue;
//                    }
//                    byte[] bgr24 = ImageUtils.bitmapToBgr24(bitmap);
//                    boolean success = FaceServer.getInstance().registerBgr24(DataManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
//                            studentList.get(i).getStudentCode());
//                    if (!success) {
//                        Log.e("faceRegister", "人脸注册失败" + studentList.get(i).getStudentCode());
//                    } else {
//                        successCount++;
//                    }
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.showShort("头像导入成功");
//                        progressDialog.dismiss();
//                    }
//                });
//                Log.i("DataManageActivity", "run: " + executorService.isShutdown());
//            }
//        });
//
//    }

    public void uploadFace() {
        DataBaseExecutor.addTask(new DataBaseTask(this, "获取考生人脸特征，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {
                return new DataBaseRespon(true, "", DBManager.getInstance().queryStudentFeatures());
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                final List<Student> studentList = (List<Student>) respon.getObject();
                if (studentList == null || studentList.size() == 0) {
                    ToastUtils.showShort("当前所有考生无头像信息，请先进行名单下载");
                    return;
                }
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        final int totalCount = studentList.size();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setMaxProgress(totalCount);
                                progressDialog.show();
                            }
                        });
                        if (FaceServer.getFaceRegisterInfoList() != null) {
                            FaceServer.getFaceRegisterInfoList().clear();
                        }

                        List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                        for (int i = 0; i < studentList.size(); i++) {

                            Student student = studentList.get(i);
                            registerInfoList.add(new FaceRegisterInfo(Base64.decode(student.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));
                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null) {
                                        progressDialog.refreshProgress(finalI);
                                    }
                                }
                            });
                        }
                        FaceServer.getInstance().addFaceList(registerInfoList);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("人脸特征检入成功");
                                progressDialog.dismiss();
                            }
                        });
                        Log.i("DataManageActivity", "run: " + executorService.isShutdown());
                    }
                });
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        if (isProcessingData) {
            ToastUtils.showShort(R.string.data_exit_hint);
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
                        initAfrCount();
                        break;

                    case ExlListener.EXEL_READ_FAIL:

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

    private void createFileNameDialog(EditDialog.OnConfirmClickListener confirmListener) {


        new EditDialog.Builder(this).setTitle(getString(R.string.add_file_title))
                .setCanelable(false)
                .setMessage(getString(R.string.add_file_message))
                .setEditHint(getString(R.string.please_edit_file_hint))
                .setEditText(String.format(getString(R.string.please_edit_file_txt),
                        SettingHelper.getSystemSetting().getTestName(), SettingHelper.getSystemSetting().getHostId(),
                        DateUtil.getCurrentTime("yyyy年MMddHHmmss")))
                .setPositiveButton(confirmListener)
                .build().show();


    }

    private void showExportFileNameDialog() {
        createFileNameDialog(new EditDialog.OnConfirmClickListener() {
            @Override
            public void OnClickListener(Dialog dialog, String content) {
                String text = content.trim();
                if (StringChineseUtil.patternFileName(text)) {
                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    ToastUtils.showShort(R.string.file_name_legal_hint);
                    return;
                }
                UsbFile targetFile;
                try {
                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".xls");
                    OperateProgressBar.showLoadingUi(DataManageActivity.this, getString(R.string.export_exl_hint));
                    //导入学生信息和学生项目信息
                    isProcessingData = true;
                    new ResultExlWriter(DataManageActivity.this).writeExelData(targetFile);

                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort(R.string.file_create_failed);
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
                if (StringChineseUtil.patternFileName(text)) {
                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    ToastUtils.showShort(R.string.file_name_legal_hint);
                    return;
                }
                backData(text);
//                UsbFile targetFile;
//                try {
//                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".db");
//                    boolean backupSuccess = backupManager.backup(targetFile);
//                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + text + "delete.db");
//                    deleteFile.delete();
//                    ToastUtils.showShort(backupSuccess ? R.string.backup_db_succeed : R.string.backup_db_error);
//                    Logger.i(backupSuccess ? ("数据库备份成功,备份文件名:" +
//                            FileSelectActivity.sSelectedFile.getName() + "/" + targetFile.getName())
//                            : "数据库备份失败");
//                    FileSelectActivity.sSelectedFile = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.file_create_failed);
//                    Logger.i("文件创建失败,数据库备份失败");
//                }
            }
        });
    }


    private DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private void backData(final String fileName) {

        OperateProgressBar.showLoadingUi(DataManageActivity.this, "数据备份中...");
        final UsbFile file = new UsbFileAdapter(new File(MyApplication.BACKUP_DIR));

        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                UsbFile targetFile = null;
                try {
                    targetFile = file.createFile(fileName + df.format(new Date()) + ".db");
                    boolean backupSuccess = backupManager.backup(targetFile);
                    UsbFile deleteFile = file.createFile("." + fileName + "delete.db");
                    deleteFile.delete();
                    ToastUtils.showShort(backupSuccess ? "数据库备份成功" : "数据库备份失败");

                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
                    Logger.i("文件创建失败,数据库备份失败");
                }

                return new DataBaseRespon(true, "", ((UsbFileAdapter) targetFile).getFile());
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                final UsbFile excelFile;
                final File dbFile = (File) respon.getObject();
                try {
                    excelFile = file.createFile(fileName + df.format(new Date()) + ".xls");
                    new ResultExlWriter(new ExlListener() {
                        @Override
                        public void onExlResponse(final int responseCode, final String reason) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort(reason);
                                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                                    if (responseCode == ExlListener.EXEL_WRITE_SUCCESS) {
                                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "备份文件压缩中...");

                                    } else {
                                        isProcessingData = false;
                                    }
                                }
                            });

                            if (responseCode == ExlListener.EXEL_WRITE_SUCCESS) {
                                zipFile(fileName, MyApplication.BACKUP_DIR + fileName + df.format(new Date()) + ".zip",
                                        dbFile, ((UsbFileAdapter) excelFile).getFile());
                            }


                        }
                    }).writeExelData(excelFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private void zipFile(String fileName, String zipPathName, File dbFile, File excelFile) {
        // 生成的压缩文件
        try {
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + backUp);
//            File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + excel);
//                File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/KS_LOGGER/operationLogger");
            ZipFile zipFile = new ZipFile(zipPathName);
            ZipParameters parameters = new ZipParameters();
            // 压缩方式
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            // 压缩级别
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            if (dbFile.exists()) {
                zipFile.addFile(dbFile, parameters);
                Log.i("zipFile", Environment.getExternalStorageDirectory().getAbsolutePath() + dbFile + "file is exists");
            } else {
                Log.i("zipFile", Environment.getExternalStorageDirectory().getAbsolutePath() + dbFile + "file is not exists");
            }
            if (excelFile.exists()) {
                zipFile.addFile(excelFile, parameters);
                Log.i("zipFile", Environment.getExternalStorageDirectory().getAbsolutePath() + excelFile + "file is  exists");
            } else {
                Log.i("zipFile", Environment.getExternalStorageDirectory().getAbsolutePath() + excelFile + "file1 is not exists");
            }
            zipFile.addFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MyApplication.LOG_PATH_NAME , parameters);

            UsbFile copeFile = FileSelectActivity.sSelectedFile.createFile(fileName + ".zip");
            FileUtil.copyFile(zipFile.getFile(), copeFile);

            UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile(".Delete" + fileName + ".zip");
            deleteFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
            }
        });


    }


    public void chooseFile() {
        Intent intent = new Intent();
        intent.setClass(this, FileSelectActivity.class);
        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
        startActivityForResult(intent, REQUEST_CODE_RESTORE);
        ToastUtils.showShort(getString(R.string.please_select_backup_file));
    }

    @Override
    public void onRestoreConfirmed() {
        chooseFile();
        ToastUtils.showShort(getString(R.string.please_recover_db_file));
    }

    @Override
    public void onClearDBConfirmed() {

        DataBaseExecutor.addTask(new DataBaseTask(this, "数据清除中，请稍后。。。", false) {
            @Override
            public DataBaseRespon executeOper() {
                boolean autoBackup = backupManager.autoBackup();
                Logger.i(autoBackup ? "自动备份成功" : "自动备份失败");
                DBManager.getInstance().clear();
                SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                SharedPrefsUtil.remove(DataManageActivity.this, DownLoadPhotoHeaders.class);
                DBManager.getInstance().initDB();
                TestConfigs.init(DataManageActivity.this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                FileUtil.delete(MyApplication.PATH_IMAGE);
                FileUtil.delete(FaceServer.ROOT_PATH);
                FileUtil.mkdirs2(FaceServer.ROOT_PATH);
                FileUtil.mkdirs(MyApplication.PATH_IMAGE);
                Glide.get(DataManageActivity.this).clearDiskCache();
                Logger.i("进行数据清空");
                FaceServer.getInstance().unInit();
                FaceServer.getInstance().init(DataManageActivity.this);
                return new DataBaseRespon(true, "", "");
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Logger.i("数据清空完成");
                ToastUtils.showShort("数据清空完成");
                initAfrCount();
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });

    }

    // @Override
    // public void onClearResultsConfirmed() {
    //     DBManager.getInstance().deleteItemResult();
    //     ToastUtils.showShort("清空本地学生成绩信息完成");
    //     Logger.i("清空本地学生成绩信息完成");
    // }
    public void uploadData(final List<String> resultList) {
        DataBaseExecutor.addTask(new DataBaseTask(this, "成绩上传中，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {


                return new DataBaseRespon(true, "", UploadResultUtil.getUploadDataByStuCode(resultList));
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                ServerIml.uploadResult(DataManageActivity.this, results);
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }
}
