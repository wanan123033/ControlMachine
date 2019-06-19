package com.feipulai.host.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity(
		// Define indexes spanning multiple columns here.
		indexes = {
				@Index(value = "studentCode ASC,itemCode ASC, machineCode ASC", unique = true)
		}
)
public class StudentItem{
	
	@Id(autoincrement = true)
	private Long id;//学生项目ID
	@NotNull
	private String studentCode;//考号
	@NotNull
	private String itemCode;//默认为default
	@NotNull
	private int machineCode;
	
	private String remark1;
	private String remark2;
	private String remark3;

	@Generated(hash = 1420021159)
	public StudentItem(Long id, @NotNull String studentCode,
			@NotNull String itemCode, int machineCode, String remark1, String remark2,
			String remark3) {
		this.id = id;
		this.studentCode = studentCode;
		this.itemCode = itemCode;
		this.machineCode = machineCode;
		this.remark1 = remark1;
		this.remark2 = remark2;
		this.remark3 = remark3;
	}

	@Generated(hash = 383807586)
	public StudentItem() {
	}
	
	public Long getId(){
		return id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	
	public String getStudentCode(){
		return studentCode;
	}
	
	public void setStudentCode(String studentCode){
		this.studentCode = studentCode;
	}
	
	public String getItemCode(){
		return itemCode;
	}
	
	public void setItemCode(String itemCode){
		this.itemCode = itemCode;
	}
	
	public int getMachineCode(){
		return machineCode;
	}
	
	public void setMachineCode(int machineCode){
		this.machineCode = machineCode;
	}
	
	public String getRemark1(){
		return remark1;
	}
	
	public void setRemark1(String remark1){
		this.remark1 = remark1;
	}
	
	public String getRemark2(){
		return remark2;
	}
	
	public void setRemark2(String remark2){
		this.remark2 = remark2;
	}
	
	public String getRemark3(){
		return remark3;
	}
	
	public void setRemark3(String remark3){
		this.remark3 = remark3;
	}
}
