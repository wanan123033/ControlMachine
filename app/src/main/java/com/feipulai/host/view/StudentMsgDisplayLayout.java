package com.feipulai.host.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.host.R;
import com.feipulai.host.entity.Student;

/**
 * Created by James on 2018/6/4 0004.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class StudentMsgDisplayLayout extends RelativeLayout{
	
	public StudentMsgDisplayLayout(Context context){
		super(context);
		init();
	}
	
	public StudentMsgDisplayLayout(Context context,AttributeSet attrs){
		this(context,attrs,0);
	}
	
	public StudentMsgDisplayLayout(Context context,AttributeSet attrs,int defStyleAttr){
		super(context,attrs,defStyleAttr);
		init();
	}
	
	private void init(){
		View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_details_message,null);
		addView(view);
	}
	
	/**
	 * 为该view设置学生信息
	 *
	 * @param student
	 */
	public void setStudent(Student student){
		TextView tvStuName = (TextView)findViewById(R.id.tv_stuName);
		tvStuName.setText(student.getStudentName() == null ? "" : student.getStudentName());
		
		TextView tvStuCode = (TextView)findViewById(R.id.tv_stuCode);
		tvStuCode.setText(student.getStudentCode());
		
		TextView tvSex = (TextView)findViewById(R.id.tv_sex);
		tvSex.setText(student.getSex() == 0 ? "男" : "女");
		
		TextView tvCardNum = (TextView)findViewById(R.id.tvidcard_num);
		tvCardNum.setText(student.getIdCardNo() == null ? "" : student.getIdCardNo());
		
		TextView tvClassName = (TextView)findViewById(R.id.tv_classname);
		tvClassName.setText(student.getClassName() == null ? "" : student.getGradeName());
		
		TextView tvGradeName = (TextView)findViewById(R.id.tv_gradename);
		tvGradeName.setText(student.getGradeName() == null ? "" : student.getGradeName());
	}
	
}
