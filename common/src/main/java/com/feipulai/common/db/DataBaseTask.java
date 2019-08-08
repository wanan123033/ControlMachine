package com.feipulai.common.db;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.view.LoadingDialog;

/**
 * 纯数据库操作事务
 * <p/>
 * All rights reserved.
 */
public abstract class DataBaseTask implements Runnable {
    /**
     * 等待提示信息
     */
    protected String processMsg = "";
    /**
     * 窗口是否可取消
     */
    protected boolean cancelable = true;

    /**
     * 加载提示框
     */
    protected LoadingDialog loadingDialog;
    /**
     * 数据库操作返回数据
     */
    protected DataBaseRespon respon;
    /**
     * 请求成功
     */
    protected static final int REQUEST_SUCCESS = 11000;
    /**
     * 请求失败
     */
    protected static final int REQUEST_FAIL = 11200;

    /**
     * 无参构造函数
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2014年4月23日,上午11:30:51
     * <br/> UpdateTime:  2014年4月23日,上午11:30:51
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     */
    public DataBaseTask() {
    }

    ;

    /**
     * 有参构造函数
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2014年4月19日,下午2:29:03
     * <br/> UpdateTime:  2014年4月19日,下午2:29:03
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param context    上下文
     * @param processMsg 处理提示文本信息
     */
    public DataBaseTask(Context context, String processMsg, boolean cancelable) {
        this.processMsg = processMsg;
        this.cancelable = cancelable;

        if (!TextUtils.isEmpty(this.processMsg)) {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.showDialog(processMsg, this.cancelable);
            loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                }
            });
        }
    }

    @Override
    public void run() {

        // 执行数据库操作
        respon = executeOper();
        if (respon.isSuccess()) {
            HandlerUtil.sendMessage(mHandler, REQUEST_SUCCESS, respon);
        } else {
            HandlerUtil.sendMessage(mHandler, REQUEST_FAIL, respon);
        }

    }

    /**
     * 执行数据库操作
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2014年4月23日,上午10:27:22
     * <br/> UpdateTime:  2014年4月23日,上午10:27:22
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @return 数据库操作返回数据
     */
    public abstract DataBaseRespon executeOper();

    /**
     * 数据库操作成功回调
     * <p/>
     * <br/> Version: 1.0
     * <br/> CreateTime:  2014年4月23日,上午11:11:01
     * <br/> UpdateTime:  2014年4月23日,上午11:11:01
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param respon 数据库操作返回数据
     */
    public abstract void onExecuteSuccess(DataBaseRespon respon);

    /**
     * <br/> Version: 1.0
     * <br/> CreateTime:  2014年4月23日,上午11:21:31
     * <br/> UpdateTime:  2014年4月23日,上午11:21:31
     * <br/> CreateAuthor:  CodeApe
     * <br/> UpdateAuthor:  CodeApe
     * <br/> UpdateInfo:  (此处输入修改内容,若无修改可不写.)
     *
     * @param respon
     */
    public abstract void onExecuteFail(DataBaseRespon respon);


    /**
     * 异步处理Handler对象
     */
    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_SUCCESS:// 数据库操作成功
                    //				ProcessDialogUtil.dismissDialog();
                    if (loadingDialog != null) {
                        loadingDialog.dismissDialog();
                    }
                    onExecuteSuccess((DataBaseRespon) msg.obj);
                    break;
                case REQUEST_FAIL:// 数据库操作失败
                    //				ProcessDialogUtil.dismissDialog();
                    if (loadingDialog != null) {
                        loadingDialog.dismissDialog();
                    }
                    onExecuteFail((DataBaseRespon) msg.obj);
                    break;
            }
        }

    };


}
