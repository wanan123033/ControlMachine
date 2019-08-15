package com.feipulai.exam.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 项目日程表
 * Created by zzs on  2018/12/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Entity(
		// Define indexes spanning multiple columns here.
		indexes = {
				@Index(value = " itemCode ASC , scheduleNo ASC ", unique = true)
		}
)
public class ItemSchedule {
	
    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private String itemCode;
    @NotNull
    private String scheduleNo;
    @Generated(hash = 831422966)
    public ItemSchedule(Long id, @NotNull String itemCode,
            @NotNull String scheduleNo) {
        this.id = id;
        this.itemCode = itemCode;
        this.scheduleNo = scheduleNo;
    }
    @Generated(hash = 1344254478)
    public ItemSchedule() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getItemCode() {
        return this.itemCode;
    }
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
    public String getScheduleNo() {
        return this.scheduleNo;
    }
    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }

   
    
}
