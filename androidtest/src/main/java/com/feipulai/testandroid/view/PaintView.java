package com.feipulai.testandroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by pengjf on 2019/3/21.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PaintView extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mPath = new Path();
    // 上次的位置
    private float mLastX;
    private float mLastY;
    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //画笔为实心
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setColor(Color.WHITE);
        //笔触为圆形
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPaint.setStrokeWidth(10f);

        setBackgroundColor(Color.LTGRAY);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                // 画出路径
                mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
                invalidate();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        invalidate();
        return true;
    }

}
