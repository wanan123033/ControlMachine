package com.feipulai.exam.activity.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.arcsoft.face.util.ImageUtils;
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
import com.feipulai.common.dbutils.FileSelectAdapter;
import com.feipulai.common.dbutils.UsbFileAdapter;
import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
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
import com.feipulai.exam.bean.SoftApp;
import com.feipulai.exam.bean.TypeListBean;
import com.feipulai.exam.bean.UpdateApp;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.exl.ResultExlWriter;
import com.feipulai.exam.exl.StuItemExLReader;
import com.feipulai.exam.exl.ThermometerExlWriter;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.download.DownLoadProgressDialog;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.FileUtils;
import com.feipulai.exam.utils.ImageUtil;
import com.feipulai.exam.utils.StringChineseUtil;
import com.feipulai.exam.view.OperateProgressBar;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.widget.ProgressDialog;
import com.ww.fpl.videolibrary.camera.HkCameraManager;
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
import java.io.Serializable;
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
    private static final int REQUEST_CODE_EXPORT_TEMPLATE = 6;
    private static final int REQUEST_CODE_EXPORT_THERMIMETER = 7;
    private static final int REQUEST_CODE_BACKUP_VIDEO = 8;
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
    private List<TypeListBean> typeDatas;
    public BackupManager backupManager;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder update_zcp_dialog;

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

//        if (!FaceServer.getInstance().init(this)) {
//            ToastUtils.showShort("人脸识别引擎初始化失败");
//        }

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
                R.mipmap.icon_result_import, R.mipmap.icon_template_export, R.mipmap.icon_thermometer, R.mipmap.icon_result_import, R.mipmap.icon_position_down, R.mipmap.icon_data_backup, R.mipmap.icon_data_backup};
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
                        LogUtils.operation("用户点击了数据导入...");
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
                        LogUtils.operation("用户点击了分组数据导入...");
                        isGroupImport = true;
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT);
                        break;

                    case 2: //名单下载
                        LogUtils.operation("用户点击了名单下载...");
//                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
//                        ServerMessage.downloadData(DataManageActivity.this);
                        showDownloadDataDialog();
                        break;
                    case 3://头像导入
                        LogUtils.operation("用户点击了头像导入...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
                        break;
                    case 4://头像下载
                        LogUtils.operation("用户点击了头像下载...");
                        ToastUtils.showShort("功能未开放，敬请期待");
//                        uploadPortrait();
                        break;
                    case 5://删除头像
                        //TODO 测试使用
                        LogUtils.operation("用户点击了删除头像...");
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
                    case 6: //数据库备份
                        LogUtils.operation("用户点击了数据备份...");
                        //选择备份到的文件夹
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_BACKUP);
                        break;
                    case 7://数据恢复
                        LogUtils.operation("用户点击了数据恢复...");
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_FOR_RESTORE, DataManageActivity.this).process();
                        break;
                    case 8: //数据查询
                        LogUtils.operation("用户点击了数据查询...");
                        intent.setClass(DataManageActivity.this, DataRetrieveActivity.class);
                        startActivity(intent);
                        break;
                    case 9: //数据清空
                        LogUtils.operation("用户点击了数据清空...");
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                        break;

                    case 10://成绩上传
                        LogUtils.operation("用户点击了成绩上传...");
                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                            List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                            if (itemList != null && itemList.size() > 0)
                                showZcpSelect(itemList);
                        } else {
//                            if (TestConfigs.sCurrentItem.getItemCode() == null && TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP){
//                                List<Item> items = DBManager.getInstance().queryItemsByMachineCode(18);
//                                if (items.size()>0){
//                                    TestConfigs.sCurrentItem.setItemCode(items.get(0).getItemCode());
//                                }
//                            }
                            uploadData();
                        }
                        break;

                    case 11://数据导出
                        LogUtils.operation("用户点击了数据导出...");
                        //选择文件夹并命名文件导出文件
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT);
                        break;
                    case 12://exl模版导出
                        LogUtils.operation("用户点击了exl模板导出...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT_TEMPLATE);
                        break;
                    case 13://体温查询
                        LogUtils.operation("用户点击了体温查询...");
                        IntentUtil.gotoActivity(DataManageActivity.this, ThermometerSearchActivity.class);
                        break;
                    case 14://体温导出
                        LogUtils.operation("用户点击了体温导出...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT_THERMIMETER);
                        break;
                    case 15://人脸特征检入
                        LogUtils.operation("用户点击了人脸特征检入...");
                        uploadFace();
                        break;
                    case 16://备份中长跑视频到U盘并清空本地视频
                        //选择备份到的文件夹
//                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
//                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
//                        startActivityForResult(intent, REQUEST_CODE_BACKUP_VIDEO);
                        break;
                    case 17://软件更新
                        getAPPS();
                        break;
                    default:
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

                if (result.size()>0){
                    showAppDataDialog(result);
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                toastSpeak(errorMsg);
            }
        });
    }

    /**app版本选择*/
    private void showAppDataDialog(final List<SoftApp> result) {
        Intent intent = new Intent(this,UpdateAppActivity.class);
        intent.putExtra("SoftApp", (Serializable) result);
        startActivity(intent);
//        String [] exemType = new String[result.size()];
//        for (int i = 0;i< result.size() ;i++) {
//            SoftApp softApp = result.get(i);
//            Log.i("softApp",result.get(i).toString());
//            exemType[i] = softApp.getSoftwareName()+softApp.getRemark()+softApp.getVersion();
//        }
//
//
//        new AlertDialog.Builder(this).setTitle("选择下载App版本")
//                .setItems(exemType, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        showAuthCodeDialog(result.get(which).getVersion());
//                    }
//                }).create().show();
    }



    private int choice;

    /**
     * 中长跑选择项目上传（和后面的上传更新逻辑有关，所以此处最好分项目上传)
     */
    private void showZcpSelect(final List<Item> itemList) {
        //默认选中第一个
        final String[] items = new String[itemList.size()];
        for (int i = 0; i < itemList.size(); i++) {
            items[i] = itemList.get(i).getItemName();
        }
        choice = -1;
        update_zcp_dialog = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("选择上传项目")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choice = i;
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (choice != -1) {
                            uploadDataZCP(itemList.get(choice).getItemCode());
                        }
                    }
                });
        update_zcp_dialog.create().show();
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
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                ServerMessage.uploadResult(DataManageActivity.this, results);
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    public void uploadDataZCP(final String itemCode) {
        DataBaseExecutor.addTask(new DataBaseTask(this, "成绩上传中，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {
                List<UploadResults> uploadResultsList = DBManager.getInstance().getUploadResultsAll(itemCode);

                return new DataBaseRespon(true, "", uploadResultsList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                ServerMessage.uploadZCPResult(DataManageActivity.this, itemCode, results);
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
            case REQUEST_CODE_EXPORT_TEMPLATE:

                boolean copySucceed;
                try {
                    UsbFile targetFile = FileSelectActivity.sSelectedFile.createFile("智能主机分组模板.xls");
                    FileUtil.copyFromAssets(getResources().getAssets(), "智能主机分组模板.xls", targetFile);
                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile(".智能主机分组模板delete.xls");
                    deleteFile.delete();

                    targetFile = FileSelectActivity.sSelectedFile.createFile("智能主机个人模板.xls");
                    copySucceed = FileUtil.copyFromAssets(getResources().getAssets(), "智能主机个人模板.xls", targetFile);
                    deleteFile = FileSelectActivity.sSelectedFile.createFile(".智能主机个人模板delete.xls");
                    deleteFile.delete();

                    ToastUtils.showShort(copySucceed ? "模版导出成功" : "模版导出失败");

                    FileSelectActivity.sSelectedFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
                    Logger.i("文件创建失败,模板备份失败");
                }


                break;

            case REQUEST_CODE_EXPORT_THERMIMETER:
                showExportThermometerFileNameDialog();

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
            case REQUEST_CODE_BACKUP_VIDEO:
                String videoFile = "HKVideo" + DateUtil.getCurrentTime("yyyyMMddHHmmss");
                String newFile = FileSelectActivity.sSelectedFile.getName() + File.separator + videoFile;
                Log.i("newFile", newFile);
                try {
                    FileSelectActivity.sSelectedFile.createDirectory(videoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileUtils.copyFolder(HkCameraManager.PATH, newFile);
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
        progressDialog = new ProgressDialog(this);
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
//                        File failedFile = new File(file + File.separator + jpgFile.getName());
////                        if (!failedFile.getParentFile().exists()) {
////                            failedFile.getParentFile().mkdirs();
////                        }
////                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    Student student = DBManager.getInstance().queryStudentByStuCode(jpgFile.getName().substring(0, jpgFile.getName().indexOf(".")));
                    if (student != null) {
                        com.feipulai.common.utils.ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, student.getStudentCode() + ".jpg", bitmap);

                    }
//                    bitmap = ImageUtils.alignBitmapForBgr24(bitmap);
                    bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
                    if (bitmap == null) {
//                        File failedFile = new File(file + File.separator + jpgFile.getName());
//                        if (!failedFile.getParentFile().exists()) {
//                            failedFile.getParentFile().mkdirs();
//                        }
//                        jpgFile.renameTo(failedFile);
                        continue;
                    }

                    byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
                    int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                    if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                        continue;
                    }
//                    byte[] bgr24 = ImageUtils.bitmapToBgr24(bitmap);
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
        LogUtils.life("DataManageActivity onDestroy");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
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

    private void showExportThermometerFileNameDialog() {

        createFileNameDialog(new EditDialog.OnConfirmClickListener() {
            @Override
            public void OnClickListener(Dialog dialog, String content) {
                OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在导出学生体温...");
                String text = content.toString().trim();
                if (StringChineseUtil.patternFileName(text)) {
                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    ToastUtils.showShort("文件创建失败,请确保输入文件名合法(中文、字母、数字和下划线),且不存在已有文件");
                    return;
                }
                UsbFile targetFile;
                try {
                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".xls");
                    new ThermometerExlWriter(new ExlListener() {
                        @Override
                        public void onExlResponse(int responseCode, final String reason) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //关闭等待窗口
                                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                                    ToastUtils.showShort(reason);
                                    isProcessingData = false;
                                }
                            });

                        }
                    }).writeExelData(targetFile);

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
        DataBaseExecutor.addTask(new DataBaseTask(this, "数据清除中，请稍后。。。.", false) {
            @Override
            public DataBaseRespon executeOper() {
                boolean autoBackup = backupManager.autoBackup();
                Logger.i(autoBackup ? "自动备份成功" : "自动备份失败");
                DBManager.getInstance().clear();
                SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                DBManager.getInstance().initDB();
                TestConfigs.init(DataManageActivity.this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                FileUtil.delete(MyApplication.PATH_IMAGE);//清理图片
                FileUtil.mkdirs(MyApplication.PATH_IMAGE);
                FileUtil.delete(MyApplication.PATH_PDF_IMAGE);//清理成绩PDF与图片
                FileUtil.mkdirs(MyApplication.PATH_PDF_IMAGE);
                FileUtil.delete(FaceServer.ROOT_PATH);
                FileUtil.mkdirs2(FaceServer.ROOT_PATH);
                Glide.get(DataManageActivity.this).clearDiskCache();
                FaceServer.getInstance().unInit();
                FaceServer.getInstance().init(DataManageActivity.this);
                Logger.i("进行数据清空");

                return new DataBaseRespon(true, "", "");
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Logger.i("数据清空完成");
                ToastUtils.showShort("数据清空完成");
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

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
                if (studentList.size() == 0) {
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
}
