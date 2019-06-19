package com.feipulai.exam;

public class Algorithm {

    public static void main(String[] args) {
        int a[] = new int[100000];
        int b[] = new int[901];

        for (int i = 0; i < a.length; i++) {
            a[i] = (int) ((Math.random() * 898) + 3);
            // System.out.println(a[i]);
        }

        for (int i = 0; i < a.length; i++) {
            b[a[i]]++;
        }
        int cursor = 0;
        for (int i = 3; i <= 900; i++) {
            for (int j = 0; j < b[i]; j++) {
                a[cursor++] = i;
            }
        }

        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println("\n" + a.length);
    }

}
