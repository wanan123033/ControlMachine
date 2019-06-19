package com.feipulai.host.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.device.idcard.IDCardDevice;
import com.feipulai.device.qrcode.QRManager;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.feipulai.host.utils.SharedPrefsUtil;
import com.feipulai.host.view.AddStudentDialog;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

/**
 * Created by James on 2018/5/24 0024.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class BaseCheckActivity
        extends BaseActivity
        implements QRManager.QRCodeListener,
        NFCDevice.OnICCardListener,
        IDCardDevice.OnIDReadListener {
    
    private static final int STUDENT_CODE = 0x0;
    private static final int ID_CARD_NO = 0x1;
	private static final int CHECK_IN = 0x2;
	protected int hostId;
    private MyHandler mHandler = new MyHandler(this);
    
    // 用于检录设备开关
    private HandlerThread handlerThread;
    private Handler openDeviceHandler;
    private NFCDevice nfcd;
    private IDCardDevice mIDCardDevice;
    private boolean needAdd = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hostId = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.HOST_ID, 1);
        setTitle("智能主机[体测版]-" + TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()) + "\t" + hostId + "号机");
        
        nfcd = new NFCDevice();
        mIDCardDevice = new IDCardDevice();
        
        handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        
        openDeviceHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        nfcd.open(BaseCheckActivity.this);
                        mIDCardDevice.open(BaseCheckActivity.this);
                        QRManager.getInstance().startScan();
                        
                        QRManager.getInstance().setQRCodeListener(BaseCheckActivity.this);
                        nfcd.setOnICCardListener(BaseCheckActivity.this);
                        mIDCardDevice.setOnIDReadListener(BaseCheckActivity.this);
                        break;
                        
                    case 1:
                        nfcd.close();
                        mIDCardDevice.close();
                        QRManager.getInstance().stopScan();
                        break;
                }
            }
        };
    }
    
    @Override
	protected void onResume() {
		super.onResume();

		openDeviceHandler.sendEmptyMessage(0);
	}

    @Override
	protected void onPause() {
		super.onPause();
		openDeviceHandler.sendEmptyMessage(1);
		EventBus.getDefault().unregister(this);
	}
 
	protected void setAddable(boolean needAdd){
    	this.needAdd = needAdd;
	}
	
    /**
     * 真正的有考生成功的检录进来时调用,这里不需要再验证考生信息了
     * 该方法的调用就表示了这个人可以测试了
     */
    public abstract void onCheckIn(Student student);

    // 为学生报名项目
    private void registerStuItem(Student student) {
        StudentItem studentItem = new StudentItem();
        studentItem.setStudentCode(student.getStudentCode());
        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        studentItem.setItemCode(itemCode == null ? TestConfigs.DEFAULT_ITEM_CODE : itemCode);
        studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        DBManager.getInstance().insertStudentItem(studentItem);
    }

    private void checkInUIThread(Student student) {
        Message msg = Message.obtain();
        msg.what = CHECK_IN;
        msg.obj = student;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onQrArrived(String qrCode) {
		boolean needAdd = checkQulification(qrCode,STUDENT_CODE);
		if(needAdd){
			Student student = new Student();
			student.setStudentCode(qrCode);
			showAddHint(student);
		}
    }

    @Override
    public void onICCardFound() {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
		StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();
	
		if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
			toastSpeak("读卡失败");
			return;
		}
	
		boolean needAdd = checkQulification(stuInfo.getStuCode(), STUDENT_CODE);
		if (needAdd) {
			Student student = new Student();
			student.setStudentCode(stuInfo.getStuCode());
			student.setStudentName(stuInfo.getStuName());
			student.setSex(stuInfo.getSex());
			showAddHint(student);
		}
    }

    @Override
	public void onIdCardRead(IDCardInfo idCardInfo) {
		boolean needAdd = checkQulification(idCardInfo.getId(), ID_CARD_NO);
		if (needAdd) {
			Student student = new Student();
			student.setIdCardNo(idCardInfo.getId());
			showAddHint(student);
		}
    }

    public void checkInput(Student student) {
		boolean needAdd = checkQulification(student.getStudentCode(),STUDENT_CODE);
		if(needAdd){
			showAddHint(student);
		}
    }
    
    
    private boolean checkQulification(String code,int flag){
        Student student = null;
        
        switch(flag){
            
            case ID_CARD_NO:
                student = DBManager.getInstance().queryStudentByIDCode(code);
                break;
            
            case STUDENT_CODE:
                student = DBManager.getInstance().queryStudentByStuCode(code);
                break;
            
        }
        
        if (student == null) {
        	if(!needAdd){
        		toastSpeak("该考生不存在");
			}
            return needAdd;
        }
	
		StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
		if (studentItem == null) {
			if (needAdd) {
				registerStuItem(student);
				checkInUIThread(student);
			}else{
				toastSpeak("无此项目");
			}
			return false;
		} else {
			checkInUIThread(student);
			return false;
		}
    }
    
    private static class MyHandler extends Handler {

        private WeakReference<BaseCheckActivity> mReference;

        public MyHandler(BaseCheckActivity reference) {
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseCheckActivity activity = mReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
				case CHECK_IN:
                    activity.onCheckIn((Student) msg.obj);
                    break;
            }
        }
    }

    private void showAddHint(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(BaseCheckActivity.this)
                        .setCancelable(false)
                        .setTitle("提示")
                        .setMessage("无考生信息，是否新增")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AddStudentDialog(BaseCheckActivity.this).showDialog(student, false);
                            }
                        })
                        .setNegativeButton("否", null)
                        .show();
            }
        });
    }

	@Subscribe
	@Override
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            onCheckIn((Student) baseEvent.getData());
        }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handlerThread.quitSafely();
	}
	
}
