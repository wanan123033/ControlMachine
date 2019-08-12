package com.feipulai.device;

/**
 * Created by zzs on  2019/8/5
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SysConvertUtil {
    public static String convert16To2(int num) {

        String string2 = Integer.toBinaryString(num & 0xFF);
        String data="";
        if (string2.length()<8){
            int addLength=8-string2.length();
            for (int i = 0; i < addLength; i++) {
                data+="0";
            }
            data+=string2;
        }else {
            data=string2;
        }
        return data;
//        //& 两两为1即为1
//        //>>>无符号右移
//        /**
//         * eg.60
//         *       0000-0000 0000-0000 0000-0000 0011-1100   60的二进制表示
//         * &     0000-0000 0000-0000 0000-0000 0000-1111   15的二进制表示
//         * &后的值   0000-0000 0000-0000 0000-0000 0000-1100          值为12即16进制的C
//         */
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < 8; i++) {
//            int temp = num & 15;
//            if (temp > 9) {
//                sb.append((char) (temp - 10 + 'A'));//强转成16进制
//
//            } else {
//                sb.append(temp);
//            }
//            num = num >>> 4;
//        }
//        return sb.reverse().toString();
    }
}
