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
    private long id;
    @NotNull
    private String itemCode;
    @NotNull
    private String scheduleNo;

    @Keep
    @Generated(hash = 1847413446)
    public ItemSchedule(long id, @NotNull String itemCode,
            @NotNull String scheduleNo) {
        this.id = id;
        this.itemCode = itemCode;
        this.scheduleNo = scheduleNo;
    }
    @Keep
    @Generated(hash = 1344254478)
    public ItemSchedule() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getScheduleNo() {
        return scheduleNo;
    }

    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }

    @Override
    public String toString() {
        return "ItemSchedule{" +
                "id=" + id +
                ", itemCode='" + itemCode + '\'' +
                ", scheduleNo='" + scheduleNo + '\'' +
                '}';
    }
    
}
