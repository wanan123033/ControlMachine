package com.feipulai.exam.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zzs on  2019/6/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MySpinner extends android.support.v7.widget.AppCompatSpinner {

    private ItemClick mItemClick;

    public MySpinner(Context context) {
        super(context);
        init();
    }

    public MySpinner(Context context, int mode) {
        super(context, mode);
        init();
    }

    public MySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MySpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        init();
    }


    public void setItemClick(ItemClick mItemClick) {
        this.mItemClick = mItemClick;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void init() {
        Class<?> myClass = Spinner.class;
        try {
            Class<?>[] params = new Class[1];
            params[0] = OnItemClickListener.class;
            Method m = myClass.getDeclaredMethod("setOnItemClickListenerInt", params);
            m.setAccessible(true);
            m.invoke(this, new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class<?> myClass = AdapterView.class;
                    try {
                        Field field = myClass.getDeclaredField("mOldSelectedPosition");
                        field.setAccessible(true);
                        field.setInt(MySpinner.this, AdapterView.INVALID_POSITION);

                        mItemClick.onClick(position);

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public interface ItemClick {
        void onClick(int position);
    }
}
