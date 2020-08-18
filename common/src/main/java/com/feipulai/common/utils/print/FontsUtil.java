package com.feipulai.common.utils.print;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by zzs on  2020/8/17
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class FontsUtil {

    private static HashMap<String, Typeface> fontCache = new HashMap<>();

    public static Typeface getTypeface(String fontname, Context context) {
        Typeface typeface = fontCache.get(fontname);

        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), fontname);
            } catch (Exception e) {
                return null;
            }

            fontCache.put(fontname, typeface);
        }

        return typeface;
    }
}
