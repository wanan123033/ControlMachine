package com.feipulai.host.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.beans.HeightWeightResult;
import com.feipulai.host.activity.height_weight.HWConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;

/**
 * Created by James on 2019/2/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultUtils {

    public static RoundResult generateRoughResultWithRaw(Student student, Object machineResult) {
        return generateRoughResultWithRaw(student, machineResult, 0);
    }

    /**
     * 依据机器成绩,生成一个简要的轮次成绩,该成绩仅为一个模板,不做最好成绩判断等
     *
     * @param student       考生信息
     * @param machineResult 机器成绩
     * @param arg           目前为身高体重专用,如果希望返回 身高成绩,传入 1 ,体重成绩 则传入2
     * @return
     */
    public static RoundResult generateRoughResultWithRaw(Student student, Object machineResult, int arg) {
        String testTime = DateUtil.getCurrentTime() + "";
        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        RoundResult result = new RoundResult();
        result.setMachineCode(machineCode);
        result.setStudentCode(student.getStudentCode());
        result.setItemCode(TestConfigs.getCurrentItemCode());
        result.setTestTime(testTime);
        result.setResultState(RoundResult.RESULT_STATE_NORMAL);
        result.setRoundNo(1);

        switch (machineCode) {
            case ItemDefault.CODE_HW:
                HeightWeightResult heightWeightResult = (HeightWeightResult) machineResult;
                if (arg == 1) {
                    result.setItemCode(TestConfigs.HEIGHT_ITEM_CODE);
                    result.setResult((int) (heightWeightResult.getHeight() * 10));// mm
                } else if (arg == 2) {
                    result.setItemCode(TestConfigs.WEIGHT_ITEM_CODE);
                    result.setResult((int) (heightWeightResult.getWeight() * 1000));// g
                } else {
                    throw new IllegalArgumentException("height weight item must have arg(1 or 2)");
                }
                break;

            default:
                throw new IllegalArgumentException("wrong machine code");
        }

        return result;
    }

    /**
     * 保存轮次成绩到数据库
     *
     * @param roundResult       当前轮次成绩,也即需要保存的成绩
     * @param currentBestResult 当前已知的最好成绩,用于与当前轮次成绩比较,确定最好成绩
     */
    public static void saveResults(Context context, @NonNull RoundResult roundResult, RoundResult currentBestResult) {
        checkResultRange(roundResult);
        int result = roundResult.getResult();
        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        // 身高体重特殊处理
        if (machineCode == ItemDefault.CODE_HW) {
            // 体质指数（BMI）= 体重（kg）÷ 身高²（m） = 体重（g）÷ 身高²（mm） * 0.001
            // 已与产品同事---刘俊潜 沟通,直接取最后一次成绩作为做好成绩就行,不用计算BMI等
            switchIsLastResult(roundResult, currentBestResult);
        } else if (currentBestResult == null  // 目前没有最好成绩,当前成绩就是最好成绩
                || currentBestResult.getResultState() == RoundResult.RESULT_STATE_FOUL // 原有的最好成绩犯规
                || currentBestResult.getResult() <= result// 原有最好成绩比当前成绩差
                ) {
            switchIsLastResult(roundResult, currentBestResult);
        }
        DBManager.getInstance().insertRoundResult(roundResult);
    }

    private static void switchIsLastResult(RoundResult roundResult, RoundResult currentBestResult) {
        roundResult.setIsLastResult(1);
        if (currentBestResult != null) {
            currentBestResult.setIsLastResult(0);
            DBManager.getInstance().updateRoundResult(currentBestResult);
        }
    }

    private static void checkResultRange(RoundResult roundResult) {
        int result = roundResult.getResult();
        String itemCode = roundResult.getItemCode();
        Item item = TestConfigs.sCurrentItem;
        if (TestConfigs.HEIGHT_ITEM_CODE.equals(itemCode)) {
            item = HWConfigs.HEIGHT_ITEM;
        } else if (TestConfigs.WEIGHT_ITEM_CODE.equals(itemCode)) {
            item = HWConfigs.WEIGHT_ITEM;
        }
        int maxValue = item.getMaxValue();
        int minValue = item.getMinValue();
        if (!(maxValue == 0 && item.getMinValue() == 0)) {
            roundResult.setResult(result > maxValue ? maxValue : (result < minValue ? minValue : result));
        }
    }

}
