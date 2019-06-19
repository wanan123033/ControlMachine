package com.feipulai.host.activity.vccheck;

import android.app.Activity;
import android.os.Bundle;

import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.netUtils.netapi.ItemSubscriber;

import java.util.Calendar;

public class UploadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        upload();
    }

    private void upload() {
        BaseStuPair baseStuPair = new BaseStuPair();
        baseStuPair.setResult(128);
        baseStuPair.setResultState(0);
        Student student = new Student();
        student.setStudentCode("123456");
        baseStuPair.setStudent(student);

        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
        roundResult.setItemCode(itemCode);
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setTestTime(TestConfigs.df.format(Calendar.getInstance().getTime()));
        roundResult.setRoundNo(1);

        RoundResult bestResult = DBManager.getInstance().queryBestScore(baseStuPair.getStudent().getStudentCode());
        if (bestResult != null) {
            // 原有最好成绩犯规 或者原有最好成绩没有犯规但是现在成绩更好
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // 这个时候就要同时修改这两个成绩了
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            } else {
                roundResult.setIsLastResult(0);
            }
        } else {
            // 第一次测试
            roundResult.setIsLastResult(1);
        }

        ItemSubscriber itemSubscriber = new ItemSubscriber();
        itemSubscriber.setDataUpLoad(roundResult,roundResult);
    }
}
