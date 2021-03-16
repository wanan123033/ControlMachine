package com.feipulai.exam;

import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.exam.utils.EncryptUtil;
import com.orhanobut.logger.Logger;

import java.util.Random;

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
    }

}
