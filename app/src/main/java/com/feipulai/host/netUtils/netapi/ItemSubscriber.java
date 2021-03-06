package com.feipulai.host.netUtils.netapi;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.feipulai.common.utils.ImageUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.SplashScreenActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.bean.BatchBean;
import com.feipulai.host.bean.ItemBean;
import com.feipulai.host.bean.RoundResultBean;
import com.feipulai.host.bean.StudentBean;
import com.feipulai.host.bean.UploadResults;
import com.feipulai.host.bean.UserPhoto;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentFace;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.RequestSub;
import com.feipulai.host.utils.EncryptUtil;
import com.orhanobut.logger.Logger;
import com.ww.fpl.libarcface.common.Constants;
import com.ww.fpl.libarcface.faceserver.FaceServer;
import com.ww.fpl.libarcface.model.FaceRegisterInfo;
import com.ww.fpl.libarcface.util.ConfigUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by pengjf on 2018/10/18.
 * ??????????????????????????????????????????   ????????????:??????
 */

public class ItemSubscriber {

    private int pageNo = 1;
    private int hostId = SettingHelper.getSystemSetting().getHostId();
    public static final int DWON_ITEL_ALL = 2001;
    public static final int DWON_STAUENT_DATA = 2003;
    public static final int UPLOAD_BIZ = 4001;
    private OnRequestEndListener onRequestEndListener;

    public void setOnRequestEndListener(OnRequestEndListener onRequestEndListener) {
        this.onRequestEndListener = onRequestEndListener;
    }

    /**
     * ??????????????????
     *
     * @param token          ????????????????????????token
     * @param testItemResult ??????????????????
     * @param examItemResult ??????????????????
     */
    private void setUploadResultAction(String token, String testItemResult, String examItemResult, String machineNumber, String remark, DisposableObserver subscriber) {

    }

    /**
     * ?????? ???????????????????????????
     *
     * @param
     * @param
     */
    public void getItemAll(final Context context) {
        Observable<HttpResult<List<ItemBean>>> observable = HttpManager.getInstance().getHttpApi().getAllItem(
                "bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery(DWON_ITEL_ALL + "", null));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<ItemBean>>(new OnResultListener<List<ItemBean>>() {
            @Override
            public void onSuccess(List<ItemBean> body) {
                if (body == null || body.size() == 0) {
                    return;
                }
                List<Item> itemList = new ArrayList<>();
                for (ItemBean itemBean : body) {
                    if (TextUtils.isEmpty(itemBean.getExamItemCode())) {
                        ToastUtils.showShort("???????????????????????????????????????????????????????????????");
                        return;
                    }

                    if (TextUtils.isEmpty(itemBean.getMachineCode())) {
                        ToastUtils.showShort("??????????????????????????????????????????????????????????????????");
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
                    item.setfResultType(itemBean.getLastResultMode());
                    try {
                        item.setMachineCode(Integer.valueOf(itemBean.getMachineCode()));
                    } catch (Exception e) {
                        ToastUtils.showShort("???????????????????????????????????????");
                        return;
                    }

                    item.setMaxValue(itemBean.getMaxResult());
                    item.setMinValue(itemBean.getMinResult());
                    item.setTestNum(itemBean.getResultTestNum());
                    item.setItemType(itemBean.getTestType());
                    item.setUnit(itemBean.getResultUnit());
                    itemList.add(item);
                }

                DBManager.getInstance().freshAllItems(itemList);
//                int initState = TestConfigs.init(context,
//                        TestConfigs.sCurrentItem.getMachineCode(),
//                        TestConfigs.sCurrentItem.getItemCode(),
//                        null);
                int initState = TestConfigs.init(context, TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                            ToastUtils.showShort("????????????????????????????????????????????????");
                            if (onRequestEndListener != null) {
                                onRequestEndListener.onFault(DWON_ITEL_ALL);
                            }
                            return;
                        }
                        if (onRequestEndListener != null)
                            onRequestEndListener.onSuccess(DWON_ITEL_ALL);
                    }
                });


                if (initState == TestConfigs.INIT_MULTI_ITEM_CODE) {
                    ToastUtils.showShort("????????????????????????????????????");
                    return;
                }
                if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                    ToastUtils.showShort("?????????????????????????????????");
                    if (onRequestEndListener != null) {
                        onRequestEndListener.onFault(DWON_ITEL_ALL);
                    }
                    return;
                }
                if (onRequestEndListener != null) {
                    onRequestEndListener.onSuccess(DWON_ITEL_ALL);
                }
//                if (roundResults == null) {
//                    getStudentData(lastDownLoadTime, context);
//                    Logger.i("student start download:" + System.currentTimeMillis());
//                } else {
//                    setDataUpLoad(roundResults, context);
//                }
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(DWON_ITEL_ALL);
                }
            }
        }));
    }

    public int size = 0;
    private List<String> stuList = new ArrayList<>();

    // ??????????????????
    public void getStudentData(final int pageNo, final String lastDownLoadTime) {
        getStudentData(pageNo, lastDownLoadTime, new String[]{});
    }

    public void getStudentData(final int pageNo, final String lastDownLoadTime, final String... studentCode) {
        long loadTime = TextUtils.isEmpty(lastDownLoadTime) ? 0 : Long.parseLong(lastDownLoadTime);
        HashMap<String, Object> parameData = new HashMap<>();
        parameData.put("batch", pageNo);
        parameData.put("pageSize", "200");
        parameData.put("upLoadTime", loadTime);
//        parameData.put("itemName", TestConfigs.sCurrentItem.getItemName());
        parameData.put("examItemCode", TestConfigs.sCurrentItem.getItemCode());
        if (studentCode != null && studentCode.length != 0) {
            parameData.put("studentCodeList", studentCode);
        }
        LogUtil.logDebugMessage(parameData.toString());
//        parameData.put("machineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
        Observable<HttpResult<BatchBean<List<StudentBean>>>> observable = HttpManager.getInstance().getHttpApi().getStudentData(
                "bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery(DWON_STAUENT_DATA + "", loadTime + "", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<BatchBean<List<StudentBean>>>(new OnResultListener<BatchBean<List<StudentBean>>>() {
            @Override
            public void onSuccess(BatchBean<List<StudentBean>> result) {
                if (pageNo == 1) {
                    size = 0;
                    stuList.clear();
                }
                if (result == null || result.getDataInfo() == null || result.getDataInfo().size() == 0) {
                    ToastUtils.showShort("???????????????????????????");
                    if (onRequestEndListener != null)
                        onRequestEndListener.onSuccess(DWON_STAUENT_DATA);
                    return;
                }
                size += result.getDataInfo().size();
                Logger.i("??????????????????====???" + size);
                final List<Student> studentList = new ArrayList<>();
                final List<StudentItem> studentItemList = new ArrayList<>();
                List<FaceRegisterInfo> registerInfoList = new ArrayList<>();
                List<StudentFace> studentFaces = new ArrayList<>();
//                //TODO ???????????????????????????????????????OOM??? ???????????????????????????????????????
//                savePortrait(result.getDataInfo());
                for (StudentBean studentBean : result.getDataInfo()) {
                    if (!stuList.contains(studentBean.getIdCard())) {
                        stuList.add(studentBean.getIdCard());
                    } else {
                        Logger.i("????????????====???" + studentBean.toString());
                    }
//                    Logger.i("getItemStudent" + studentBean.toString());
                    Student student = new Student();
                    student.setSex(studentBean.getGender());
                    student.setSchoolName(studentBean.getSchoolName());
                    student.setClassName(studentBean.getClassName());
                    student.setIcCardNo(studentBean.getIdCard());
                    student.setFacultyName(studentBean.getDeptName());
                    student.setGradeName(studentBean.getGradeName());
                    student.setMajorName(studentBean.getSubject());
                    student.setStudentName(studentBean.getStudentName());
                    student.setStudentCode(studentBean.getStudentCode());
                    if (!TextUtils.isEmpty(studentBean.getFaceFeature())) {
                        StudentFace face = new StudentFace();
                        face.setStudentCode(studentBean.getStudentCode());
                        face.setFaceFeature(studentBean.getFaceFeature());
                        studentFaces.add(face);
                    }

//                    student.setPortrait(studentBean.getPhotoData());
//
//                    if (studentBean.getPhotoData() != null) {
//                        ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, studentBean.getStudentCode() + ".jpg", ImageUtil.base64ToBitmap(studentBean.getPhotoData()));
//                    }
//                    student.setFaceFeature(studentBean.getFaceFeature());
                    student.setIdCardNo(TextUtils.isEmpty(studentBean.getIdCard()) ? null : EncryptUtil.setEncryptString(Student.ENCRYPT_KEY, studentBean.getIdCard()));

                    studentList.add(student);
                    StudentItem studentItem = new StudentItem(studentBean.getStudentCode(),
                            studentBean.getExamItemCode(), studentBean.getMachineCode(), studentBean.getStudentType(),
                            studentBean.getExamType(), studentBean.getScheduleNo());
                    studentItemList.add(studentItem);

                    if (!TextUtils.isEmpty(studentBean.getFaceFeature())) {
                        registerInfoList.add(new FaceRegisterInfo(Base64.decode(studentBean.getFaceFeature(), Base64.DEFAULT), student.getStudentCode()));
                    }
                }
                if (SettingHelper.getSystemSetting().getCheckTool() == 4) {
                    FaceServer.getInstance().addFaceList(registerInfoList);
                }
                DBManager.getInstance().insertStudentList(studentList);
                DBManager.getInstance().insertStuItemList(studentItemList);
                DBManager.getInstance().insertStudentFaces(studentFaces);
                if (onRequestEndListener != null) {
                    onRequestEndListener.onRequestData(studentList);
                }
                if (result.getBatch() < result.getBatchTotal()) {
                    getStudentData(result.getBatch() + 1, lastDownLoadTime);
                } else {
                    if (onRequestEndListener != null) {
                        onRequestEndListener.onSuccess(DWON_STAUENT_DATA);
                    }
                    ToastUtils.showShort("??????????????????");
                    if (result.getDataInfo() != null && result.getDataInfo().size() > 0) {
                        SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS,
                                SharedPrefsConfigs.LAST_DOWNLOAD_TIME, result.getDataInfo().get(result.getDataInfo().size() - 1).getDownloadTime());
                    }
                }

            }

            @Override
            public void onFault(int code, String errorMsg) {
                if (onRequestEndListener != null) {
                    onRequestEndListener.onFault(DWON_STAUENT_DATA);
                }
                ToastUtils.showShort(errorMsg);
            }
        }));
    }

    private void savePortrait(final List<StudentBean> studentBeanList) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (StudentBean studentBean : studentBeanList) {
                    if (studentBean.getPhotoData() != null) {
                        ImageUtil.saveBitmapToFile(MyApplication.PATH_IMAGE, studentBean.getStudentCode() + ".jpg", ImageUtil.base64ToBitmap(studentBean.getPhotoData()));
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

//    int index = 0;

    /**
     * ??????????????????
     *
     * @param uploadResultsList
     */
    public void uploadResult(final List<UploadResults> uploadResultsList) {
//        index = 0;
        int pageSum = 1;

        if (uploadResultsList.size() > 100) {
            //????????????????????????
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
        Logger.i("setUploadResult===>" + pageNo);
        Observable<HttpResult<List<UploadResults>>> observable = HttpManager.getInstance().getHttpApi().uploadResult("bearer " + MyApplication.TOKEN,
                CommonUtils.encryptQuery(UPLOAD_BIZ + "", uploadData));

        HttpManager.getInstance().toSubscribe(observable, new RequestSub<List<UploadResults>>(new OnResultListener<List<UploadResults>>() {
            @Override
            public void onSuccess(List<UploadResults> result) {
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

    public void netSb(String photoData, final OnRequestEndListener onRequestEndListener) {
        Map<String, String> params = new HashMap<>();
        params.put("photoData", photoData);
        Observable<HttpResult<UserPhoto>> observable = HttpManager.getInstance().getHttpApi().netSh(
                "bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("8001", params));

//        Observable<HttpResult<UserPhoto>> observable = HttpManager.getInstance().getHttpApi().netSh("bearer " + MyApplication.TOKEN,
//                CommonUtils.encryptQuery( "8001", params));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserPhoto>(new OnResultListener<UserPhoto>() {
            @Override
            public void onSuccess(UserPhoto result) {
                onRequestEndListener.onRequestData(result);
            }

            @Override
            public void onFault(int code, String errorMsg) {
                onRequestEndListener.onFault(8001);
            }
        }));
    }

    public void sendFaceOnline(String studentCode, String base64Face, String base64Feature) {
        HashMap<String, Object> parameData = new HashMap<>();
        parameData.put("photoData", base64Face);
        parameData.put("studentCode", studentCode);
        parameData.put("faceFeature", base64Feature);
        Observable<HttpResult<UserPhoto>> observable = HttpManager.getInstance().getHttpApi().netSh(
                "bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("8001", parameData));
        HttpManager.getInstance().toSubscribe(observable, new RequestSub<UserPhoto>(new OnResultListener<UserPhoto>() {

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


//    // ????????????
//    public void setDataUpLoad(List<RoundResult> roundResults) {
//        setDataUpLoad(roundResults, null);
//    }
//
//    // ????????????
//    public void setDataUpLoad(List<RoundResult> roundResults, Context context) {
//        if (roundResults == null || roundResults.size() == 0)
//            return;
//        //????????????
//        List<HashMap<String, Object>> testItemResult = new ArrayList<>();
//        //????????????????????????
//        List<String> addStudentList = new ArrayList<>();
//        int pageSum = 1;
//        for (RoundResult roundResult : roundResults) {
//
//            //????????????????????????
//            if (!addStudentList.contains(roundResult.getStudentCode())) {
//                addStudentList.add(roundResult.getStudentCode());
//
//                HashMap<String, Object> itemResult = new HashMap<>();
//                itemResult.put("studentCode", roundResult.getStudentCode());
//                itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
//                itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
//                RoundResult lastResult;
//                //??????????????????
//                if (TestConfigs.sCurrentItem.getfResultType() == 0) {
//                    //??????
//                    lastResult = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(roundResult.getStudentCode());
//                } else {
//                    //??????
//                    lastResult = DBManager.getInstance().queryLastScoreByStuCode(roundResult.getStudentCode());
//                }
//                itemResult.put("result", lastResult.getResult());
//                itemResult.put("testTime", lastResult.getTestTime());
//                itemResult.put("resultState", "0");
//
//                //???????????????????????????????????????
//                List<RoundResult> studentRound = DBManager.getInstance().queryUploadStudentResults(roundResult.getStudentCode(), false);
//                itemResult.put("roundResult", studentRound);
//
//                testItemResult.add(itemResult);
//            }
//        }
//
//        //????????????????????????100?????????100????????????
//        if (testItemResult.size() > 100) {
//            //????????????????????????
//            if (testItemResult.size() % 100 == 0) {
//                pageSum = testItemResult.size() / 100;
//            } else {
//                pageSum = testItemResult.size() / 100 + 1;
//            }
//        }
//        setUploadResult(0, pageSum, testItemResult, context);
//    }
//
//    public void setStudentDataUpLoad(final Context context, final List<String> studentList) {
//
//        getAll(MyApplication.TOKEN, new RequestSub(new OnResultListener<List<Item>>() {
//            public void onSuccess(List<Item> body) {
//                if (body == null || body.size() == 0) {
//                    return;
//                }
//                DBManager.getInstance().freshAllItems(body);
//                int initState = TestConfigs.init(context,
//                        TestConfigs.sCurrentItem.getMachineCode(),
//                        TestConfigs.sCurrentItem.getItemCode(),
//                        null);
//
//                if (initState == TestConfigs.INIT_MULTI_ITEM_CODE) {
//                    ToastUtils.showShort("????????????????????????????????????");
//                    return;
//                }
//                List<HashMap<String, Object>> testItemResult = getUploadData(studentList);
//                int pageSum = 1;
//                //????????????????????????100?????????100????????????
//                if (testItemResult.size() > 100) {
//                    //????????????????????????
//                    if (testItemResult.size() % 100 == 0) {
//                        pageSum = testItemResult.size() / 100;
//                    } else {
//                        pageSum = testItemResult.size() / 100 + 1;
//                    }
//                }
//                setUploadResult(0, pageSum, testItemResult, context);
//            }
//
//            @Override
//            public void onFault(String errorMsg) {
//                ToastUtils.showShort(errorMsg);
//            }
//        }, context));
//
//
//    }
//
//    private List<HashMap<String, Object>> getUploadData(List<String> studentList) {
//        //????????????
//        List<HashMap<String, Object>> testItemResult = new ArrayList<>();
//
//        for (String stuCode : studentList) {
//            HashMap<String, Object> itemResult = new HashMap<>();
//            itemResult.put("studentCode", stuCode);
//            itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
//            itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
//            RoundResult lastResult;
//            //??????????????????
//            if (TestConfigs.sCurrentItem.getfResultType() == 0) {
//                //??????
//                lastResult = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(stuCode);
//            } else {
//                //??????
//                lastResult = DBManager.getInstance().queryLastScoreByStuCode(stuCode);
//            }
//            itemResult.put("result", lastResult.getResult());
//            itemResult.put("testTime", lastResult.getTestTime());
//            itemResult.put("resultState", "0");
//
//            //???????????????????????????????????????
//            List<RoundResult> studentRound = DBManager.getInstance().queryResultsByStudentCode(stuCode);
//            itemResult.put("roundResult", studentRound);
//            testItemResult.add(itemResult);
//        }
//        return testItemResult;
//
//    }
//
//    /**
//     * ??????????????????
//     *
//     * @param roundResult ????????????,???????????????????????????????????????
//     * @param lastResult  ????????????,??????{@link Item#fResultType}??????,?????????????????????????????????(?????????????????????)??????????????????
//     */
//    public void setDataUpLoad(RoundResult roundResult, RoundResult lastResult) {
//        //????????????
//        List<HashMap<String, Object>> testItemResult = new ArrayList<>();
//        //????????????????????????
//        List<String> addStudentList = new ArrayList<>();
//
//        addStudentList.add(roundResult.getStudentCode());
//
//        HashMap<String, Object> itemResult = new HashMap<>();
//
//        itemResult.put("studentCode", roundResult.getStudentCode());
//        itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
//        itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
//        //??????????????????
//        itemResult.put("result", lastResult.getResult());
//        itemResult.put("testTime", lastResult.getTestTime());
//        itemResult.put("resultState", "0");
//
//        List<RoundResult> studentRound = DBManager.getInstance().queryUploadStudentResults(roundResult.getStudentCode(),
//                false);
//        itemResult.put("roundResult", studentRound);
//
//        testItemResult.add(itemResult);
//        setUploadResult(0, 1, testItemResult, null);
//    }
//
//    /**
//     * ????????????
//     */
//    private void setUploadResult(final int pageNo, final int pageSum,
//                                 final List<HashMap<String, Object>> testItemResult,
//                                 final Context context) {
//        final List<HashMap<String, Object>> uploadData;
//        if (pageNo == pageSum - 1) {
//            uploadData = testItemResult.subList(pageNo * 100, testItemResult.size());
//        } else {
//            uploadData = testItemResult.subList(pageNo * 100, (pageNo + 1) * 100);
//        }
//
//        HashMap<String, String> parameData = new HashMap<>();
//        parameData.put("Token", token);
//        parameData.put("testItemResult", testItemResult);
//        parameData.put("examItemResult", examItemResult);
//        parameData.put("machineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
//        parameData.put("MachineNumber", machineNumber);
//        parameData.put("remark", remark);
//        Observable<HttpResult<String>> observable = HttpManager.getInstance().getHttpApi().uploadResult(CommonUtils.encryptQuery(parameData));
//        HttpManager.getInstance().toSubscribe(observable, subscriber);
//
//
//        setUploadResultAction(MyApplication.TOKEN, data,
//                null, hostId + "", null, new RequestSub(new OnResultListener() {
//                    @Override
//                    public void onSuccess(Object result) {
//                        // ??????????????????
//                        for (HashMap<String, Object> resultMap : uploadData) {
//                            List<RoundResult> studentRound = (List<RoundResult>) resultMap.get("roundResult");
//                            for (RoundResult roundResult : studentRound) {
//                                roundResult.setUpdateState(1);
//                            }
//                            DBManager.getInstance().updateRoundResult(studentRound);
//                        }
//                        //??????????????????????????????????????????
//                        if (pageNo == pageSum - 1) {
//                            Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
//                            MyApplication.getInstance().sendBroadcast(intent);
//                            ToastUtils.showLong("????????????");
//                        } else {
//                            setUploadResult(pageNo + 1, pageSum, testItemResult, context);
//                        }
//                    }
//
//                    @Override
//                    public void onFault(String errorMsg) {
//                        ToastUtils.showShort(errorMsg);
//                    }
//                }, context));
//    }

}
