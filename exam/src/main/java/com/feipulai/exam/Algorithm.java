package com.feipulai.exam;

import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.exam.utils.EncryptUtil;
import com.orhanobut.logger.Logger;

public class Algorithm {

    public static void main(String[] args) {
        Logger.d("test" + EncryptUtil.setEncryptString(PrintBean.ENCRY_KEY, "440513122145221554"));
        Logger.d("test" + EncryptUtil.setEncryptString(PrintBean.ENCRY_KEY, "440513122145221554"));
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
    }

}
