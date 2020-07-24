package com.feipulai.host.db;

import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.StudentItem;

import java.util.List;

/**
 * Created by James on 2018/10/31 0031.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MachineItemCodeUtil {


    /**
     * 用指定的项目代码替换指定学生信息和成绩信息中的项目代码
     * <p>
     * 应在原项目代码为null,获取到该项目信息时调用
     *
     * @param studentItems 需要更改itemCode的学生报名信息
     * @param roundResults 需要更改itemCode的成绩信息
     * @param itemCode     指定的itemCode
     */
    public static void fillDefaultItemCode(List<StudentItem> studentItems, List<RoundResult> roundResults, String itemCode) {
        if (itemCode == null)
            return;
        if (studentItems != null) {
            for (StudentItem studentItem : studentItems) {
                studentItem.setItemCode(itemCode);
            }
            // 将原来项目代码为default的学生报名信息更新为选择的项目代码
            DBManager.getInstance().updateStudentItem(studentItems);
        }
        if (roundResults != null) {
            // 将原来项目代码为default的学生成绩信息更新为选择的项目代码
            for (RoundResult result : roundResults) {
                result.setItemCode(itemCode);
            }
            DBManager.getInstance().updateRoundResult(roundResults);
        }
    }

}
