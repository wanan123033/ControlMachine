package com.feipulai.common.utils.print;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zzs on  2020/8/14
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class TextSongTiView extends AppCompatTextView {
    public TextSongTiView(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public TextSongTiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public TextSongTiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }


    private void applyCustomFont(Context context) {
//        Typeface customFont = FontsUtil.getTypeface("fonts/st.ttf", context);
        Typeface customFont = FontsUtil.getTypeface("fonts/newsimsun.ttc", context);
        setTypeface(customFont);
        getPaint().setAntiAlias(true);
    }


}
