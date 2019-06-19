package com.feipulai.device.udp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.DataOutputStream;

/**
 * Created by zzs on  2019/6/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UdpLEDUtil {
    /**
     * 生成LED显示图片
     *
     * @param text
     * @param align
     * @return
     */
    private static Bitmap fromText(String text, Paint.Align align) {
        //字体画笔
        Paint textPaint = new Paint();
        textPaint.setTextAlign(align);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(16);
        //背景画笔
        Paint bgRect = new Paint();
        bgRect.setStyle(Paint.Style.FILL);
        bgRect.setColor(Color.BLACK);
        RectF rectF = new RectF(0, 0, 80, 16);
        Bitmap bitmap = Bitmap.createBitmap(80, 16, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //画背景
        canvas.drawRect(rectF, bgRect);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseline = rectF.centerY() + distance;
        //画文字
        if (align == Paint.Align.CENTER) {
            canvas.drawText(text, rectF.centerX(), baseline, textPaint);
        } else if (align == Paint.Align.RIGHT) {
            canvas.drawText(text, rectF.right, baseline, textPaint);
        } else {
            canvas.drawText(text, rectF.left, baseline, textPaint);
        }

        canvas.save();
        return bitmap;
    }

    /***
     * 根据图片获取LED显示指令数据
     * @param text
     * @return
     */
    public static byte[] getLedByte(String text, Paint.Align align) {
        Bitmap bitmap = fromText(text, align);
        byte[] ledData = new byte[160];
        int i, j, k;
        byte bcr[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80};
        int color;
        for (i = 0; i < 16; i++) {
            for (j = 0; j < 10; j++) {
                ledData[i * 10 + j] = 0x00;
                for (k = 0; k < 8; k++) {
                    color = bitmap.getPixel(j * 8 + k, i);
                    if (Color.red(color) < byteToInt((byte) 0x7f)) {
                        ledData[i * 10 + j] = (byte) (ledData[i * 10 + j] | bcr[8 - k - 1]);
                    }
                }
            }
        }
//        ledData = new byte[]{(byte) (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xdf, (byte) 0xfd, (byte) 0xff, (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfc, 0x01,
//                (byte) 0xfe, (byte) 0xff, (byte) 0xb0, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xbf, (byte) 0xc0, 0x07, (byte) 0xbe, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe, 0x03, (byte) 0xf7, (byte) 0xde, 0x0d, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                0x7f, (byte) 0xf7, (byte) 0xdf, (byte) 0xbb, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xf8, 0x00, (byte) 0xfb, (byte) 0xbf, (byte) 0xb0, 0x3f, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, (byte) 0xfe, (byte) 0xfb, (byte) 0xfb, (byte) 0xbf, (byte) 0x9a, (byte) 0xbf
//                , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfd, 0x05, (byte) 0xfd, 0x7e, 0x3a, (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xfb, (byte) 0xde, (byte) 0xfe, (byte) 0xff, (byte) 0xb5, (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfc, 0x01, (byte) 0xfd, 0x7f, (byte) 0xad, (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, 0x57, (byte) 0xf3, (byte) 0x9f, (byte) 0xbb, (byte) 0xbf, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe, (byte) 0xdb, (byte) 0xcf, (byte) 0xe7, 0x36, 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//        };
        return ledData;
    }

    public static int byteToInt(byte b) {
//Java 总是把 byte 当做有符处理；我们可以通过将其和 0x(byte) 0xff, 进行二进制与得到它的无符值
        return b & 0xff;
    }

    public static boolean shellExec( String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

}
