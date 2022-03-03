package com.feipulai.exam;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.exam.utils.EncryptUtil;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class Algorithm {

    public static void main(String[] args) {

        Random random = new Random();//指定种子数字

        System.out.println(random.nextInt(2));
        System.out.println(random.nextInt(1));
        System.out.println(random.nextInt(1));

//        int a[] = new int[100000];
//        int b[] = new int[901];
//
//        for (int i = 0; i < a.length; i++) {
//            a[i] = (int) ((Math.random() * 898) + 3);
//            // System.out.println(a[i]);
//        }
//
//        for (int i = 0; i < a.length; i++) {
//            b[a[i]]++;
//        }
//        int cursor = 0;
//        for (int i = 3; i <= 900; i++) {
//            for (int j = 0; j < b[i]; j++) {
//                a[cursor++] = i;
//            }
//        }
//
//        for (int i = 0; i < a.length; i++) {
//            System.out.print(a[i] + " ");
//        }
//        System.out.println("\n" + a.length);

//        byte d = (byte) 0xfd;
//        System.out.println("======>" + d);


//        byte[] bytes = new byte[]{00, 0x38, 0x39, (byte) 0x91, 0x5a, 0x20, (byte) 0xff};
//        System.out.println("======>" + new String(bytes));

//        System.out.println("======>" + formatTime2(1635147285883L,"yyyyMMddHHmmss.SSS"));
        System.out.println("======>" + formatTime(12580, "ss.SS"));
        System.out.println("======>" + formatTime(12580, "ss.SS"));
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

        String[] resultArray = new String[list.size()];
        list.toArray(resultArray);
        System.out.println("======>" + Arrays.toString(resultArray));
    }

    public static String formatTime(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));//计时时间换算不需要加8
        return sdf.format(new Date(timeMillis));
    }

    public static String formatTime2(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));//时钟时间换算需要加8
        return sdf.format(new Date(timeMillis));
    }
}
