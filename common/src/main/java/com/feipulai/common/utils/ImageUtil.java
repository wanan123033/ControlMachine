package com.feipulai.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zzs on  2020/1/9
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ImageUtil {
    /**
     * 将bitmap转为base64格式的字符串
     *
     * @param bit 传入的bitmap
     * @return
     */
    public static String bitmapToStrByBase64(Bitmap bit) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
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

    /**
     * 图片压缩比例
     */
    private int compressScale = 50;

    /**
     * 保存位图到本地文件
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param saveParentPath // 文件保存目录
     * @param fileName       文件名称
     * @param bitmap         位图对象
     */
    public static void saveBitmapToFile(String saveParentPath, String fileName, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        try {
            File saveimg = new File(saveParentPath + fileName);
            if (saveimg.exists()) {
                return;
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveimg));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
