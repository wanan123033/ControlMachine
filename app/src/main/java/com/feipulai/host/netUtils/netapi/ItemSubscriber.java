package com.feipulai.host.netUtils.netapi;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.host.MyApplication;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.netUtils.CommonUtils;
import com.feipulai.host.netUtils.HttpManager;
import com.feipulai.host.netUtils.HttpResult;
import com.feipulai.host.netUtils.OnResultListener;
import com.feipulai.host.netUtils.RequestSub;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by pengjf on 2018/10/18.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ItemSubscriber {

    private int pageNo = 1;
    private int hostId = SettingHelper.getSystemSetting().getHostId();

    /**
     * 获取全部项目信息
     *
     * @param token 设备绑定是返回的token
     */
    private void getAll(String token, DisposableObserver subscriber) {
        Observable<HttpResult<List<Item>>> observable = HttpManager.getInstance().getHttpApi().getAll(CommonUtils.query(token));
        HttpManager.getInstance().toSubscribe(observable, subscriber);
    }

    /**
     * 获取学生信息
     *
     * @param token      设备绑定是返回的token
     * @param examStatus 考试状态 0 正常 1缓考 2补考
     */
    private void getStudentAllAction(String token, String examStatus, String uploadTime, String pageNo, String pageSize, DisposableObserver subscriber) {
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("Token", token);
        parameData.put("pageNo", pageNo);
        parameData.put("pageSize", pageSize);
        parameData.put("upLoadTime", uploadTime);
        parameData.put("itemName", TestConfigs.sCurrentItem.getItemName());
        parameData.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
        parameData.put("examStatus", examStatus);
        parameData.put("machineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
        Observable<HttpResult<List<Student>>> observable = HttpManager.getInstance().getHttpApi().getStudent(CommonUtils.query(parameData));
        HttpManager.getInstance().toSubscribe(observable, subscriber);
    }

    /**
     * 成绩上传接口
     *
     * @param token          设备绑定是返回的token
     * @param testItemResult 体测项目成绩
     * @param examItemResult 考试项目成绩
     */
    private void setUploadResultAction(String token, String testItemResult, String examItemResult, String machineNumber, String remark, DisposableObserver subscriber) {
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("Token", token);
        parameData.put("testItemResult", testItemResult);
        parameData.put("examItemResult", examItemResult);
        parameData.put("machineCode", TestConfigs.sCurrentItem.getMachineCode() + "");
        parameData.put("MachineNumber", machineNumber);
        parameData.put("remark", remark);
        Observable<HttpResult<String>> observable = HttpManager.getInstance().getHttpApi().uploadResult(CommonUtils.query(parameData));
        HttpManager.getInstance().toSubscribe(observable, subscriber);
    }

    /**
     * 获取 项目所有的项目信息
     *
     * @param lastDownLoadTime
     * @param roundResults
     */
    public void getItemAll(final String token, final Context context,
                           final String lastDownLoadTime, final List<RoundResult> roundResults) {
        pageNo = 1;
        getAll(token, new RequestSub(new OnResultListener<List<Item>>() {
            public void onSuccess(List<Item> body) {
                if (body == null || body.size() == 0) {
                    return;
                }
                for (Item item : body) {
                    if (TextUtils.isEmpty(item.getItemCode())) {
                        ToastUtils.showShort("项目代码不能为空，请联系管理员进行数据更新");
                        return;
                    }

                    if (item.getMachineCode() == 0) {
                        ToastUtils.showShort("项目机器码不能为空，请联系管理员进行数据更新");
                        return;
                    }
                }

                DBManager.getInstance().freshAllItems(body);
                int initState = TestConfigs.init(context,
                        TestConfigs.sCurrentItem.getMachineCode(),
                        TestConfigs.sCurrentItem.getItemCode(),
                        null);

                if (initState == TestConfigs.INIT_MULTI_ITEM_CODE) {
                    ToastUtils.showShort("请处理项目信息后重新操作");
                    return;
                }

                if (roundResults == null) {
                    getStudentData(lastDownLoadTime, context);
                    Logger.i("student start download:" + System.currentTimeMillis());
                } else {
                    setDataUpLoad(roundResults, context);
                }
            }

            @Override
            public void onFault(String errorMsg) {
                ToastUtils.showShort(errorMsg);
            }
        }, context));
    }


    // 获取学生信息
    private void getStudentData(final String lastDownLoadTime, final Context context) {
        getStudentAllAction(MyApplication.TOKEN, "", lastDownLoadTime, pageNo + "", "3000",
                new RequestSub(new OnResultListener<List<Student>>() {

                    @Override
                    public void onSuccess(List<Student> studentList) {
                        if (studentList.size() > 0) {
                            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS,
                                    SharedPrefsConfigs.LAST_DOWNLOAD_TIME, studentList.get(studentList.size() - 1).getDownloadTime());
                            pageNo++;
                            for (Student student : studentList) {

                                student.setIdCardNo(TextUtils.isEmpty(student.getIdCardNo()) ? null : student.getIdCardNo());
                            }
                            DBManager.getInstance().insertStudentList(studentList);

                            ArrayList<StudentItem> studentItems = new ArrayList<>();
                            for (Student student : studentList) {
                                for (StudentItem studentItem : student.getStudentItemList()) {
                                    studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                                    studentItem.setStudentCode(student.getStudentCode());
                                }
                                studentItems.addAll(student.getStudentItemList());
                            }
                            DBManager.getInstance().insertStuItemList(studentItems);
                            getStudentData(lastDownLoadTime, context);
                        }
                    }

                    @Override
                    public void onFault(String errorMsg) {
                        if (errorMsg.equals("成功获取该项目学生信息")) {
                            Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
                            context.sendBroadcast(intent);
                        }
                        ToastUtils.showShort(errorMsg);
                    }
                }, context));
    }

    // 数据上传
    public void setDataUpLoad(List<RoundResult> roundResults) {
        setDataUpLoad(roundResults, null);
    }

    // 数据上传
    public void setDataUpLoad(List<RoundResult> roundResults, Context context) {
        if (roundResults == null || roundResults.size() == 0)
            return;
        //上传数据
        List<HashMap<String, Object>> testItemResult = new ArrayList<>();
        //保存已添加的学生
        List<String> addStudentList = new ArrayList<>();
        int pageSum = 1;
        for (RoundResult roundResult : roundResults) {

            //该学生是否已添加
            if (!addStudentList.contains(roundResult.getStudentCode())) {
                addStudentList.add(roundResult.getStudentCode());

                HashMap<String, Object> itemResult = new HashMap<>();
                itemResult.put("studentCode", roundResult.getStudentCode());
                itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
                itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
                RoundResult lastResult;
                //判断成绩类型
                if (TestConfigs.sCurrentItem.getfResultType() == 0) {
                    //最好
                    lastResult = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(roundResult.getStudentCode());
                } else {
                    //最后
                    lastResult = DBManager.getInstance().queryLastScoreByStuCode(roundResult.getStudentCode());
                }
                itemResult.put("result", lastResult.getResult());
                itemResult.put("testTime", lastResult.getTestTime());
                itemResult.put("resultState", "0");

                //获取该学生未上传的成绩轮次
                List<RoundResult> studentRound = DBManager.getInstance().queryUploadStudentResults(roundResult.getStudentCode(), false);
                itemResult.put("roundResult", studentRound);

                testItemResult.add(itemResult);
            }
        }

        //上传数量是否大于100，大于100分页上传
        if (testItemResult.size() > 100) {
            //获取上传分页数目
            if (testItemResult.size() % 100 == 0) {
                pageSum = testItemResult.size() / 100;
            } else {
                pageSum = testItemResult.size() / 100 + 1;
            }
        }
        setUploadResult(0, pageSum, testItemResult, context);
    }

    public void setStudentDataUpLoad(final Context context, final List<String> studentList) {

        getAll(MyApplication.TOKEN, new RequestSub(new OnResultListener<List<Item>>() {
            public void onSuccess(List<Item> body) {
                if (body == null || body.size() == 0) {
                    return;
                }
                DBManager.getInstance().freshAllItems(body);
                int initState = TestConfigs.init(context,
                        TestConfigs.sCurrentItem.getMachineCode(),
                        TestConfigs.sCurrentItem.getItemCode(),
                        null);

                if (initState == TestConfigs.INIT_MULTI_ITEM_CODE) {
                    ToastUtils.showShort("请处理项目信息后重新操作");
                    return;
                }
                List<HashMap<String, Object>> testItemResult = getUploadData(studentList);
                int pageSum = 1;
                //上传数量是否大于100，大于100分页上传
                if (testItemResult.size() > 100) {
                    //获取上传分页数目
                    if (testItemResult.size() % 100 == 0) {
                        pageSum = testItemResult.size() / 100;
                    } else {
                        pageSum = testItemResult.size() / 100 + 1;
                    }
                }
                setUploadResult(0, pageSum, testItemResult, context);
            }

            @Override
            public void onFault(String errorMsg) {
                ToastUtils.showShort(errorMsg);
            }
        }, context));


    }

    private List<HashMap<String, Object>> getUploadData(List<String> studentList) {
        //上传数据
        List<HashMap<String, Object>> testItemResult = new ArrayList<>();

        for (String stuCode : studentList) {
            HashMap<String, Object> itemResult = new HashMap<>();
            itemResult.put("studentCode", stuCode);
            itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
            itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
            RoundResult lastResult;
            //判断成绩类型
            if (TestConfigs.sCurrentItem.getfResultType() == 0) {
                //最好
                lastResult = DBManager.getInstance().queryResultsByStudentCodeIsLastResult(stuCode);
            } else {
                //最后
                lastResult = DBManager.getInstance().queryLastScoreByStuCode(stuCode);
            }
            itemResult.put("result", lastResult.getResult());
            itemResult.put("testTime", lastResult.getTestTime());
            itemResult.put("resultState","0");

            //获取该学生未上传的成绩轮次
            List<RoundResult> studentRound = DBManager.getInstance().queryResultsByStudentCode(stuCode);
            itemResult.put("roundResult", studentRound);
            testItemResult.add(itemResult);
        }
        return testItemResult;

    }

    /**
     * 数据实时上传
     *
     * @param roundResult 当前成绩,需实时上传的当前次测试成绩
     * @param lastResult  最终成绩,依据{@link Item#fResultType}判断,选出最好成绩或最后成绩(当前次测试成绩)作为最终成绩
     */
    public void setDataUpLoad(RoundResult roundResult, RoundResult lastResult) {
        //上传数据
        List<HashMap<String, Object>> testItemResult = new ArrayList<>();
        //保存已添加的学生
        List<String> addStudentList = new ArrayList<>();

        addStudentList.add(roundResult.getStudentCode());

        HashMap<String, Object> itemResult = new HashMap<>();

        itemResult.put("studentCode", roundResult.getStudentCode());
        itemResult.put("itemCode", TestConfigs.sCurrentItem.getItemCode());
        itemResult.put("itemName", TestConfigs.sCurrentItem.getItemName());
        //最后成绩信息
        itemResult.put("result", lastResult.getResult());
        itemResult.put("testTime", lastResult.getTestTime());
        itemResult.put("resultState", "0");

        List<RoundResult> studentRound = DBManager.getInstance().queryUploadStudentResults(roundResult.getStudentCode(),
                false);
        itemResult.put("roundResult", studentRound);

        testItemResult.add(itemResult);
        setUploadResult(0, 1, testItemResult, null);
    }

    /**
     * 上传成绩
     */
    private void setUploadResult(final int pageNo, final int pageSum,
                                 final List<HashMap<String, Object>> testItemResult,
                                 final Context context) {
        final List<HashMap<String, Object>> uploadData;
        if (pageNo == pageSum - 1) {
            uploadData = testItemResult.subList(pageNo * 100, testItemResult.size());
        } else {
            uploadData = testItemResult.subList(pageNo * 100, (pageNo + 1) * 100);
        }
        //转Json
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String data = gson.toJson(uploadData);
        // Log.i("james",data);

        setUploadResultAction(MyApplication.TOKEN, data,
                null, hostId + "", null, new RequestSub(new OnResultListener() {
                    @Override
                    public void onSuccess(Object result) {
                        // 更新本地数据
                        for (HashMap<String, Object> resultMap : uploadData) {
                            List<RoundResult> studentRound = (List<RoundResult>) resultMap.get("roundResult");
                            for (RoundResult roundResult : studentRound) {
                                roundResult.setUpdateState(1);
                            }
                            DBManager.getInstance().updateRoundResult(studentRound);
                        }
                        //是否是最一次上传，关闭加载窗
                        if (pageNo == pageSum - 1) {
                            Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
                            MyApplication.getInstance().sendBroadcast(intent);
                            ToastUtils.showLong("上传成功");
                        } else {
                            setUploadResult(pageNo + 1, pageSum, testItemResult, context);
                        }
                    }

                    @Override
                    public void onFault(String errorMsg) {
                        ToastUtils.showShort(errorMsg);
                    }
                }, context));
    }

}
