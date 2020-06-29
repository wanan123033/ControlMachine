package com.ww.fpl.libarcface.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

/**
 * created by ww on 2020/4/24.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class YV12Util {

    private static int width = 1024;
    private static int height = 600;

    private static int size = width * height;
    private static byte[] nv21Bytes = new byte[size + size / 2];

    public static byte[] YV12toNV21(byte[] input) {
        int quarter = size / 4;
        int uPosition = size + quarter; // This is where U starts

        System.arraycopy(input, 0, nv21Bytes, 0, size); // Y is same

        for (int i = 0; i < quarter; i++) {
            nv21Bytes[size + i * 2] = input[size + i]; // For NV21, V first
            nv21Bytes[size + i * 2 + 1] = input[uPosition + i]; // For Nv21, U second
        }
        return nv21Bytes;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize这里默认设置200*250
        options.inSampleSize = calculateInSampleSize(options, 200, 250);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;

        Log.i("bitmap---", width + "---" + height);
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        Log.i("缩放比例", "------" + inSampleSize);
        return inSampleSize;
    }

    public static Bitmap getSmallBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, 200, 250, true);
    }
}
