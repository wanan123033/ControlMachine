package com.feipulai.host.netUtils;

import com.feipulai.host.bean.RoundResultBean;
import com.feipulai.host.bean.UploadResults;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理成绩上传格式转换
 * Created by zzs on  2019/12/30
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UploadResultUtil {

    public static List<UploadResults> getUploadData(RoundResult roundResult, RoundResult lastResult) {
        List<UploadResults> uploadResults = new ArrayList<>();
        uploadResults.add(new UploadResults(
                TestConfigs.getCurrentItemCode(), roundResult.getStudentCode()
                , "1", lastResult,
                RoundResultBean.beanCope2(roundResult)));
        Logger.i("自动上传成绩:" + uploadResults.toString());
        return uploadResults;

    }

    public static List<UploadResults> getUploadData(List<RoundResult> resultList) {
        List<UploadResults> uploadResults = new ArrayList<>();
        //保存已添加的学生
        List<String> addStudentList = new ArrayList<>();

        for (RoundResult roundResult : resultList) {
            //该学生是否已添加
            if (!addStudentList.contains(roundResult.getStudentCode())) {
                addStudentList.add(roundResult.getStudentCode());

                uploadResults.add(new UploadResults(
                        TestConfigs.getCurrentItemCode(), roundResult.getStudentCode()
                        , "1",
                        RoundResultBean.beanCope(DBManager.getInstance().queryResultsByStudentCode(roundResult.getStudentCode()))));

            }
        }

        return uploadResults;
    }

    public static List<UploadResults> getUploadDataByStuCode(List<String> stuCodeList) {
        List<UploadResults> uploadResults = new ArrayList<>();
        //保存已添加的学生
        List<String> addStudentList = new ArrayList<>();

        for (String stuCode : stuCodeList) {
            //该学生是否已添加
            if (!addStudentList.contains(stuCode)) {
                addStudentList.add(stuCode);

                uploadResults.add(new UploadResults(
                        TestConfigs.getCurrentItemCode(), stuCode
                        , "1",
                        RoundResultBean.beanCope(DBManager.getInstance().queryResultsByStudentCode(stuCode))));

            }
        }

        return uploadResults;
    }
}
