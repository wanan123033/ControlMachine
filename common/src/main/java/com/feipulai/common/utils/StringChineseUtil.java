package com.feipulai.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * 验证合法文件名称
     *
     * @param fileName
     * @return
     */
    public static boolean patternFileName(String fileName) {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher m = pattern.matcher(fileName);
        return m.matches();
    }


    private static final Pattern PATTERN = Pattern.compile("0|([-]?[1-9][0-9]*)");
    private static boolean isInt(String str) {
        return PATTERN.matcher(str).matches();
    }
}
