package com.feipulai.common.utils;

import com.feipulai.device.ic.utils.ItemDefault;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by James on 2018/4/25 0025.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultDisplayTools {

    private static final int BQS = 0x0;
    private static final int SSWR = 0x01;
    private static final int FLSQ = 0x02;
    private static final int FLJW = 0x03;

    /**
     * 获得有效的成绩单位,首先检查数据库的单位,如果数据库的没有 或 不合格,则使用默认的单位
     *
     * @param machineCode 机器码
     * @param unit        数据库中的单位
     * @param arg         因为身高体重有两个成绩,也就有两个单位,如果为身高 传入 1,为体重 则传入 2 , 默认传入 0
     * @return 有效的成绩单位, 获取成绩显示时会使用该单位
     */
    public static String getQualifiedUnit(int machineCode, String unit, int arg) {
        String result = null;
        switch (machineCode) {

            case ItemDefault.CODE_TS:
            case ItemDefault.CODE_YWQZ:
            case ItemDefault.CODE_YTXS:
            case ItemDefault.CODE_PQ:
            case ItemDefault.CODE_FWC:
            case ItemDefault.CODE_SHOOT:
                if ("次".equals(unit) || "个".equals(unit)) {
                    result = unit;
                } else
                    result = "次";
                break;

            case ItemDefault.CODE_LDTY:
            case ItemDefault.CODE_HWSXQ:
            case ItemDefault.CODE_ZWTQQ:
            case ItemDefault.CODE_MG:
                if ("厘米".equals(unit) || "米".equals(unit) || "毫米".equals(unit)) {
                    result = unit;
                } else {
                    result = "厘米";
                }
                break;
            case ItemDefault.CODE_LQYQ:
            case ItemDefault.CODE_ZQYQ:
            case ItemDefault.CODE_ZFP:
                if ("分'秒".equals(unit) || "秒".equals(unit)) {
                    result = unit;
                } else {
                    result = "秒";
                }
                break;
            case ItemDefault.CODE_FHL:
                result = "毫升";
                break;

            case ItemDefault.CODE_HW:
                switch (arg) {

                    case 1:
                        if ("厘米".equals(unit) || "米".equals(unit) || "毫米".equals(unit)) {
                            result = unit;
                        } else {
                            result = "厘米";
                        }
                        break;

                    case 2:
                        result = "千克";
                        break;

                    default:
                        throw new IllegalArgumentException("height weight with no arg");
                }
                break;
            case ItemDefault.CODE_ZCP:
                if ("分'秒".equals(unit) || "秒".equals(unit)) {
                    result = unit;
                } else {
                    result = "秒";
                }
                break;
            case ItemDefault.CODE_WLJ:
                result = "千克";
                break;
            case ItemDefault.CODE_JGCJ:
                result = "米";
                break;
            case ItemDefault.CODE_SL:
                result = "";
                break;
            default:
                throw new IllegalArgumentException("wrong machineCode");

        }
        return result;
    }

    /**
     * 身高体重项目传入时必须保证有单位,否则无法进行转换(无法区分成绩为 身高成绩 还是 体重成绩)
     * <p>
     * 当前测试项目成绩从数据库格式转换为显示格式,返回的成绩格式可以直接使用,不需要判断
     *
     * @param machineCode 成绩对应的机器码
     * @param dbResult    数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不需要在这里转换)
     * @param digital     保留小数点位数
     * @param carryMode   进位方式 {@link #BQS}:不取舍 {@link #SSWR}:四舍五入  {@link #FLSQ}:非零舍去 {@link #FLJW}:非零进一
     * @param unit        单位,ITEM表中对应字段,目前取值范围为"厘米"、"米"、"分'秒"、"秒"、"千克"、"次"、"毫升"
     * @return 可以用于显示的成绩字符串, 如果unit为空, 或者未找到对应的单位, 会使用默认的对应的机器的单位格式(50m计时为分'秒)
     */
    public static String getStrResultForDisplay(int machineCode, int dbResult, int digital, int carryMode, String unit, int arg, boolean isReturnUnit) {
        String strResult = null;
        unit = getQualifiedUnit(machineCode, unit, arg);
        switch (unit) {

            case "厘米":
            case "米":
                strResult = analyzeLengthResult(dbResult, digital, carryMode, unit, isReturnUnit);
                break;
            case "分/秒":
            case "分.秒":
            case "分:秒":
            case "分'秒":
            case "秒":
                //TODO 时间显示默认显示百分位
                if (digital == 0) {
                    digital = 2;
                }
                strResult = DateUtil.caculateTime(dbResult, digital, carryMode);
//                strResult = analyzeTimeResult(dbResult, unit, digital, carryMode, isReturnUnit);
//                strResult = getFormatTime(dbResult,digital,carryMode);
                break;

            case "千克":
                strResult = analyzeWeightResult(dbResult, unit, digital, carryMode, isReturnUnit);
                break;

            case "毫米":
            case "个":
            case "次":
            case "毫升":
                strResult = dbResult + (isReturnUnit ? unit : "");
                break;
            case "":
                strResult = String.valueOf((double) dbResult / 10.0);
                break;
        }
        return strResult;
    }


    private static String analyzeWeightResult(int dbResult, String unit, int digital, int carryMode, boolean isReturnUnit) {
        double tmpResult = 0;
        double result;
        String strResult;
        boolean isLegalUnit = false;

        switch (unit) {

            case "千克":
                tmpResult = dbResult / 1000.0;
                isLegalUnit = true;
                break;
        }

        if (!isLegalUnit) {
            return null;
        }
        result = dealResultWithCarryMode(digital, carryMode, tmpResult);

        // strResult = result + unit;
        strResult = result + (isReturnUnit ? unit : "");
        return strResult;
    }

    private static String analyzeLengthResult(int dbResult, int digital, int carryMode, String unit, boolean isReturnUnit) {
        double tmpResult = 0;
        double result;
        String strResult;
        boolean isLegalUnit = false;
        switch (unit) {

            case "厘米":
                tmpResult = dbResult / 10.0;
                isLegalUnit = true;
                break;

            case "米":
                tmpResult = dbResult / 1000.0;
                isLegalUnit = true;
                break;

            case "毫米":
                tmpResult = dbResult;
                isLegalUnit = true;
                break;

        }
        if (!isLegalUnit) {
            return null;
        }
        result = dealResultWithCarryMode(digital, carryMode, tmpResult);
        strResult = result + (isReturnUnit ? unit : "");
        return strResult;
    }

    private static double dealResultWithCarryMode(int digital, int carryMode, double tmpResult) {
        BigDecimal bigDecimal = new BigDecimal(tmpResult);

        double result;
        switch (carryMode) {

            //四舍五入
            case SSWR:
//                tmpResult *= Math.pow(10, digital);
//                result = (int) (tmpResult + 0.5);
//                result = result / Math.pow(10, digital);
                result = bigDecimal.setScale(digital, BigDecimal.ROUND_HALF_UP).doubleValue();
                break;

            // 非零舍去
            case FLSQ:
//                result = (int) (tmpResult * Math.pow(10, digital));
//                result /= Math.pow(10, digital);
//                String pattern = "#.";
//                for (int i = 0; i < digital; i++) {
//                    pattern += "0";
//                }

                NumberFormat nf = NumberFormat.getNumberInstance();
                // 保留两位小数
                nf.setMaximumFractionDigits(digital);
                // 如果不需要四舍五入，可以使用RoundingMode.DOWN
                nf.setRoundingMode(RoundingMode.DOWN);
                result = Double.valueOf(nf.format(tmpResult).replaceAll(",", ""));

//                result = Double.valueOf(new DecimalFormat(pattern).format(tmpResult));

                break;

            // 非零进位
            case FLJW:
//                result = tmpResult * Math.pow(10, digital + 1);
//                if (result % 10 > 0) {
//                    result = (int) (result / 10 + 1);
//                } else {
//                    result = (int) (result / 10);
//                }
//                result /= Math.pow(10, digital);
                result = bigDecimal.setScale(digital, BigDecimal.ROUND_UP).doubleValue();
                break;

            // 默认不取舍
            default:
                result = tmpResult;
                break;
        }
        return result;
    }

    /**
     * 将数据中中的原有时间值转换为用于显示的目的字符串
     *
     * @param dbResult     数据库中的原有数值,单位为ms
     * @param unit
     * @param digital      保留小数点位数
     * @param carryMode    进位方式 {@link #SSWR}:四舍五入  {@link #FLSQ}:非零舍去 {@link #FLJW}:非零进一   @return 转换后的显示字符串
     * @param isReturnUnit
     */
    private static String analyzeTimeResult(int dbResult, String unit, int digital, int carryMode, boolean isReturnUnit) {
        // ms -> s
        double tmpResult = dbResult / 1000.0;
        double result;
        String strResult = "";

        if (!"分'秒".equals(unit) && !"秒".equals(unit)) {
            return null;
        }

        result = dealResultWithCarryMode(digital, carryMode, tmpResult);
        System.out.println("result:" + result);

        int hour = (int) (result / 3600);
        int minute = (int) ((result - hour * 3600) / 60);
        int second = (int) (result % 60);
        int digitalInt = (int) ((result - (int) result) * Math.pow(10, digital));

        switch (unit) {

            case "分'秒":
                if (hour != 0) {
                    // 不取舍的时候digital无效
                    if (carryMode == BQS) {
                        String ms = String.format("%.3f", result - (int) result);
                        ms = ms.substring(ms.indexOf("."), ms.length());
                        strResult = String.format(Locale.CHINA, "%2d:%02d:%02d" + ms, hour, minute, second);
                    } else {
                        strResult = String.format(Locale.CHINA, "%2d:%02d:%02d.%0" + digital + "d", hour, minute, second, digitalInt);
                    }
                } else {
                    if (carryMode == BQS) {
                        String ms = String.format("%.3f", result - (int) result);
                        ms = ms.substring(ms.indexOf("."), ms.length());
                        strResult = String.format(Locale.CHINA, "%2d:%02d" + ms, minute, second);
                    } else {
                        strResult = String.format(Locale.CHINA, "%2d:%02d.%0" + digital + "d", minute, second, digitalInt);
                    }
                }
                break;

            case "秒":
                if (carryMode == BQS) {
                    strResult = result + "";
                } else {
                    strResult = String.format(Locale.CHINA, "%d.%0" + digital + "d", (int) tmpResult, digitalInt);
                }
                break;
        }

        return strResult;
    }

    //public static void main(String args[]){
    //	String result = getStrResultForDisplay(601_351,2,FLSQ,"分'秒");
    //	System.out.println(result);
    //}

}
