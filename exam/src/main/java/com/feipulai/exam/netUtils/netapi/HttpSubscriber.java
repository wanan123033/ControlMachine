package com.feipulai.exam.netUtils.netapi;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.BatchBean;
import com.feipulai.exam.bean.GroupBean;
import com.feipulai.exam.bean.ItemBean;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.bean.StudentBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.bean.UserBean;
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
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.HttpResult;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.RequestSub;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by zzs on  2018/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class HttpSubscriber {

    public final static int SCHEDULE_BIZ = 2002;
    public final static int ITEM_BIZ = 2001;
    public final static int STUDENT_BIZ = 2003;
    public final static int GROUP_BIZ = 2004;
    public final static int GROUP_INFO_BIZ = 2005;
    public final static int UPLOAD_BIZ = 4001;


    private OnRequestEndListener onRequestEndListener;

    public void setOnRequestEndListener(OnRequestEndListener onRequestEndListener) {
        this.onRequestEndListener = onRequestEndListener;
    }

    /**
     * 用户登录
     *
     * @param username
     * @param password
     */
    public void login(Context context, String username, String password, OnResultListener listener) {
        Map<String, String> parameData = new HashMap<>();
        parameData.put("username", username + "@" + CommonUtils.getDeviceId(context));
//        parameData.put("username", username);
        parameData.put("password", password);
        //TODO 登录协议与其它接口分离，单传用户名和密码
        String serverToken = SharedPrefsUtil.getValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.DEFAULT_SERVER_TOKEN, "dGVybWluYWw6dGVybWluYWxfc2VjcmV0");
        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login("Basic " + serverToken, parameData);
//        Observable<HttpResult<UserBean>> observable = HttpManager.getInstance().getHttpApi().login(CommonUtils.query("1001", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserBean>(listener, context));
    }

    /**
     * 获取考点日程
     */
    public void getScheduleAll() {
        Observable<HttpResult<ScheduleBean>> observable = HttpManager.getInstance().getHttpApi().getScheduleAll("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery(SCHEDULE_BIZ + "", null));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<ScheduleBean>(new OnResultListener<ScheduleBean>() {
            @Override
            public void onSuccess(ScheduleBean result) {
//                Logger.i("getScheduleAll====>" + result.toString());
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
                        //当返回项目没有当前项目，不进行下载
                        if (TextUtils.isEmpty(item.getExamItemCode())) {
                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                            ToastUtils.showShort("日程项目代码不能为空，请联系管理员进行数据更新");
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
                ToastUtils.showShort("获取日程：" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(SCHEDULE_BIZ);
                }
            }
        }));
    }

    /**
     * 获取考点考试所有项目
     */
    public void getItemAll(final Context context) {
        Observable<HttpResult<List<ItemBean>>> observable = HttpManager.getInstance().getHttpApi().getItemAll("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(ITEM_BIZ + "", null));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<ItemBean>>(new OnResultListener<List<ItemBean>>() {
            @Override
            public void onSuccess(List<ItemBean> result) {
//                Logger.i("getItemAll====>" + result.toString());
                if (result == null)
                    return;
                List<Item> itemList = new ArrayList<>();
                for (ItemBean itemBean : result) {
                    //当返回项目没有当前项目，不进行下载
                    if (TextUtils.isEmpty(itemBean.getExamItemCode())) {
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                        ToastUtils.showShort("项目代码不能为空，请联系管理员进行数据更新");
                        if (onRequestEndListener != null) {
                            onRequestEndListener.onFault(ITEM_BIZ);
                        }
                        return;
                    }

                    if (itemBean.getMachineCode() == 0) {
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                        ToastUtils.showShort("项目机器码不能为空，请联系管理员进行数据更新");
                        if (onRequestEndListener != null) {
                            onRequestEndListener.onFault(ITEM_BIZ);
                        }
                        return;
                    }

                    Item item = new Item();
                    item.setCarryMode(itemBean.getCarryMode());
                    item.setDigital(itemBean.getDecimalDigits());
                    item.setItemCode(itemBean.getExamItemCode());
                    item.setItemName(itemBean.getItemName());
                    item.setLastResultMode(itemBean.getLastResultMode());
                    item.setMachineCode(itemBean.getMachineCode());
                    item.setMaxValue(itemBean.getMaxResult());
                    item.setMinValue(itemBean.getMinResult());
                    item.setTestNum(itemBean.getResultTestNum());
                    item.setTestType(itemBean.getTestType());
                    item.setUnit(itemBean.getResultUnit());
                    itemList.add(item);
                }
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
                List<Item> items = DBManager.getInstance().queryItemsByMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                //更新当前项目信息
                if (TestConfigs.sCurrentItem.getItemCode() == null) {
                    if (items.size() == 1) {
                        //当返回项目没有当前项目，不进行下载
                        if (TextUtils.isEmpty(items.get(0).getItemCode())) {
                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                            ToastUtils.showShort("当前考点无此项目，请重新选择项目");
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
                                ToastUtils.showShort("当前考点无此项目，请重新选择项目");
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
                        if (initState == TestConfigs.INIT_SUCCESS) {
                            onRequestEndListener.onSuccess(ITEM_BIZ);
                        } else {
                            onRequestEndListener.onFault(ITEM_BIZ);
                        }
                    }


                } else {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(ITEM_BIZ);
                }

            }

            @Override
            public void onFault(int code, String errorMsg) {
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("获取项目：" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(ITEM_BIZ);
                }
            }
        }));
    }

//    /**
//     * 这里将所有表中为项目代码为default的表内容改为已知的项目代码
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
//        new AlertDialog.Builder(context).setTitle("请设置当前项目")
//                .setCancelable(false)
//                .setItems(itemData, new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //当返回项目没有项目代码，不进行下载
//                        if (TextUtils.isEmpty(items.get(which).getItemCode())) {
//                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
//                            ToastUtils.showShort("当前考点无此项目，请重新选择项目");
//                            if (onRequestEndListener != null) {
//                                onRequestEndListener.onFault(ITEM_BIZ);
//                            }
//                            return;
//                        }
//                        TestConfigs.sCurrentItem = items.get(which);
//                        SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, TestConfigs.getCurrentItemCode());
//                        // 这里将所有表中为项目代码为default的表内容改为已知的项目代码
//                        fillItemCodes();
//
//                    }
//                }).create().show();
//    }

    /**
     * 获取当前项目考生
     *
     * @param itemCode
     * @param batch
     * @param examType
     */
    public void getItemStudent(final String itemCode, int batch, final int examType) {
        Map<String, Object> parameData = new HashMap<>();
        parameData.put("examItemCode", itemCode);
        parameData.put("batch", batch);
        parameData.put("examType", examType);
        Observable<HttpResult<BatchBean<List<StudentBean>>>> observable = HttpManager.getInstance().getHttpApi().getStudent("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(STUDENT_BIZ + "", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<BatchBean<List<StudentBean>>>(new OnResultListener<BatchBean<List<StudentBean>>>() {
            @Override
            public void onSuccess(BatchBean<List<StudentBean>> result) {
                Set<String> supplements = new HashSet<>();// 补考考生考号集合
//                Logger.i("getItemStudent" + result.toString());
                if (result == null || result.getDataInfo() == null) {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(STUDENT_BIZ);
                    return;
                }

                final List<Student> studentList = new ArrayList<>();
                final List<StudentItem> studentItemList = new ArrayList<>();

                for (StudentBean studentBean : result.getDataInfo()) {
                    if (studentBean.getExamType() != 2 && supplements.contains(studentBean.getStudentCode())) {
                        // 已经是补考的数据,有新的相同的数据过来,但是不是补考状态,不处理
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
                    student.setIdCardNo(TextUtils.isEmpty(studentBean.getIdCard()) ? null : studentBean.getIdCard());
                    studentList.add(student);
                    if (ScheduleBean.SITE_EXAMTYPE == 0) {
                        SettingHelper.getSystemSetting().setTestPattern(SystemSetting.PERSON_PATTERN);
                        StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
                                studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
                                studentBean.getExamType(), studentBean.getScheduleNo());
                        studentItemList.add(studentItem);
                    } else {
                        SettingHelper.getSystemSetting().setTestPattern(SystemSetting.GROUP_PATTERN);
                    }
                }
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                DBManager.getInstance().insertStudentList(studentList);
                DBManager.getInstance().insertStuItemList(studentItemList);

                if (result.getBatch() < result.getBatchTotal()) {
                    getItemStudent(itemCode, result.getBatch() + 1, examType);
                } else {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(STUDENT_BIZ);
                }


            }

            @Override
            public void onFault(int code, String errorMsg) {
                Logger.i("getItemStudent  onFault");
                EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_FAULT));
                ToastUtils.showShort("获取考生：" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(STUDENT_BIZ);
                }
            }
        }));
    }

    /**
     * 获取分组信息
     */
    public void getItemGroupAll(final String itemCode, final String scheduleNo, int batch, final int examType) {
        Map<String, Object> parameData = new HashMap<>();
        parameData.put("examItemCode", itemCode);
        parameData.put("scheduleNo", scheduleNo);
        parameData.put("batch", batch);
        parameData.put("examType", examType);
        Observable<HttpResult<BatchBean<List<GroupBean>>>> observable = HttpManager.getInstance().getHttpApi().getGroupAll("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(GROUP_BIZ + "", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<BatchBean<List<GroupBean>>>(new OnResultListener<BatchBean<List<GroupBean>>>() {
            @Override
            public void onSuccess(BatchBean<List<GroupBean>> result) {
//                Logger.i("getItemGroupAll====>" + result.toString());
                if (result == null || result.getDataInfo() == null) {
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(STUDENT_BIZ);
                    return;
                }
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
                                    , groupBean.getScheduleNo(), student.getStudentCode(), student.getTrackNo(), 0);
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
                ToastUtils.showShort("获取分组：" + errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(GROUP_BIZ);
                }
            }
        }));
    }

    /**
     * 获取分组信息
     */
    public void getItemGroupInfo(final String itemCode, String scheduleNo, String sortName, String groupNo, String groupType) {
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
                                    , groupBean.getScheduleNo(), student.getStudentCode(), student.getTrackNo(), 0);
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
     * 上传多个成绩
     *
     * @param uploadResultsList
     */
    public void uploadResult(final List<UploadResults> uploadResultsList) {
        int pageSum = 1;

        if (uploadResultsList.size() > 100) {
            //获取上传分页数目
            if (uploadResultsList.size() % 100 == 0) {
                pageSum = uploadResultsList.size() / 100;
            } else {
                pageSum = uploadResultsList.size() / 100 + 1;
            }
        }

        setUploadResult(0, pageSum, uploadResultsList);
    }

    private void setUploadResult(final int pageNo, final int pageSum, final List<UploadResults> uploadResultsList) {
        final List<UploadResults> uploadData;
        if (pageNo == pageSum - 1) {
            uploadData = uploadResultsList.subList(pageNo * 100, uploadResultsList.size());
        } else {
            uploadData = uploadResultsList.subList(pageNo * 100, (pageNo + 1) * 100);
        }

        Observable<HttpResult<List<UploadResults>>> observable = HttpManager.getInstance().getHttpApi().uploadResult("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(UPLOAD_BIZ + "", uploadData));

        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<UploadResults>>(new OnResultListener<List<UploadResults>>() {
            @Override
            public void onSuccess(List<UploadResults> result) {

                if (result != null && result.size() > 0) {
                    String toastData = "上传失败考生：";
                    for (UploadResults uploadResults : result) {
                        toastData += uploadResults.getStudentCode() + ",";
                    }
                    ToastUtils.showLong(toastData);
                }
                //更新上传状态
                List<RoundResult> roundResultList = new ArrayList<>();
                for (UploadResults uploadResults : uploadResultsList) {
                    if (result != null && result.contains(uploadResults)) {
                        continue;
                    }
                    List<RoundResult> roundResults = RoundResultBean.dbCope(uploadResults.getRoundResultList());
                    for (RoundResult roundResult : roundResults) {
                        roundResult.setUpdateState(1);
                        roundResultList.add(roundResult);
                    }
                }
                DBManager.getInstance().updateRoundResult(roundResultList);

                //是否是最一次上传，关闭加载窗
                if (pageNo == pageSum - 1) {
                    ToastUtils.showShort("上传成功");
                    if (onRequestEndListener != null) {
                        onRequestEndListener.onSuccess(UPLOAD_BIZ);
                    }

                } else {
                    setUploadResult(pageNo + 1, pageSum, uploadResultsList);
                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort("上传失败:" + errorMsg);

                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(UPLOAD_BIZ);
                }
            }
        }));
    }


    public interface OnRequestEndListener {
        void onSuccess(int bizType);

        void onFault(int bizType);
    }
}
