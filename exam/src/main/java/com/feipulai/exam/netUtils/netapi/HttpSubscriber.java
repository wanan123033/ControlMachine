package com.feipulai.exam.netUtils.netapi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ImageUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.tcp.PackageHeadInfo;
import com.feipulai.device.tcp.SendTcpClientThread;
import com.feipulai.device.tcp.TCPConst;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.ActivateBean;
import com.feipulai.exam.bean.BatchBean;
import com.feipulai.exam.bean.GroupBean;
import com.feipulai.exam.bean.ItemBean;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.RoundScoreBean;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.bean.SoftApp;
import com.feipulai.exam.bean.StudentBean;
import com.feipulai.exam.bean.UpdateApp;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.bean.UserBean;
import com.feipulai.exam.bean.UserPhoto;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.ItemSchedule;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Schedule;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentFace;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.HttpResult;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.RequestSub;
import com.feipulai.exam.netUtils.TCPResultPackage;
import com.feipulai.exam.netUtils.download.DownLoadProgressDialog;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.utils.EncryptUtil;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.observers.DefaultObserver;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by zzs on  2018/12/29
 * ??????????????????????????????????????????   ????????????:??????
 */
public class HttpSubscriber {

    public final static int SCHEDULE_BIZ = 2002;
    public final static int ITEM_BIZ = 2001;
    public final static int STUDENT_BIZ = 2003;
    public final static int GROUP_BIZ = 2004;
    public final static int GROUP_INFO_BIZ = 2005;
    public final static int UPLOAD_BIZ = 4001;
    public final static int ROUNDRESULT_BIZ = 5001;


    private OnRequestEndListener onRequestEndListener;

    public void setOnRequestEndListener(OnRequestEndListener onRequestEndListener) {
        this.onRequestEndListener = onRequestEndListener;
    }

    /**
     * ??????
     *
     * @param currentRunTime
     * @param listener
     */
    public void activate(long currentRunTime, int distribution, OnResultListener listener) {
        //??????????????????
        HttpManager.DEFAULT_CONNECT_TIMEOUT = 5;
        HttpManager.DEFAULT_READ_TIMEOUT = 5;
        HttpManager.DEFAULT_WRITE_TIMEOUT = 5;
        HttpManager.resetManager();
        Map<String, String> parameData = new HashMap<>();
        parameData.put("deviceIdentify", CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("currentRunTime", currentRunTime + "");
        parameData.put("softwareCode", MyApplication.SOFTWAREUUID);
        parameData.put("softwareName", CommonUtils.getAppName(MyApplication.getInstance()));
        parameData.put("keyType", "0");
        parameData.put("systemEnvironment", "Android");
        parameData.put("distribution", distribution + "");
        Observable<HttpResult<ActivateBean>> observable = HttpManager.getInstance().getHttpApi().activate(CommonUtils.encryptQuery("300021100", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<ActivateBean>(listener));

    }

    /**
     * ????????????
     *
     * @param crashMsg
     */
    public void uploadLog(String crashMsg) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, String> parameData = new HashMap<>();
        parameData.put("logLevel", "ERROR");
        parameData.put("errorText", crashMsg);
        parameData.put("deviceIdentify", CommonUtils.getDeviceId(MyApplication.getInstance()));
        parameData.put("operateName", "????????????");
        parameData.put("softwareCode", MyApplication.SOFTWAREUUID);
        parameData.put("softwareName", CommonUtils.getAppName(MyApplication.getInstance()));
        Observable<HttpResult<String>> observable = HttpManager.getInstance().getHttpApi().uploadLog(CommonUtils.encryptQuery("300021300", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<String>(new OnResultListener() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("zzs==============>??????????????????");
            }

            @Override
            public void onFault(int code, String errorMsg) {
                System.out.println("zzs==============>??????????????????");
            }

            @Override
            public void onResponseTime(String responseTime) {

            }
        }));
    }

    /**
     * ????????????
     *
     * @param username
     * @param password
     */
    public void login(Context context, String username, String password, OnResultListener listener) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, String> parameData = new HashMap<>();
        parameData.put("username", username + "@" + CommonUtils.getDeviceId(context));
//        parameData.put("username", username);
        parameData.put("password", password);
        //TODO ????????????????????????????????????????????????????????????
        String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login("Basic " + serverToken, parameData);
//        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login(CommonUtils.query("1001", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserBean>(listener, context));
    }

    /**
     * ??????????????????
     */
    public void getScheduleAll() {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Observable<HttpResult<ScheduleBean>> observable = HttpManager.getInstance().getHttpApi().getScheduleAll("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery(SCHEDULE_BIZ + "", null));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<ScheduleBean>(new OnResultListener<ScheduleBean>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(ScheduleBean result) {
                LogUtils.net("?????????????????????????????????" + result.toString());
                if (result == null) {
                    if (onRequestEndListener != null) {
                        onRequestEndListener.onFault(SCHEDULE_BIZ);
                    }
                    return;
                }

                ScheduleBean.SITE_EXAMTYPE = result.getExamType();
                List<Schedule> dbScheduleList = new ArrayList<>();
                List<ItemSchedule> dbItemScheduleList = new ArrayList<>();
                for (ScheduleBean.ResponseSchedule scheduleBean : result.getSiteScheduleInfoVOList()) {
                    Schedule schedule = new Schedule();
                    schedule.setScheduleNo(scheduleBean.getScheduleNo());
                    schedule.setBeginTime(scheduleBean.getBeginTime());
                    schedule.setEndTime(schedule.getEndTime());
                    dbScheduleList.add(schedule);

                    for (ItemBean item : scheduleBean.getExamItemVOList()) {
                        //???????????????????????????????????????????????????
                        if (TextUtils.isEmpty(item.getExamItemCode())) {
                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                            ToastUtils.showShort("?????????????????????????????????????????????????????????????????????");
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onFault(SCHEDULE_BIZ);
                            }
                            return;
                        }
                        ItemSchedule itemSchedule = new ItemSchedule();
                        itemSchedule.setScheduleNo(scheduleBean.getScheduleNo());
                        itemSchedule.setItemCode(item.getExamItemCode());
                        dbItemScheduleList.add(itemSchedule);
                    }
                }
                DBManager.getInstance().insertSchedulesList(dbScheduleList);
                DBManager.getInstance().insertItemSchedulesList(dbItemScheduleList);
                if (onRequestEndListener != null)
                    onRequestEndListener.onSuccess(SCHEDULE_BIZ);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("???????????????" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(SCHEDULE_BIZ);
                }
            }
        }));
    }

    /**
     * ??????????????????????????????
     */
    public void getItemAll(final Context context) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Observable<HttpResult<List<ItemBean>>> observable = HttpManager.getInstance().getHttpApi().getItemAll("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(ITEM_BIZ + "", null));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<ItemBean>>(new OnResultListener<List<ItemBean>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(List<ItemBean> result) {
                LogUtils.net("???????????????????????????????????????" + result.toString());
                if (result == null)
                    return;
                List<Item> itemList = new ArrayList<>();
                for (ItemBean itemBean : result) {
                    //???????????????????????????????????????????????????
                    if (TextUtils.isEmpty(itemBean.getExamItemCode())) {
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                        ToastUtils.showShort("???????????????????????????????????????????????????????????????");
                        if (onRequestEndListener != null) {
                            onRequestEndListener.onFault(ITEM_BIZ);
                        }
                        return;
                    }

                    if (TextUtils.isEmpty(itemBean.getMachineCode())) {
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                        ToastUtils.showShort("??????????????????????????????????????????????????????????????????");
                        if (onRequestEndListener != null) {
                            onRequestEndListener.onFault(ITEM_BIZ);
                        }
                        return;
                    }
                    if (TextUtils.equals(itemBean.getMachineCode(), "0")) {
                        continue;
                    }

                    Item item = new Item();
                    item.setCarryMode(itemBean.getCarryMode());
                    item.setDigital(itemBean.getDecimalDigits());
                    item.setItemCode(itemBean.getExamItemCode());
                    item.setItemName(itemBean.getItemName());
                    item.setLastResultMode(itemBean.getLastResultMode());
                    try {
                        item.setMachineCode(Integer.valueOf(itemBean.getMachineCode()));
                    } catch (Exception e) {
                        ToastUtils.showShort("???????????????????????????????????????");
                        return;
                    }
                    item.setMaxValue(itemBean.getMaxResult());
                    item.setMinValue(itemBean.getMinResult());
                    item.setTestNum(itemBean.getResultTestNum());
                    item.setTestType(itemBean.getTestType());
                    item.setUnit(itemBean.getResultUnit());
                    itemList.add(item);
                }
                if (TestConfigs.sCurrentItem != null)
                    if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                        boolean initState = TestConfigs.initZCP(itemList);
                        if (initState) {
                            DBManager.getInstance().insertItems(itemList);
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onSuccess(ITEM_BIZ);
                            }
                        } else {
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onFault(ITEM_BIZ);
                            }
                        }
                        return;
                    }
                DBManager.getInstance().insertItems(itemList);
                if (TestConfigs.sCurrentItem != null) {
                    List<Item> items = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    //????????????????????????
                    if (TestConfigs.sCurrentItem.getItemCode() == null) {
                        if (items.size() == 1) {
                            //???????????????????????????????????????????????????
                            if (TextUtils.isEmpty(items.get(0).getItemCode())) {
                                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                                ToastUtils.showShort("????????????????????????????????????????????????");
                                if (onRequestEndListener != null) {
                                    onRequestEndListener.onFault(ITEM_BIZ);
                                }
                                return;
                            }
                        }
                        int initState = TestConfigs.init(context, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                                    EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                                    ToastUtils.showShort("????????????????????????????????????????????????");
                                    if (onRequestEndListener != null) {
                                        onRequestEndListener.onFault(ITEM_BIZ);
                                    }
                                    return;
                                }
                                if (onRequestEndListener != null)
                                    onRequestEndListener.onSuccess(ITEM_BIZ);
                            }
                        });
                        if (onRequestEndListener != null) {
                            if (initState == TestConfigs.INIT_SUCCESS && !TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                                onRequestEndListener.onSuccess(ITEM_BIZ);
                            } else {
                                onRequestEndListener.onFault(ITEM_BIZ);
                            }
                        }


                    } else {
                        if (onRequestEndListener != null)
                            onRequestEndListener.onSuccess(ITEM_BIZ);
                    }
                } else {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(ITEM_BIZ);
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("???????????????" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(ITEM_BIZ);
                }
            }
        }));
    }

    //    /**
//     * ???????????????????????????????????????default???????????????????????????????????????
//     */
//    private void fillItemCodes() {
//        List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
//        for (StudentItem stuItem : studentItems) {
//            stuItem.setItemCode(TestConfigs.sCurrentItem.getItemCode());
//        }
//        DBManager.getInstance().updateStudentItem(studentItems);
//        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
//        if (roundResults.size() > 0) {
//            for (RoundResult roundResult : roundResults) {
//                roundResult.setItemCode(TestConfigs.sCurrentItem.getItemCode());
//            }
//            DBManager.getInstance().updateRoundResult(roundResults);
//        }
//        if (onRequestEndListener != null)
//            onRequestEndListener.onSuccess(ITEM_BIZ);
//    }
//
//    private void showSelectItemDialog(final Context context, final List<Item> items) {
//        String[] itemData = new String[items.size()];
//        for (int i = 0; i < items.size(); i++) {
//            itemData[i] = items.get(i).getItemName();
//        }
//        new AlertDialog.Builder(context).setTitle("?????????????????????")
//                .setCancelable(false)
//                .setItems(itemData, new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //???????????????????????????????????????????????????
//                        if (TextUtils.isEmpty(items.get(which).getItemCode())) {
//                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
//                            ToastUtils.showShort("????????????????????????????????????????????????");
//                            if (onRequestEndListener != null) {
//                                onRequestEndListener.onFault(ITEM_BIZ);
//                            }
//                            return;
//                        }
//                        TestConfigs.sCurrentItem = items.get(which);
//                        SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, TestConfigs.getCurrentItemCode());
//                        // ???????????????????????????????????????default???????????????????????????????????????
//                        fillItemCodes();
//
//                    }
//                }).create().show();
//    }
    private List<String> stuList = new ArrayList<>();

    // ??????????????????
    public void getItemStudent(final String lastDownLoadTime, final String itemCode, int batch, final int examType) {
        getItemStudent(null, itemCode, batch, examType, lastDownLoadTime, new String[]{});
    }

    // ??????????????????
    public void getItemStudent(final String itemCode, int batch, final int examType) {
        getItemStudent(null, itemCode, batch, examType, "0", new String[]{});
    }

    public void getItemStudent(String lastDownLoadTime, final String itemCode, int batch, int examType, String scheduleNo) {
        getItemStudent(scheduleNo, itemCode, batch, examType, lastDownLoadTime, new String[]{});
    }

    /**
     * ??????????????????
     */
    public void getItemGroupAll(final String itemCode, final String scheduleNo, int batch, final int examType) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, Object> parameData = new HashMap<>();
        parameData.put("examItemCode", itemCode);
        parameData.put("scheduleNo", scheduleNo);
        parameData.put("batch", batch);
        parameData.put("examType", examType);
        Observable<HttpResult<BatchBean<List<GroupBean>>>> observable = HttpManager.getInstance().getHttpApi().getGroupAll("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(GROUP_BIZ + "", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<BatchBean<List<GroupBean>>>(new OnResultListener<BatchBean<List<GroupBean>>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(BatchBean<List<GroupBean>> result) {

                if (result == null || result.getDataInfo() == null) {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(GROUP_BIZ);
                    return;
                }
                LogUtils.net("???????????????????????????" + result.toString());
                final List<Group> groupList = new ArrayList<>();
                final List<GroupItem> groupItemList = new ArrayList<>();

                for (GroupBean groupBean : result.getDataInfo()) {
                    //int groupType, String sortName, int groupNo, String scheduleNo,String itemCode, int examType, int isTestComplete
                    Group group = new Group(groupBean.getGroupType(), groupBean.getSortName(), groupBean.getGroupNo()
                            , groupBean.getScheduleNo(), itemCode, groupBean.getExamType(), 0);
                    groupList.add(group);

                    if (groupBean.getStudentCodeList() != null) {
                        for (StudentBean student : groupBean.getStudentCodeList()) {
                            //String itemCode, int groupType, String sortName, int groupNo, String scheduleNo, String studentCode, int trackNo, int identityMark
                            GroupItem groupItem = new GroupItem(itemCode, groupBean.getGroupType(), groupBean.getSortName(), groupBean.getGroupNo()
                                    , groupBean.getScheduleNo(), student.getStudentCode(), student.getTrackNo(), 0, groupBean.getExamType());
                            groupItemList.add(groupItem);
                        }
                    }

                }

                DBManager.getInstance().insertGroupList(groupList);
                DBManager.getInstance().insertGroupItemList(groupItemList);
                if (result.getBatch() < result.getBatchTotal()) {
                    getItemGroupAll(itemCode, scheduleNo, result.getBatch() + 1, examType);
                } else {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(GROUP_BIZ);
                }


            }

            @Override
            public void onFault(int code, String errorMsg) {
                Logger.i("getItemGroupAll  onFault");
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("???????????????" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(GROUP_BIZ);
                }
            }
        }));
    }

    /**
     * ????????????????????????
     *
     * @param itemCode
     * @param batch
     * @param examType
     */
    public void getItemStudent(String scheduleNo, final String itemCode, int batch, final int examType, final String lastDownLoadTime, final String... studentCode) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, Object> parameData = new HashMap<>();
        parameData.put("examItemCode", itemCode);
        parameData.put("batch", batch);
        parameData.put("examType", examType);
        if (studentCode != null && studentCode.length != 0) {
            parameData.put("studentCodeList", studentCode);
        }
        if (!TextUtils.isEmpty(scheduleNo)) {
            parameData.put("scheduleNo", scheduleNo);
        }
        Observable<HttpResult<BatchBean<List<StudentBean>>>> observable = HttpManager.getInstance().getHttpApi().getStudent("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(STUDENT_BIZ + "", lastDownLoadTime, parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<BatchBean<List<StudentBean>>>(new OnResultListener<BatchBean<List<StudentBean>>>() {
            @Override
            public void onResponseTime(String responseTime) {
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS,
                        SharedPrefsConfigs.LAST_DOWNLOAD_TIME, responseTime);
            }

            @Override
            public void onSuccess(BatchBean<List<StudentBean>> result) {
                LogUtils.net("?????????????????????????????????" + result.toString());
                if (result.getBatch() == 1) {
                    stuList.clear();
                }
                Set<String> supplements = new HashSet<>();// ????????????????????????
                if (result == null || result.getDataInfo() == null || result.getDataInfo().size() == 0) {
                    ToastUtils.showShort("???????????????????????????");
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(STUDENT_BIZ);
                    return;
                }

                final List<Student> studentList = new ArrayList<>();
                final List<StudentItem> studentItemList = new ArrayList<>();
                List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                List<StudentFace> studentFaces = new ArrayList<>();
                for (StudentBean studentBean : result.getDataInfo()) {
                    if (!stuList.contains(studentBean.getIdCard())) {
                        stuList.add(studentBean.getIdCard());
                    } else {
                        Logger.i("????????????====???" + studentBean.toString());
                    }
                    if (studentBean.getExamType() != 2 && supplements.contains(studentBean.getStudentCode())) {
                        // ????????????????????????,??????????????????????????????,????????????????????????,?????????
                        continue;
                    }
                    if (studentBean.getExamType() == 2) {
                        supplements.add(studentBean.getStudentCode());
                    }
//                    Logger.i("getItemStudent" + studentBean.toString());
                    Student student = new Student();
                    student.setSex(studentBean.getGender());
                    student.setSchoolName(studentBean.getSchoolName());
                    student.setClassName(studentBean.getClassName());
                    student.setStudentName(studentBean.getStudentName());
                    student.setStudentCode(studentBean.getStudentCode());
                    if (!TextUtils.isEmpty(studentBean.getFaceFeature())) {
                        StudentFace face = new StudentFace();
                        face.setStudentCode(studentBean.getStudentCode());
                        face.setFaceFeature(studentBean.getFaceFeature());
                        studentFaces.add(face);
                    }
                    //                    student.setPortrait(studentBean.getPhotoData());
                    //TODO ???????????????????????????????????????OOM??? ???????????????????????????????????????
                    if (studentBean.getPhotoData() != null) {
                        ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, studentBean.getStudentCode() + ".jpg", ImageUtil.base64ToBitmap(studentBean.getPhotoData()));
                    }
                    student.setFaceFeature(studentBean.getFaceFeature());
                    student.setIdCardNo(TextUtils.isEmpty(studentBean.getIdCard()) ? null : EncryptUtil.setEncryptString(Student.ENCRYPT_KEY, studentBean.getIdCard()));
                    studentList.add(student);
                    if (ScheduleBean.SITE_EXAMTYPE == 0) {
                        SettingHelper.getSystemSetting().setTestPattern(SystemSetting.PERSON_PATTERN);
//                        if (studentBean.getExamType() == 0 || studentBean.getExamType() == 1) {
//                            StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
//                                    studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
//                                    studentBean.getExamType(), 0, studentBean.getScheduleNo());
//                            studentItemList.add(studentItem);
//                        } else if (studentBean.getExamType() == 2) {
//                            StudentItem dbStuItem = DBManager.getInstance().queryStudentItemByCode(studentBean.getExamItemCode(), studentBean.getStudentCode());
//                            StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
//                                    studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
//                                    dbStuItem == null ? 0 : dbStuItem.getExamType(), 1, studentBean.getScheduleNo());
//                            studentItemList.add(studentItem);
//                        } else {
//                            StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
//                                    studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
//                                    1, 1, studentBean.getScheduleNo());
//                            studentItemList.add(studentItem);
//                        }
                        StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
                                studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
                                studentBean.getExamType(), studentBean.getScheduleNo());
                        studentItemList.add(studentItem);
                    } else {
                        SettingHelper.getSystemSetting().setTestPattern(SystemSetting.GROUP_PATTERN);
//                        StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
//                                studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
//                                studentBean.getExamType(), studentBean.getScheduleNo());
//                        studentItemList.add(studentItem);
                    }
                    if (!TextUtils.isEmpty(student.getFaceFeature())) {
                        registerInfoList.add(new FaceRegisterInfo(Base64.decode(student.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));
                    }
                }
                if (SettingHelper.getSystemSetting().getCheckTool() == 4) {
                    FaceServer.getInstance().addFaceList(registerInfoList);
                }
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                DBManager.getInstance().insertStudentList(studentList);
                DBManager.getInstance().insertStuItemList(studentItemList);
                DBManager.getInstance().insertStudentFaces(studentFaces);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onRequestData(studentList);
                }
                if (result.getBatch() < result.getBatchTotal()) {
                    getItemStudent(null, itemCode, result.getBatch() + 1, examType, lastDownLoadTime, studentCode);
                } else {

                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(STUDENT_BIZ);
                }


            }

            @Override
            public void onFault(int code, String errorMsg) {
                Logger.i("getItemStudent  onFault");
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("???????????????" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(STUDENT_BIZ);
                }
            }
        }));
    }

    /**
     * ??????????????????
     */
    public void getItemGroupInfo(final String itemCode, String scheduleNo, String sortName, String groupNo, String groupType) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, Object> parameData = new HashMap<>();
        parameData.put("examItemCode", itemCode);
        parameData.put("scheduleNo", scheduleNo);
        parameData.put("sortName", sortName);
        parameData.put("groupNo", groupNo);
        parameData.put("groupType", groupType);
        Observable<HttpResult<List<GroupBean>>> observable = HttpManager.getInstance().getHttpApi().getGroupInfo("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(GROUP_INFO_BIZ + "", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<GroupBean>>(new OnResultListener<List<GroupBean>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(List<GroupBean> result) {
//                Logger.i("getItemGroupInfo====>" + result.toString());
                if (result == null)
                    return;
                List<GroupItem> groupItemList = new ArrayList<>();
                for (GroupBean groupBean : result) {
                    if (groupBean.getStudentCodeList() != null) {
                        for (StudentBean student : groupBean.getStudentCodeList()) {
                            //String itemCode, int groupType, String sortName, int groupNo, String scheduleNo, String studentCode, int trackNo, int identityMark
                            GroupItem groupItem = new GroupItem(itemCode, groupBean.getGroupType(), groupBean.getSortName(), groupBean.getGroupNo()
                                    , groupBean.getScheduleNo(), student.getStudentCode(), student.getTrackNo(), 0, groupBean.getExamType());
                            groupItemList.add(groupItem);
                        }
                    }

                }
                DBManager.getInstance().insertGroupItemList(groupItemList);
                if (onRequestEndListener != null)
                    onRequestEndListener.onSuccess(GROUP_INFO_BIZ);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(GROUP_INFO_BIZ);
                }
            }
        }));
    }

    /**
     * ?????????????????????
     *
     * @param sitCode     ??????ID
     * @param scheduleNo  ????????????ID
     * @param itemCode    ????????????
     * @param studentCode ????????????
     * @param groupNo     ??????
     * @param sortName    ????????????
     * @param groupType   ???????????? 0.?????? 1.?????? 2.??????, ????????????
     * @param examStatus  ????????????0.?????????1.?????????2.??????
     */
    public void getRoundResult(String sitCode, final String scheduleNo, final String itemCode, final String studentCode,
                               String groupNo, String sortName, String groupType, final String examStatus,
                               final OnResultListener<RoundScoreBean> onResultListener) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("sitCode", sitCode);
        params.put("scheduleNo", scheduleNo);
        params.put("itemCode", itemCode);
        params.put("studentCode", studentCode);
        params.put("groupNo", groupNo);
        params.put("sortName", sortName);
        params.put("groupType", groupType);
        params.put("examStatus", examStatus);
        Logger.e("----sitCode=" + params.toString());

        Observable<HttpResult<RoundScoreBean>> observable = HttpManager.getInstance().getHttpApi().
                getRoundScore("bearer " + MyApplication.TOKEN,
                        CommonUtils.encryptQuery(ROUNDRESULT_BIZ + "", params));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<RoundScoreBean>(new OnResultListener<RoundScoreBean>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(RoundScoreBean result) {
                LogUtils.net("??????????????????????????????" + result.toString());
                if (result.getExist() == 1) {
                    List<RoundScoreBean.ScoreBean> scoreBeanList = result.getRoundList();
                    for (RoundScoreBean.ScoreBean score : scoreBeanList) {

                        RoundResult dbResult = DBManager.getInstance().queryFinallyRountScoreByExamType(studentCode, examStatus);
                        int testNo = dbResult == null ? 1 : dbResult.getTestNo();
                        RoundResult roundResult = new RoundResult();
                        roundResult.setStudentCode(studentCode);
                        roundResult.setItemCode(itemCode);
                        roundResult.setResult(Integer.parseInt(score.getResult()));
                        roundResult.setPenaltyNum(Integer.parseInt(score.getPenalty()));
                        roundResult.setRoundNo(Integer.parseInt(score.getRoundNo()));
                        roundResult.setTestNo(testNo);
                        roundResult.setScheduleNo(scheduleNo);
                        roundResult.setExamType(Integer.parseInt(examStatus));
                        roundResult.setResultState(score.resultStatus);
                        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                        roundResult.setStumbleCount(score.getStumbleCount());
                        roundResult.setTestTime(score.getTestTime());
                        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
                        roundResult.setMtEquipment(score.getMtEquipment());


                        RoundResult bestResult = DBManager.getInstance().queryBestScore(studentCode, testNo);
                        if (bestResult != null) {
                            // ???????????????????????? ????????????????????????????????????????????????????????????
                            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && score.resultStatus == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= roundResult.getResult()) {
                                // ????????????????????????????????????????????????
                                roundResult.setIsLastResult(1);
                                bestResult.setIsLastResult(0);
                                DBManager.getInstance().updateRoundResult(bestResult);

                            } else {
                                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                                    roundResult.setIsLastResult(1);
                                    bestResult.setIsLastResult(0);
                                    DBManager.getInstance().updateRoundResult(bestResult);

                                } else {
                                    roundResult.setIsLastResult(0);
                                }
                            }
                        } else {
                            // ???????????????
                            roundResult.setIsLastResult(1);
                        }
                        List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(studentCode);
                        boolean flag = false;
                        for (RoundResult re : results) {
                            if (re.getRoundNo() == Integer.valueOf(score.roundNo) &&
                                    re.getResult() == roundResult.getResult()) {
                                flag = true;
                                break;
                            } else {
                                flag = false;
                            }
                        }
                        if (!flag) {
                            DBManager.getInstance().insertRoundResult(roundResult);
                        }
                    }
                }

                if (onResultListener != null)
                    onResultListener.onSuccess(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
                if (onResultListener != null) {
                    onResultListener.onFault(code, errorMsg);
                }
            }
        }));
    }

    /**
     * http??????????????????
     *
     * @param uploadResultsList
     */
    public void uploadResult(final List<UploadResults> uploadResultsList) {
        int pageSum = 1;
        if (uploadResultsList.size() > 20) {
            //????????????????????????
            if (uploadResultsList.size() % 20 == 0) {
                pageSum = uploadResultsList.size() / 20;
            } else {
                pageSum = uploadResultsList.size() / 20 + 1;
            }
        }
        setUploadResult(0, pageSum, uploadResultsList);
    }

    /**
     * tcp??????????????????
     *
     * @param uploadResultsList
     */
    public void uploadResultTCP(Activity activity, final List<UploadResults> uploadResultsList) {
        int pageSum = 1;
        if (uploadResultsList.size() > 50) {
            //????????????????????????
            if (uploadResultsList.size() % 50 == 0) {
                pageSum = uploadResultsList.size() / 50;
            } else {
                pageSum = uploadResultsList.size() / 50 + 1;
            }
        }
        sendTcpResult(activity, 0, pageSum, uploadResultsList);
    }

    private SendTcpClientThread tcpClientThread;

    /**
     * ??????tcp ???????????????50???
     *
     * @param activity
     * @param pageNo
     * @param pageSum
     * @param uploadResultsList
     */
    private void sendTcpResult(final Activity activity, final int pageNo, final int pageSum, final List<UploadResults> uploadResultsList) {
        final List<UploadResults> uploadData;
        if (pageNo == pageSum - 1) {
            uploadData = uploadResultsList.subList(pageNo * 50, uploadResultsList.size());
        } else {
            uploadData = uploadResultsList.subList(pageNo * 50, (pageNo + 1) * 50);
        }

        LogUtils.net("?????????????????????" + uploadResultsList.toString());
        Logger.i("setUploadResult===>" + pageNo);

        TCPResultPackage rcPackage = new TCPResultPackage();
        rcPackage.m_nEventType = String.valueOf(TCPConst.enumEvent.EventAllData.getIndex());
        rcPackage.m_strPackType = "PFPAndroidSend";
        rcPackage.m_strEvent = TestConfigs.sCurrentItem.getItemName();
        rcPackage.m_nProperty = TestConfigs.sCurrentItem.getTestType();

        final String data = rcPackage.EncodePackage(TestConfigs.sCurrentItem, uploadData, new PackageHeadInfo(), false, TCPConst.enumCodeType.CodeGB2312.getIndex());
        Log.i("data---", data);
        LogUtils.net("?????????????????????????????????" + data);
        if (tcpClientThread == null) {
            String tcpIp = SettingHelper.getSystemSetting().getTcpIp();
            if (TextUtils.isEmpty(tcpIp)) {
                ToastUtils.showShort("TCP???????????????TCP??????????????????");
                onRequestEndListener.onFault(UPLOAD_BIZ);
                return;
            }
            String ipStr = tcpIp.split(":")[0];
            String portStr = tcpIp.split(":")[1];
            tcpClientThread = new SendTcpClientThread(ipStr, Integer.parseInt(portStr), new SendTcpClientThread.SendTcpListener() {
                @Override
                public void onMsgReceive(String text) {
                    //??????????????????
                    List<RoundResult> roundResultList = new ArrayList<>();
                    for (UploadResults uploadResults : uploadData) {
                        List<RoundResult> roundResults = RoundResultBean.dbCope(uploadResults.getRoundResultList());
                        for (RoundResult roundResult : roundResults) {
                            roundResult.setUpdateState(1);
                            roundResultList.add(roundResult);
                        }
                    }
                    Logger.i("setUploadResult===>" + pageNo + "   " + roundResultList.size() + "   ????????????");
                    DBManager.getInstance().updateRoundResult(roundResultList);

                    //??????????????????????????????????????????
                    if (pageNo == pageSum - 1) {
                        Logger.i("setUploadResult===>" + pageNo + " ????????????");
                        if (activity == null) {
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onSuccess(UPLOAD_BIZ);
                            }
                            return;
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("????????????");
                                if (onRequestEndListener != null) {
                                    onRequestEndListener.onSuccess(UPLOAD_BIZ);
                                }
                            }
                        });
                    } else {
                        sendTcpResult(activity, pageNo + 1, pageSum, uploadResultsList);
                    }
                }

                @Override
                public void onSendFail(final String msg) {
                    if (activity == null) {
                        if (onRequestEndListener != null) {
                            onRequestEndListener.onFault(UPLOAD_BIZ);
                        }
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("????????????:" + msg);
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onFault(UPLOAD_BIZ);
                            }
                        }
                    });
                }

                @Override
                public void onConnectFlag(boolean isConnect) {
                    Log.e("onConnectFlag", "---------" + isConnect);
                    if (isConnect) {
                        tcpClientThread.write(data);
                    } else {
                        tcpClientThread = null;
                        if (activity == null) {
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onFault(UPLOAD_BIZ);
                            }
                            return;
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("?????????????????????");
                                if (onRequestEndListener != null) {
                                    onRequestEndListener.onFault(UPLOAD_BIZ);
                                }
                            }
                        });
                    }
                }
            });
            tcpClientThread.start();
        } else {
            if (tcpClientThread.isInterrupted()) {
                tcpClientThread.start();
            }
        }
    }

    /**
     * ??????tcp??????
     */
    public void sendTestTcp(final Activity activity) {
        if (tcpClientThread == null) {
            String tcpIp = SettingHelper.getSystemSetting().getTcpIp();
            String ipStr = tcpIp.split(":")[0];
            String portStr = tcpIp.split(":")[1];
            tcpClientThread = new SendTcpClientThread(ipStr, Integer.parseInt(portStr), new SendTcpClientThread.SendTcpListener() {
                @Override
                public void onMsgReceive(String text) {
                    stopSendTcpThread();
                }

                @Override
                public void onSendFail(final String msg) {
                    stopSendTcpThread();
                }

                @Override
                public void onConnectFlag(boolean isConnect) {
                    if (isConnect) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("?????????????????????");
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("?????????????????????");
                            }
                        });
                    }
                    stopSendTcpThread();
                }
            });
            tcpClientThread.start();
        } else {
            if (tcpClientThread.isInterrupted()) {
                tcpClientThread.start();
            }
        }
    }


    public void stopSendTcpThread() {
        Log.i("stopSendTcpThread", "---------");
        if (tcpClientThread != null && tcpClientThread.isAlive()) {
            tcpClientThread.exit = true;
            tcpClientThread.interrupt();
        }
    }

    private void setUploadResult(final int pageNo, final int pageSum, final List<UploadResults> uploadResultsList) {
        LogUtils.net("?????????????????????" + uploadResultsList.toString());
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        final List<UploadResults> uploadData;
        if (pageNo == pageSum - 1) {
            uploadData = uploadResultsList.subList(pageNo * 20, uploadResultsList.size());
        } else {
            uploadData = uploadResultsList.subList(pageNo * 20, (pageNo + 1) * 20);
        }
        Logger.i("setUploadResult===>" + pageNo);
        Observable<HttpResult<List<UploadResults>>> observable = HttpManager.getInstance().getHttpApi().uploadResult("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(UPLOAD_BIZ + "", uploadData));

        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<UploadResults>>(new OnResultListener<List<UploadResults>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(List<UploadResults> result) {
                LogUtils.net("???????????????????????????" + result.toString());
                //??????????????????
                List<RoundResult> roundResultList = new ArrayList<>();
                for (UploadResults uploadResults : uploadData) {
                    if (result != null && result.contains(uploadResults)) {
                        continue;
                    }
                    List<RoundResult> roundResults = RoundResultBean.dbCope(uploadResults.getRoundResultList());
                    for (RoundResult roundResult : roundResults) {
                        roundResult.setUpdateState(1);
                        roundResultList.add(roundResult);
                    }
                }
                Logger.i("setUploadResult===>" + pageNo + "   " + roundResultList.size() + "   ????????????");
                DBManager.getInstance().updateRoundResult(roundResultList);

                //??????????????????????????????????????????
                if (pageNo == pageSum - 1) {
                    Logger.i("setUploadResult===>" + pageNo + " ????????????");
                    ToastUtils.showShort("????????????");
                    if (onRequestEndListener != null) {
                        onRequestEndListener.onSuccess(UPLOAD_BIZ);
                    }
                } else {
                    setUploadResult(pageNo + 1, pageSum, uploadResultsList);
                }

                if (result != null && result.size() > 0) {
                    String toastData = "?????????????????????";
                    for (UploadResults uploadResults : result) {
                        toastData += uploadResults.getStudentCode() + ",";
                    }
                    ToastUtils.showLong(toastData);
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort("????????????:" + errorMsg);

                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(UPLOAD_BIZ);
                }
            }
        }));
    }

    public void getApps(Context context, String version, final OnResultListener listener) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, String> parameData = new HashMap<>();
        parameData.put("softwareUuid", MyApplication.SOFTWAREUUID);
        parameData.put("hardwareUuid", MyApplication.HARDWAREUUID);
        parameData.put("version", version);
        parameData.put("deviceCode", MyApplication.DEVICECODE);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("Content-Type, application/json"), new JSONObject(parameData).toString());
        Observable<HttpResult<List<SoftApp>>> observable = HttpManager.getInstance().getHttpApi().getSoftApp(requestBody);
//        HttpManager.getInstance().changeBaseUrl("https://api.soft.fplcloud.com");
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<SoftApp>>(new OnResultListener<List<SoftApp>>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(List<SoftApp> result) {
                Log.i("SoftApp", "Observable");
                listener.onSuccess(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                Log.i("SoftApp", errorMsg);
                listener.onFault(code, errorMsg);
            }
        }));
    }

    /**
     * ??????APP?????????url
     *
     * @param version
     * @param updateSoftwareVersion
     * @param authorizeCode
     * @param listener
     */
    public void updateApp(String version, String updateSoftwareVersion, String authorizeCode,
                          final OnResultListener listener) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        Map<String, String> parameData = new HashMap<>();
        parameData.put("softwareUuid", MyApplication.SOFTWAREUUID);
        parameData.put("hardwareUuid", MyApplication.HARDWAREUUID);
        parameData.put("version", version);
        parameData.put("updateSoftwareVersion", updateSoftwareVersion);
        parameData.put("authorizeCode", authorizeCode);
        parameData.put("enableCompression", "0");
        parameData.put("deviceCode", MyApplication.DEVICECODE);
        final RequestBody requestBody = RequestBody.create(MediaType.parse("Content-Type, application/json"), new JSONObject(parameData).toString());
        Observable<HttpResult<UpdateApp>> observable = HttpManager.getInstance().getHttpApi().updateSoftApp(requestBody);
        HttpManager.getInstance().changeBaseUrl("https://api.soft.fplcloud.com");
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UpdateApp>(new OnResultListener<UpdateApp>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(UpdateApp result) {
                listener.onSuccess(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                Log.i("UpdateApp", code + errorMsg);
                listener.onFault(code, errorMsg);
            }
        }));


    }

    public void sendFaceOnline(String studentCode, String base64Face, String base64Feature) {
        if (HttpManager.DEFAULT_CONNECT_TIMEOUT == 5) {
            HttpManager.DEFAULT_CONNECT_TIMEOUT = 60;
            HttpManager.DEFAULT_READ_TIMEOUT = 60;
            HttpManager.DEFAULT_WRITE_TIMEOUT = 60;
            HttpManager.resetManager();
        }
        HashMap<String, Object> parameData = new HashMap<>();
        parameData.put("photoData", base64Face);
        parameData.put("studentCode", studentCode);
        parameData.put("faceFeature", base64Feature);
        Observable<HttpResult<UserPhoto>> observable = HttpManager.getInstance().getHttpApi().compareFaceFeature(
                "bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("8001", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserPhoto>(new OnResultListener<UserPhoto>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(UserPhoto result) {
                onRequestEndListener.onRequestData(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                onRequestEndListener.onFault(code);
            }
        }));
    }

    public interface OnRequestEndListener {
        void onSuccess(int bizType);

        void onFault(int bizType);

        void onRequestData(Object data);

    }


}
