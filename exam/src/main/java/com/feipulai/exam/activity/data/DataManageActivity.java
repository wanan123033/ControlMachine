package com.feipulai.exam.activity.data;

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

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
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
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SystemBrightUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.utils.archiver.IArchiverListener;
import com.feipulai.common.utils.archiver.ZipArchiver;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.common.view.dialog.EditDialog;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.login.LoginActivity;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.NewRunAdapter;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.IndexTypeAdapter;
import com.feipulai.exam.bean.SoftApp;
import com.feipulai.exam.bean.TypeListBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentFace;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.exl.ResultExlWriter;
import com.feipulai.exam.exl.StuItemExLReader;
import com.feipulai.exam.exl.ThermometerExlWriter;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.download.DownLoadProgressDialog;
import com.feipulai.exam.netUtils.download.DownService;
import com.feipulai.exam.netUtils.download.DownloadHelper;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.tcp.CommonListener;
import com.feipulai.exam.tcp.TcpDownLoadUtil;
import com.feipulai.exam.utils.FileUtils;
import com.feipulai.exam.utils.ImageUtil;
import com.feipulai.exam.utils.StringChineseUtil;
import com.feipulai.exam.view.OperateProgressBar;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.widget.ProgressDialog;
import com.ww.fpl.videolibrary.camera.HkCameraManager;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Headers;

import static com.feipulai.exam.tcp.TCPConst.PHOTO;
import static com.feipulai.exam.tcp.TCPConst.SCHEDULE;

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
    @BindView(R.id.txt_afr_count)
    TextView txtAfrCount;
    //是否在加载数据
    private boolean isProcessingData;
    //是否为分组导入
    private boolean isGroupImport;
    private List<TypeListBean> typeDatas;
    public BackupManager backupManager;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder update_zcp_dialog;
    private DownLoadProgressDialog downLoadProgressDialog;
    private MyHandler myHandler = new MyHandler(this);
    private boolean isDelPhoto, isDelAFR, isDelBase; //文件删除选择
    private SystemSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_manage;
    }

    @Override
    protected void initData() {
        setting = SettingHelper.getSystemSetting();
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

    @Override
    protected void onResume() {
        super.onResume();
        initAfrCount();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("数据管理");
    }

    private void initAfrCount() {

        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                int stuCount = DBManager.getInstance().getItemStudent("-2", TestConfigs.getCurrentItemCode(), -1, 0).size();
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    stuCount = 0;
                    List<Item> itemLists = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);

                    for (Item item : itemLists) {
                        stuCount += DBManager.getInstance().getItemStudent("-2", item.getItemCode(), -1, 0).size();

                    }
                }
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
        int[] typeRes = new int[]{R.mipmap.icon_data_import, R.mipmap.icon_group_import, R.mipmap.icon_data_down, R.mipmap.icon_position_import, R.mipmap.icon_position_down, R.mipmap.icon_delete_position
                , R.mipmap.icon_data_backup, R.mipmap.icon_data_restore, R.mipmap.icon_data_look, R.mipmap.icon_data_clear, R.mipmap.icon_result_upload,
                R.mipmap.icon_result_import, R.mipmap.icon_template_export, R.mipmap.icon_thermometer, R.mipmap.icon_result_import, R.mipmap.icon_position_down, R.mipmap.icon_data_backup, R.mipmap.icon_data_backup
                , R.mipmap.icon_delete_logger};
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
//                        showDownloadDataDialog();
                        startActivity(new Intent(DataManageActivity.this, DataDownLoadActivity.class));
                        break;
                    case 3://头像导入
                        LogUtils.operation("用户点击了头像导入...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
                        break;
                    case 4://头像下载
                        LogUtils.operation("用户点击了头像下载...");
//                        ToastUtils.showShort("功能未开放，敬请期待");
//                        uploadPortrait();
//                        showDownLoadPhotoDialog();
                        showDownLoadTopicDialog();
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
                        showClearDialog();
//                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                        break;

                    case 10://成绩上传
                        LogUtils.operation("用户点击了成绩上传...");
                        IntentUtil.gotoActivity(DataManageActivity.this, DataUploadActivity.class);
//                        showUploadDataDialog();
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
                    case 18://日志删除
                        new SweetAlertDialog(DataManageActivity.this, SweetAlertDialog.WARNING_TYPE).setTitleText("温馨提示")
                                .setContentText("是否清空删除所有日志文件").setConfirmText(getString(R.string.confirm))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                        showAuthCodeDialog();
                                    }
                                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        }).show();

                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showClearDialog() {
        new ClearDataDialog(DataManageActivity.this, new ClearDataDialog.OnProcessFinishedListener() {
            @Override
            public void onClearConfirmed(boolean isDeletePhoto, boolean isDeleteAFR, boolean isDeleteBase) {
                isDelPhoto = isDeletePhoto;
                isDelAFR = isDeleteAFR;
                isDelBase = isDeleteBase;
                new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
            }
        }).showDialog();
    }


    int selectWhich = 0;
    private Headers saveHeaders;
    private DownLoadPhotoHeaders photoHeaders;
    private DownloadUtils downloadUtils = new DownloadUtils();

    private void showDownLoadTopicDialog() {
        String[] itemList = new String[]{"HTTP下载", "TCP下载"};
        new AlertDialog.Builder(this)
                .setTitle("头像下载")
                .setItems(itemList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            showDownLoadPhotoDialog();
                        } else {
                            tcpDownLoadPhoto();
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private void tcpDownLoadPhoto() {

        String[] tcpIp = setting.getTcpIp().split(":");
        OperateProgressBar.showLoadingUi(this, "头像下载");
        final TcpDownLoadUtil tcp = new TcpDownLoadUtil(getApplicationContext(), tcpIp[0], tcpIp[1], new CommonListener() {
            @Override
            public void onCommonListener(final int no, final String string) {

                //[, , 0, 2, PFPPhoto, , 0, , 立定跳远, 14891662, 500, 0, F1_ITEM_11, 6, 1, , 0, , 0, 0, 0, , 0, null, null, null, null, null, null, null, null, null, null]

//                toastSpeak(string);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (no == TcpDownLoadUtil.DOWNLOAD_FLAG) {
                            toastSpeak(string);
                        } else {
                            String[] receiveArray = string.split(",");
                            int m_nAllProp = Integer.parseInt(receiveArray[13].trim());//总压缩包数
                            int m_nAllNum = Integer.parseInt(receiveArray[14].trim());   //当前第几个包

                            downLoadProgressDialog.setMaxProgress(m_nAllProp);
                            downLoadProgressDialog.setProgress(m_nAllNum);
                            if (m_nAllNum == m_nAllProp) {
                                downLoadProgressDialog.dismissDialog();
                                toastSpeak("头像下载完成");
                            } else {
                                if (m_nAllNum == 1) {
                                    downLoadProgressDialog.showDialog();
                                }
                            }
                        }

                        OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                    }
                });

            }
        });
        tcp.getTcpPhoto();
        downLoadProgressDialog = new DownLoadProgressDialog(this);
        downLoadProgressDialog.setCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tcp.downStop();
            }
        });
    }

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
            OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在进行头像下载...");
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
                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在进行头像下载...");
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

    private void uploadPhotos(int batch, final String uploadTime) {
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("batch", batch + "");
        parameData.put("uploadTime", uploadTime);
        parameData.put("itemcode", TestConfigs.getCurrentItemCode());
        downloadUtils.downloadFile(DownloadHelper.getInstance().buildRetrofit(CommonUtils.getIp()).createService(DownService.class)
                        .downloadFile("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("10001", uploadTime, parameData)),
                MyApplication.PATH_IMAGE, DateUtil.getCurrentTime() + ".zip", new DownloadListener() {
                    @Override
                    public void onStart(String fileName) {

                    }

                    @Override
                    public void onResponse(Headers headers) {
                        saveHeaders = headers;
                        LogUtils.normal("保存头像返回头部信息=" + saveHeaders.toString());
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

                        if (!new File(MyApplication.PATH_IMAGE + fileName).exists()) {
                            HandlerUtil.sendMessage(myHandler, 1, 1, "头像下载失败");
                            return;
                        }

                        if (photoHeaders == null) {
                            photoHeaders = SharedPrefsUtil.loadFormSource(DataManageActivity.this, DownLoadPhotoHeaders.class);
                        }
                        if (saveHeaders != null && !TextUtils.isEmpty(saveHeaders.get("BatchTotal"))) {
                            int total = Integer.valueOf(saveHeaders.get("BatchTotal"));
                            if (total == 0) {
                                HandlerUtil.sendMessage(myHandler, 1, 1, "");
                                return;
                            }
                            photoHeaders.setInit(Integer.valueOf(saveHeaders.get("PageNo")), total, saveHeaders.get("UploadTime"));
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


    private void getAPPS() {
//        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在获取软件列表...");
        final HttpSubscriber subscriber = new HttpSubscriber();
        String version = SystemBrightUtils.getCurrentVersion(this);
        subscriber.getApps(this, version, new OnResultListener<List<SoftApp>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(List<SoftApp> result) {

                if (result != null && result.size() > 0) {
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
//    private void showZcpSelect(final boolean isUploadAll, final List<Item> itemList) {
//        //默认选中第一个
//        final String[] items = new String[itemList.size()];
//        for (int i = 0; i < itemList.size(); i++) {
//            items[i] = itemList.get(i).getItemName();
//        }
//        choice = -1;
//        update_zcp_dialog = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("选择上传项目")
//                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        choice = i;
//                    }
//                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (choice != -1) {
//                            uploadDataZCP(isUploadAll, itemList.get(choice).getItemCode());
//                        }
//                    }
//                });
//        update_zcp_dialog.create().show();
//    }
    public void uploadData(final boolean isUploadAll) {
        DataBaseExecutor.addTask(new DataBaseTask(this, "成绩上传中，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {
                List<UploadResults> uploadResultsList = new ArrayList<>();
                if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                    List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                    for (Item item : itemList) {
                        List<UploadResults> dbResultsList = DBManager.getInstance().getUploadResultsAll(isUploadAll, item.getItemCode());
                        if (dbResultsList != null && dbResultsList.size() > 0)
                            uploadResultsList.addAll(dbResultsList);
                    }
                } else {
                    uploadResultsList = DBManager.getInstance().getUploadResultsAll(isUploadAll, TestConfigs.getCurrentItemCode());
                }

                return new DataBaseRespon(true, "", uploadResultsList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<UploadResults> results = (List<UploadResults>) respon.getObject();
                Log.e("UploadResults", "---------" + results.size());
                if (results.size() == 0) {
                    new SweetAlertDialog(DataManageActivity.this, SweetAlertDialog.WARNING_TYPE).setTitleText("无可上传数据")
                            .setContentText("是否进行日期筛查上传").setConfirmText(getString(R.string.confirm))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    IntentUtil.gotoActivity(DataManageActivity.this, DataUploadActivity.class);
                                }
                            }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
                } else {
                    ServerMessage.baseUploadResult(DataManageActivity.this, results);
                }

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

//    public void uploadDataZCP(final boolean isUploadAll, final String itemCode) {
//        DataBaseExecutor.addTask(new DataBaseTask(this, "成绩上传中，请稍后...", false) {
//            @Override
//            public DataBaseRespon executeOper() {
//                List<UploadResults> uploadResultsList = DBManager.getInstance().getUploadResultsAll(isUploadAll
//                        , itemCode);
//
//                return new DataBaseRespon(true, "", uploadResultsList);
//            }
//
//            @Override
//            public void onExecuteSuccess(DataBaseRespon respon) {
//                List<UploadResults> results = (List<UploadResults>) respon.getObject();
//                Log.e("UploadResults", "---------" + results.size());
//                ServerMessage.uploadZCPResult(DataManageActivity.this, itemCode, results);
//            }
//
//            @Override
//            public void onExecuteFail(DataBaseRespon respon) {
//
//            }
//        });
//    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        switch (baseEvent.getTagInt()) {
            case EventConfigs.REQUEST_UPLOAD_LOG_S:
                OperateProgressBar.removeLoadingUiIfExist(this);
                ToastUtils.showShort("网络备份完成");
                break;
            case EventConfigs.REQUEST_UPLOAD_LOG_E:
                OperateProgressBar.removeLoadingUiIfExist(this);
                ToastUtils.showShort("网络备份失败");
                break;
            case EventConfigs.REQUEST_UPLOAD_LOG_F:
                OperateProgressBar.showLoadingUi(this, "正在网络备份...");
                break;
            case EventConfigs.DATA_DOWNLOAD_SUCCEED:
                OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                ToastUtils.showShort("数据下载完成");
                initAfrCount();
                break;
            case EventConfigs.DATA_DOWNLOAD_FAULT:
                OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                break;
            case EventConfigs.TOKEN_ERROR:
                startActivity(new Intent(this, LoginActivity.class));
                break;
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
                initAfrCount();
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
                final List<FaceRegisterInfo> faceRegisterInfos = new ArrayList<>();
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
                    String studentCode = jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf("."));
                    byte[] success = FaceServer.getInstance().registerBgr24Byte(DataManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            studentCode);
                    if (student != null && success != null) {
//                        student.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
                        //                        DBManager.getInstance().updateStudent(student);

                        StudentFace studentFace = new StudentFace();
                        studentFace.setStudentCode(studentCode);
                        studentFace.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
                        DBManager.getInstance().insertStudentFace(studentFace);
                        faceRegisterInfos.add(new FaceRegisterInfo(success, studentCode));
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
                        FaceServer.getInstance().addFaceList(faceRegisterInfos);
                        initAfrCount();
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

    private void showDownLoadDialog(final int examType) {
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

                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
                        ServerMessage.downloadData(DataManageActivity.this, examType, lastDownLoadTime[0]);
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    private void showUploadDataDialog() {
        uploadData(false);
//        String[] uploadType = new String[]{"上传全部成绩", "上传未上传考生成绩"};
//        new AlertDialog.Builder(this).setTitle("选择成绩上传类型")
//                .setItems(uploadType, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
//                            List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
//                            if (itemList != null && itemList.size() > 0)
//                                showZcpSelect(which == 0, itemList);
//                        } else {
//                            uploadData(which == 0);
//                        }
//
//                    }
//                }).create().show();
    }

    int type = 0;

    private void showDownloadDataDialog() {

        String[] exemType = new String[]{"正常", "补考", "缓考", "TCP下载"};
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
                            case 3:
                                type = 3;
                                break;
                        }
                        String downTime = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS,
                                SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
                        if (type == 3) {
                            OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
                            dataDownload(SettingHelper.getSystemSetting().getTcpIp());
                            return;
                        }
                        if (!TextUtils.isEmpty(downTime)) {
                            showDownLoadDialog(examType);
                        } else {
                            OperateProgressBar.showLoadingUi(DataManageActivity.this, "正在下载最新数据...");
                            ServerMessage.downloadData(DataManageActivity.this, examType, "0");
                        }

                    }
                }).create().show();
    }

    public void dataDownload(String tcpip) {
        if (TextUtils.isEmpty(tcpip)) {
            OperateProgressBar.removeLoadingUiIfExist(this);
            Toast.makeText(getApplicationContext(), "请输入正确的TCP地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tcpip.contains(":")) {
            String ip = tcpip.substring(0, tcpip.indexOf(":"));
            String port = tcpip.substring(tcpip.indexOf(":") + 1);
            TcpDownLoadUtil tcpDownLoad = new TcpDownLoadUtil(MyApplication.getInstance(), ip, port, new CommonListener() {
                @Override
                public void onCommonListener(int no, final String string) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initAfrCount();
                            toastSpeak(string);
                            OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);

                        }
                    });
                }
            });
            tcpDownLoad.getTcp(SCHEDULE, "", 0, 0);
        } else {
            OperateProgressBar.removeLoadingUiIfExist(this);
            Toast.makeText(getApplicationContext(), "请输入正确的TCP地址", Toast.LENGTH_SHORT).show();
            return;
        }
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
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH");
        new EditDialog.Builder(this).setTitle("文件名")
                .setCanelable(false)
                .setMessage("输入合法保存文件名")
                .setEditHint("请输入文件名")
                .setEditText(SettingHelper.getSystemSetting().getTestName() + TestConfigs.sCurrentItem.getItemName() +
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
                backData(content);
//                OperateProgressBar.showLoadingUi(DataManageActivity.this, "数据备份中...");
//                String text = content.trim();
//                UsbFile targetFile;
//
//                try {
//                    targetFile = FileSelectActivity.sSelectedFile.createFile(text + ".db");
//                    boolean backupSuccess = backupManager.backup(targetFile);
//                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile("." + text + "delete.db");
//                    deleteFile.delete();
//                    ToastUtils.showShort(backupSuccess ? "数据库备份成功" : "数据库备份失败");
//                    Logger.i(backupSuccess ? ("数据库备份成功,备份文件名:" +
//                            FileSelectActivity.sSelectedFile.getName() + "/" + targetFile.getName())
//                            : "数据库备份失败");
////                    FileSelectActivity.sSelectedFile = null;
//                    if (backupSuccess) {
//                        UsbFile excelFile = FileSelectActivity.sSelectedFile.createFile(text + ".xls");
//                        new ResultExlWriter(TestConfigs.getMaxTestCount(DataManageActivity.this), DataManageActivity.this)
//                                .writeExelData(excelFile);
//                        zipFile(FileSelectActivity.sSelectedFile.getAbsolutePath() + "/" + text, FileSelectActivity.sSelectedFile.getName() + "/" + targetFile.getName(),
//                                FileSelectActivity.sSelectedFile.getName() + "/" + excelFile.getName());
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
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
                    final boolean backupSuccess = backupManager.backup(targetFile);
                    UsbFile deleteFile = file.createFile("." + fileName + "delete.db");
                    deleteFile.delete();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort(backupSuccess ? "数据库备份成功" : "数据库备份失败");
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("文件创建失败,请确保路径目录不存在已有文件");
                        }
                    });

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
                    new ResultExlWriter(TestConfigs.getMaxTestCount(DataManageActivity.this), new ExlListener() {
                        @Override
                        public void onExlResponse(final int responseCode, final String reason) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showShort(reason);
//                                    OperateProgressBar.removeLoadingUiIfExist(DataManageActivity.this);
                                    if (responseCode == ExlListener.EXEL_WRITE_SUCCESS) {
//                                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "备份文件压缩中...");

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

    private void zipFile(final String fileName, final String zipPathName, File dbFile, File excelFile) {
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
            zipFile.addFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MyApplication.PATH_LOG_NAME, parameters);

            UsbFile copeFile = FileSelectActivity.sSelectedFile.createFile(fileName + ".zip");
            FileUtil.copyFile(zipFile.getFile(), copeFile);

            UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile(".Delete" + fileName + ".zip");
            deleteFile.delete();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showUploadLog(fileName, zipPathName);
                }
            });

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

    private void showUploadLog(final String fileName, final String zipPathName) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.clear_dialog_title))
                .setContentText("上传日志到云端?")
                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                OperateProgressBar.showLoadingUi(DataManageActivity.this, "备份文件上传中...");
                ServerMessage.uploadLogFile(fileName, zipPathName);
                sweetAlertDialog.dismissWithAnimation();
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();
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
        DataBaseExecutor.addTask(new DataBaseTask(this, "数据清除中，请稍后。。。.", true) {
            @Override
            public DataBaseRespon executeOper() {

                if (isDelBase) {
                    boolean autoBackup = backupManager.autoBackup();
                    Logger.i(autoBackup ? "自动备份成功" : "自动备份失败");
                    DBManager.getInstance().clear();
                    SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                    SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, null);
                    SharedPrefsUtil.remove(DataManageActivity.this, DownLoadPhotoHeaders.class);
                    DBManager.getInstance().initDB();
                    TestConfigs.init(DataManageActivity.this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);

                }

                if (isDelPhoto) {
                    FileUtil.delete(MyApplication.PATH_IMAGE);//清理图片
                    FileUtil.mkdirs(MyApplication.PATH_IMAGE);
                }
                if (isDelAFR) {
                    DBManager.getInstance().clearFace();
                    FaceServer.getInstance().unInit();
                    FaceServer.getInstance().init(DataManageActivity.this);
                }
                FileUtil.delete(MyApplication.PATH_PDF_IMAGE);//清理成绩PDF与图片
                FileUtil.mkdirs(MyApplication.PATH_PDF_IMAGE);
//                FileUtil.delete(MyApplication.BACKUP_DIR);
//                FileUtil.mkdirs2(MyApplication.BACKUP_DIR);
                FileUtil.delete(FaceServer.ROOT_PATH);
                FileUtil.mkdirs2(FaceServer.ROOT_PATH);

                Glide.get(DataManageActivity.this).clearDiskCache();

                Logger.i("进行数据清空");

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
        progressDialog = new ProgressDialog(DataManageActivity.this);
        DataBaseExecutor.addTask(new DataBaseTask(this, "获取考生人脸特征，请稍后...", false) {
            @Override
            public DataBaseRespon executeOper() {
                return new DataBaseRespon(true, "", DBManager.getInstance().getStudentFeatures());
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                final List<StudentFace> studentList = (List<StudentFace>) respon.getObject();
                if (studentList.size() == 0) {
                    ToastUtils.showShort("当前所有考生无头像信息，请先进行名单下载");
                    return;
                }
                int activeCode = FaceEngine.activeOnline(DataManageActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                if (SettingHelper.getSystemSetting().getCheckTool() == 4 && activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {

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


                                StudentFace studentFace = studentList.get(i);
                                try {
                                    registerInfoList.add(new FaceRegisterInfo(Base64.decode(studentFace.getFaceFeature(), Base64.DEFAULT), studentFace.getStudentCode()));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
                } else {
                    ToastUtils.showShort("请启用人脸识别与激活");
                }

            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });


    }

    private void showAuthCodeDialog() {
        //每次调用都需要重新生成,因为每次要生成新的验证码
//        final EditText editText = new EditText(context);
//        //设置只允许输入数字
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        editText.setSingleLine();
//        editText.setBackgroundColor(0xffcccccc);
        final int authCode = (int) (Math.random() * 9000 + 1000);
        Logger.i("生成验证码:" + authCode);

        new EditDialog.Builder(this).setTitle("清空本地日志文件")
                .setCanelable(false)
                .setMessage(String.format(getString(com.feipulai.common.R.string.clear_data_content), "\n" + authCode))
                .setEditHint(String.format(getString(com.feipulai.common.R.string.clear_data_content), ""))
                .setPositiveButton(new EditDialog.OnConfirmClickListener() {
                    @Override
                    public void OnClickListener(Dialog dialog, String content) {
                        FileUtil.deleteDirectory(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MyApplication.PATH_LOG_NAME);
                        LogUtils.initLogger(true, true, MyApplication.PATH_LOG_NAME);
                        toastSpeak("日志文件删除成功");
                    }
                })
                .build().show();

    }
}
