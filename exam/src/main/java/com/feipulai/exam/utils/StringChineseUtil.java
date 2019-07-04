package com.feipulai.exam.utils;

/**
 * Created by zzs on  2019/7/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StringChineseUtil {
    public static String toChinese(String data) {
        String[] s1 = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

        String result = "";
        int n = data.length();
        for (int i = 0; i < n; i++) {
            try {
                int num = data.charAt(i) - '0';
                result += s1[num];
            } catch (Exception e) {
                result += data.charAt(i);
            }
        }
        return result;

    }
}