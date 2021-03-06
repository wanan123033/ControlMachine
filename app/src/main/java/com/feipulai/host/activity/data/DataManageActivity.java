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

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
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
import com.ww.fpl.libarcface.common.Constants;
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
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Headers;

/**
 * ????????????
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
    private boolean isDelPhoto, isDelAFR, isDelBase; //??????????????????

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
                R.mipmap.icon_result_import, R.mipmap.icon_template_export, R.mipmap.icon_position_import,
                R.mipmap.icon_position_down, R.mipmap.icon_position_down, R.mipmap.icon_position_down, R.mipmap.icon_data_clear};
        for (int i = 0; i < typeName.length; i++) {
            TypeListBean bean = new TypeListBean();
            bean.setName(typeName[i]);
            bean.setImageRes(typeRes[i]);
            typeDatas.add(bean);
        }
        IndexTypeAdapter indexTypeAdapter = new IndexTypeAdapter(this, typeDatas);//?????????????????????
        gridViewpager.setGVPAdapter(indexTypeAdapter);
        CommonNavigator commonNavigator = new CommonNavigator(this);//?????????
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
                commonPagerTitleView.setContentView(view);//????????????????????????????????????????????????????????????????????????????????????
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
        ViewPagerHelper.bind(indicatorContainer, gridViewpager);//??????????????????????????????

        indexTypeAdapter.setOnItemClickListener(new OnItemClickListener<TypeListBean>() {
            @Override
            public void onItemClick(View view, int position, TypeListBean data) {
                Intent intent = new Intent();
                switch (position) {
                    case 0://????????????
                        //????????????,?????????????????????????????????
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_FILE);
                        startActivityForResult(intent, REQUEST_CODE_IMPORT);
                        break;


                    case 1: //????????????
                        showDownLoadDialog();
                        break;

                    case 2: //???????????????
                        //???????????????????????????
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_BACKUP);

                        break;

                    case 3://????????????
                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_FOR_RESTORE, DataManageActivity.this).process();
                        break;
                    case 4: //????????????
                        intent.setClass(DataManageActivity.this, DataRetrieveActivity.class);
                        startActivity(intent);
                        break;
                    case 5: //????????????
                        new ClearDataDialog(DataManageActivity.this, new ClearDataDialog.OnProcessFinishedListener() {
                            @Override
                            public void onClearConfirmed(boolean isDeletePhoto, boolean isDeleteAFR, boolean isDeleteBase) {
                                isDelPhoto = isDeletePhoto;
                                isDelAFR = isDeleteAFR;
                                isDelBase = isDeleteBase;
                                new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                            }
                        }).showDialog();
//                        new DBDataCleaner(DataManageActivity.this, ClearDataProcess.CLEAR_DATABASE, DataManageActivity.this).process();
                        break;

                    case 6://????????????
//                        List<RoundResult> resultList = DBManager.getInstance().getResultsAll();
                        List<String> resultList = DBManager.getInstance().getResultsStudentByItem(TestConfigs.getCurrentItemCode());
                        if (resultList.size() == 0) {
                            ToastUtils.showShort(getString(R.string.item_result_already_upload));
                        } else {
//                            List<String> studList = resultList.subList(10 * 100, (10 + 1) * 100);
                            //??????????????????????????????????????????
                            uploadData(resultList);
                        }
                        break;

                    case 7://????????????
                        //??????????????????????????????????????????
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT);

                        break;

//                    case 4://????????????
//                    case 3://????????????
//                    case 5://????????????
                    //TODO ????????????
//                        DBManager.getInstance().roundResultClear();
//                        ToastUtils.showShort("??????????????????????????????");
//                        break;
                    case 8://????????????
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_EXPORT_TEMPLATE);
                        break;
                    case 9://????????????
                        LogUtils.operation("???????????????????????????...");
                        intent.setClass(DataManageActivity.this, FileSelectActivity.class);
                        intent.putExtra(FileSelectActivity.INTENT_ACTION, FileSelectActivity.CHOOSE_DIR);
                        startActivityForResult(intent, REQUEST_CODE_PHOTO);
                        break;
                    case 10://????????????
                        LogUtils.operation("???????????????????????????...");
//                        uploadPortrait();
//                        ToastUtils.showShort("??????????????????????????????");
                        showDownLoadPhotoDialog();
                        break;
                    case 11://????????????
                        LogUtils.operation("?????????????????????????????????...");
                        uploadFace();
                        break;
                    case 12://????????????
                        LogUtils.operation("???????????????????????????...");
                        getAPPS();
                        break;
                    case 13://????????????
                        new SweetAlertDialog(DataManageActivity.this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(com.feipulai.common.R.string.clear_dialog_title))
                                .setContentText("??????????????????????????????")
                                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                showAuthCodeDialog();
                            }
                        }).setCancelText(getString(com.feipulai.common.R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
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

    private void getAPPS() {
//        OperateProgressBar.showLoadingUi(DataManageActivity.this, "????????????????????????...");
        final HttpSubscriber subscriber = new HttpSubscriber();
        String version = SystemBrightUtils.getCurrentVersion(this);
        subscriber.getApps(this, version, new OnResultListener<List<SoftApp>>() {

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
     * app????????????
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
                        LogUtils.operation("????????????===>" + batch);
                    }

                    @Override
                    public void onResponse(Headers headers) {
                        LogUtils.operation("??????????????????===>" + batch);
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
                        LogUtils.operation("????????????===>" + batch);
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
            ToastUtils.showShort("??????????????????");
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
                    //????????????????????????
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
            itemList.add("????????????");
            itemList.add("????????????");
        }
        String[] item = itemList.toArray(new String[itemList.size()]);

        new AlertDialog.Builder(this)
                .setTitle("????????????")
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
                Logger.i(restoreSuccess ? ("?????????????????????,????????????:" + FileSelectActivity.sSelectedFile.getName())
                        : "?????????????????????");
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                DBManager.getInstance().initDB();
                TestConfigs.init(this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                initAfrCount();
                break;
            case REQUEST_CODE_EXPORT_TEMPLATE:

                boolean copySucceed;
                try {
                    UsbFile targetFile = FileSelectActivity.sSelectedFile.createFile("????????????????????????.xls");
                    copySucceed = FileUtil.copyFromAssets(getResources().getAssets(), "????????????????????????.xls", targetFile);
                    UsbFile deleteFile = FileSelectActivity.sSelectedFile.createFile(".????????????????????????delete.xls");
                    deleteFile.delete();
                    ToastUtils.showShort(copySucceed ? "??????????????????" : "??????????????????");

                    FileSelectActivity.sSelectedFile = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("??????????????????,??????????????????????????????????????????");
                    Logger.i("??????????????????,??????????????????");
                }
                break;
            case REQUEST_CODE_BACKUP:
                showBackupFileNameDialog();
                break;

            case REQUEST_CODE_IMPORT:
                OperateProgressBar.showLoadingUi(this, getString(R.string.read_exl_hint));
                //???????????????????????????????????????
                isProcessingData = true;
                Logger.i("exel????????????,?????????:" + FileSelectActivity.sSelectedFile.getName());
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
                    //????????????????????????????????????????????????
                    Bitmap bitmap = ImageUtil.getSmallBitmap(jpgFile.getAbsolutePath());
                    if (bitmap == null) {
                        continue;
                    }
                    Student student = DBManager.getInstance().queryStudentByStuCode(jpgFile.getName().substring(0, jpgFile.getName().indexOf(".")));
                    if (student != null) {
                        ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, student.getStudentCode() + ".jpg", bitmap);

                    }
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
                    if (student != null && success != null) {
                        student.setFaceFeature(Base64.encodeToString(success, Base64.DEFAULT));
                        DBManager.getInstance().updateStudent(student);
                    }
                    if (success == null) {
                        Log.e("faceRegister", "??????????????????" + studentCode);

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
//            ToastUtils.showShort("????????????????????????????????????????????????????????????");
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
//                        Log.e("faceRegister", "??????????????????" + studentList.get(i).getStudentCode());
//                    } else {
//                        successCount++;
//                    }
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastUtils.showShort("??????????????????");
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
        DataBaseExecutor.addTask(new DataBaseTask(this, "????????????????????????????????????...", false) {
            @Override
            public DataBaseRespon executeOper() {
                return new DataBaseRespon(true, "", DBManager.getInstance().queryStudentFeatures());
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                final List<Student> studentList = (List<Student>) respon.getObject();
                if (studentList == null || studentList.size() == 0) {
                    ToastUtils.showShort("????????????????????????????????????????????????????????????");
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
                            if (FaceServer.getFaceRegisterInfoList() != null) {
                                FaceServer.getFaceRegisterInfoList().clear();
                            }

                            List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                            for (int i = 0; i < studentList.size(); i++) {

                                Student student = studentList.get(i);

                                try {
                                    registerInfoList.add(new FaceRegisterInfo(Base64.decode(student.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));

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
                                    ToastUtils.showShort("????????????????????????");
                                    progressDialog.dismiss();
                                }
                            });
                            Log.i("DataManageActivity", "run: " + executorService.isShutdown());
                        }
                    });
                } else {
                    ToastUtils.showShort("??????????????????????????????");
                }
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
                        DateUtil.getCurrentTime("yyyy???MMddHHmmss")))
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
                    //???????????????????????????????????????
                    isProcessingData = true;
                    new ResultExlWriter(DataManageActivity.this).writeExelData(targetFile);

                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort(R.string.file_create_failed);
                    Logger.i("??????????????????,Exel????????????");
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
//                    Logger.i(backupSuccess ? ("?????????????????????,???????????????:" +
//                            FileSelectActivity.sSelectedFile.getName() + "/" + targetFile.getName())
//                            : "?????????????????????");
//                    FileSelectActivity.sSelectedFile = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    ToastUtils.showShort(R.string.file_create_failed);
//                    Logger.i("??????????????????,?????????????????????");
//                }
            }
        });
    }


    private DateFormat df = new SimpleDateFormat("HH:mm:ss");

    private void backData(final String fileName) {

        OperateProgressBar.showLoadingUi(DataManageActivity.this, "???????????????...");
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
                    ToastUtils.showShort(backupSuccess ? "?????????????????????" : "?????????????????????");

                } catch (IOException e) {
                    e.printStackTrace();
                    ToastUtils.showShort("??????????????????,??????????????????????????????????????????");
                    Logger.i("??????????????????,?????????????????????");
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
                                        OperateProgressBar.showLoadingUi(DataManageActivity.this, "?????????????????????...");

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
        // ?????????????????????
        try {
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + backUp);
//            File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + excel);
//                File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/KS_LOGGER/operationLogger");
            ZipFile zipFile = new ZipFile(zipPathName);
            ZipParameters parameters = new ZipParameters();
            // ????????????
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            // ????????????
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
            zipFile.addFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MyApplication.LOG_PATH_NAME, parameters);

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

        DataBaseExecutor.addTask(new DataBaseTask(this, "????????????????????????????????????", false) {
            @Override
            public DataBaseRespon executeOper() {
                if (isDelBase) {
                    boolean autoBackup = backupManager.autoBackup();
                    Logger.i(autoBackup ? "??????????????????" : "??????????????????");
                    DBManager.getInstance().clear();
                    SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                    SharedPrefsUtil.putValue(DataManageActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, null);
                    SharedPrefsUtil.remove(DataManageActivity.this, DownLoadPhotoHeaders.class);

                }

                if (isDelPhoto) {
                    FileUtil.delete(MyApplication.PATH_IMAGE);//????????????
                    FileUtil.mkdirs(MyApplication.PATH_IMAGE);
                }
                if (isDelAFR) {
                    DBManager.getInstance().clearFace();
                    FaceServer.getInstance().unInit();
                    FaceServer.getInstance().init(DataManageActivity.this);
                }
//                FileUtil.delete(MyApplication.BACKUP_DIR);
//                FileUtil.mkdirs2(MyApplication.BACKUP_DIR);
                FileUtil.delete(FaceServer.ROOT_PATH);
                FileUtil.mkdirs2(FaceServer.ROOT_PATH);


                return new DataBaseRespon(true, "", "");
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Logger.i("??????????????????");
                ToastUtils.showShort("??????????????????");
                if (isDelBase) {
                    DBManager.getInstance().initDB();
                    TestConfigs.init(DataManageActivity.this, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                }
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
    //     ToastUtils.showShort("????????????????????????????????????");
    //     Logger.i("????????????????????????????????????");
    // }
    public void uploadData(final List<String> resultList) {
        DataBaseExecutor.addTask(new DataBaseTask(this, "???????????????????????????...", false) {
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

    private void showAuthCodeDialog() {
        //?????????????????????????????????,????????????????????????????????????
//        final EditText editText = new EditText(context);
//        //???????????????????????????
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        editText.setSingleLine();
//        editText.setBackgroundColor(0xffcccccc);
        final int authCode = (int) (Math.random() * 9000 + 1000);
        Logger.i("???????????????:" + authCode);

        new EditDialog.Builder(this).setTitle("????????????????????????")
                .setCanelable(false)
                .setMessage(String.format(getString(com.feipulai.common.R.string.clear_data_content), "\n" + authCode))
                .setEditHint(String.format(getString(com.feipulai.common.R.string.clear_data_content), ""))
                .setPositiveButton(new EditDialog.OnConfirmClickListener() {
                    @Override
                    public void OnClickListener(Dialog dialog, String content) {
                        FileUtil.delete(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MyApplication.LOG_PATH_NAME);
                        LogUtils.initLogger(true, true, MyApplication.LOG_PATH_NAME);
                        toastSpeak("????????????????????????");
                    }
                })
                .build().show();

    }
}
