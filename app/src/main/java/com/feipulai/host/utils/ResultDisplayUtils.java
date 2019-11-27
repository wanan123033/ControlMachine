package com.feipulai.host.utils;

import com.feipulai.common.utils.ResultDisplayTools;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.Item;


/**
 * Created by James on 2018/4/25 0025.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultDisplayUtils {

    //private static final int BQS = 0x0;
    //private static final int SSWR = 0x01;
    //private static final int FLSQ = 0x02;
    //private static final int FLJW = 0x03;

    /**
     * 当前测试项目成绩转换为显示格式
     *
     * @param dbResult 数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
     * @return 可以用于显示的成绩字符串
     */
    public static String getStrResultForDisplay(int dbResult) {
        Item item = TestConfigs.sCurrentItem;
        return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(), dbResult,
                item.getDigital(),
                item.getCarryMode(),
                item.getUnit(),
                0,
                true);
    }

    /**
     * 当前测试项目成绩转换为显示格式
     *
     * @param dbResult     数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不需要在这里转换)
     * @param isReturnUnit 是否带单位返回
     * @return 可以用于显示的成绩字符串
     */
    public static String getStrResultForDisplay(int dbResult, boolean isReturnUnit) {
        Item item = TestConfigs.sCurrentItem;
        return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(), dbResult,
                item.getDigital(),
                item.getCarryMode(),
                item.getUnit(),
                0,
                isReturnUnit);
    }

    /**
     * 当前测试项目成绩转换为显示格式
     *
     * @param dbResult 数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不许要在这里转换)
     * @param item     指定项目进行格式转换
     * @return 可以用于显示的成绩字符串, 如果item.unit为空, 或者未找到对应的单位, 返回null
     */
    public static String getStrResultForDisplay(int dbResult, Item item) {
        return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(), dbResult,
                item.getDigital(),
                item.getCarryMode(),
                item.getUnit(),
                generateArg(item),
                true);
    }

    /**
     * 当前测试项目成绩转换为显示格式
     *
     * @param dbResult 数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不许要在这里转换)
     * @param item     指定项目进行格式转换
     * @return 可以用于显示的成绩字符串, 如果item.unit为空, 或者未找到对应的单位, 返回null
     */
    public static String getStrResultForDisplay(int dbResult, Item item, boolean isReturnUnit) {
        return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(), dbResult,
                item.getDigital(),
                item.getCarryMode(),
                item.getUnit(),
                generateArg(item),
                isReturnUnit);
    }


    private static int generateArg(Item item) {
        if (TestConfigs.HEIGHT_ITEM_CODE.equals(item.getItemCode())) {
            return 1;
        } else if (TestConfigs.WEIGHT_ITEM_CODE.equals(item.getItemCode())) {
            return 2;
        }
        return 0;
    }

    /**
     * 获得有效的成绩单位,首先检查数据库的单位,如果数据库的没有 或 不合格,则使用默认的单位
     *
     * @param machineCode 机器码
     * @param unit        数据库中的单位
     * @return 有效的成绩单位, 获取成绩显示时会使用该单位
     */
    public static String getQualifiedUnit(int machineCode, String unit) {
        return ResultDisplayTools.getQualifiedUnit(machineCode, unit, 0);
    }

    /**
     * 获得有效的成绩单位,首先检查数据库的单位,如果数据库的没有 或 不合格,则使用默认的单位
     *
     * @param machineCode 机器码
     * @param unit        数据库中的单位
     * @param arg         因为身高体重有两个成绩,也就有两个单位,如果为身高 传入 1,为体重 则传入 2 , 默认传入 0
     * @return 有效的成绩单位, 获取成绩显示时会使用该单位
     */
    public static String getQualifiedUnit(int machineCode, String unit, int arg) {
        return ResultDisplayTools.getQualifiedUnit(machineCode, unit, arg);
    }

    public static String getQualifiedUnit(Item item) {
        if (TestConfigs.HEIGHT_ITEM_CODE.equals(item.getItemCode())) {
            return ResultDisplayTools.getQualifiedUnit(item.getMachineCode(), item.getUnit(), 1);
        } else if (TestConfigs.WEIGHT_ITEM_CODE.equals(item.getItemCode())) {
            return ResultDisplayTools.getQualifiedUnit(item.getMachineCode(), item.getUnit(), 2);
        }
        return ResultDisplayTools.getQualifiedUnit(item.getMachineCode(), item.getUnit(), 1);
    }

    /**
     * 获取字符长度含中文标点数字等
     * @param value
     * @return
     */
    public static int getStringLength(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }
}
