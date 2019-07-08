package com.feipulai.exam.activity.MiddleDistanceRace;

/**
 * created by ww on 2019/7/5.
 */
public class DataUtil {

    /**
     * int[]转byte[]
     *
     * @param data
     * @return
     */
    public static byte[] byteArray2RgbArray(int[] data) {
        byte[] byteArr = new byte[data.length * 4];
        for (int i = 0; i < data.length; i++) {
            byteArr[i * 4 + 0] = (byte) ((data[i] >> 24) & 0xff);
            byteArr[i * 4 + 1] = (byte) ((data[i] >> 16) & 0xff);
            byteArr[i * 4 + 2] = (byte) ((data[i] >> 8) & 0xff);
            byteArr[i * 4 + 3] = (byte) (data[i] & 0xff);
        }
        return byteArr;
    }

    /**
     * byte数组还原为int数组
     *
     * @param data
     * @return
     */
    public static int[] byteArray2RgbArray(byte[] data) {
        if (data.length % 4 != 0) {
            return null;
        }
        int[] intarr = new int[data.length / 4];

        int i1, i2, i3, i4;
        for (int j = 0, k = 0; j < intarr.length; j++, k += 4)//j循环int,k循环byte数组
        {
            i1 = data[k];
            i2 = data[k + 1];
            i3 = data[k + 2];
            i4 = data[k + 3];
            if (i1 < 0) {
                i1 += 256;
            }
            if (i2 < 0) {
                i2 += 256;
            }
            if (i3 < 0) {
                i3 += 256;
            }
            if (i4 < 0) {
                i4 += 256;
            }
            intarr[j] = (i1 << 24) + (i2 << 16) + (i3 << 8) + (i4 << 0);//保存Int数据类型转换
        }
        return intarr;
    }

    /**
     * int转byte[]
     * 该方法将一个int类型的数据转换为byte[]形式，因为int为32bit，而byte为8bit所以在进行类型转换时，知会获取低8位，
     * 丢弃高24位。通过位移的方式，将32bit的数据转换成4个8bit的数据。注意 &0xff，在这当中，&0xff简单理解为一把剪刀，
     * 将想要获取的8位数据截取出来。
     * @param i 一个int数字
     * @return byte[]
     */
    public static byte[] int2ByteArray(int i){
        byte[] result=new byte[4];
        result[0]=(byte)((i >> 24)& 0xFF);
        result[1]=(byte)((i >> 16)& 0xFF);
        result[2]=(byte)((i >> 8)& 0xFF);
        result[3]=(byte)(i & 0xFF);
        return result;
    }
    /**
     * byte[]转int
     * 利用int2ByteArray方法，将一个int转为byte[]，但在解析时，需要将数据还原。同样使用移位的方式，将适当的位数进行还原，
     * 0xFF为16进制的数据，所以在其后每加上一位，就相当于二进制加上4位。同时，使用|=号拼接数据，将其还原成最终的int数据
     * @param bytes byte类型数组
     * @return int数字
     */
    public static int bytes2Int(byte[] bytes){
        int num=bytes[3] & 0xFF;
        num |=((bytes[2] <<8)& 0xFF00);
        num |=((bytes[1] <<16)& 0xFF0000);
        num |=((bytes[0] <<24)& 0xFF0000);
        return num;
    }
}
