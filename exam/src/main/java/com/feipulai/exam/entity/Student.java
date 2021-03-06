package com.feipulai.exam.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.feipulai.exam.utils.StringChineseUtil;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/11/24.
 */
@Entity
public class Student implements Serializable {
    public static final String ENCRYPT_KEY = "ID_CARD_ENCRYPT1";
    public static final int MALE = 0;
    public static final int FEMALE = 1;

    private static final long serialVersionUID = 6302346624591600338L;
    @Id(autoincrement = true)
    private Long id;//学生ID
    @Unique
    @NotNull
    private String studentCode;//考号
    private String studentName;//姓名
    private int sex;//性别 0-男  1-女
    @Unique
    private String idCardNo;//身份证号
    private String icCardNo;//IC卡号
    private String className;//班级
    private String schoolName;//学校名称
    private String downloadTime;//下载时间  时间戳

    @ToMany(joinProperties = {@JoinProperty(name = "studentCode", referencedName = "studentCode")})
    private List<StudentItem> studentItemList;
    private String portrait;//头像
    private String faceFeature;
    @Unique
    private String remark1;
    private String remark2;
    private String remark3;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1943931642)
    private transient StudentDao myDao;


    @Generated(hash = 1556870573)
    public Student() {
    }

    @Generated(hash = 76774790)
    public Student(Long id, @NotNull String studentCode, String studentName, int sex, String idCardNo,
                   String icCardNo, String className, String schoolName, String downloadTime, String portrait,
                   String faceFeature, String remark1, String remark2, String remark3) {
        this.id = id;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.sex = sex;
        this.idCardNo = idCardNo;
        this.icCardNo = icCardNo;
        this.className = className;
        this.schoolName = schoolName;
        this.downloadTime = downloadTime;
        this.portrait = portrait;
        this.faceFeature = faceFeature;
        this.remark1 = remark1;
        this.remark2 = remark2;
        this.remark3 = remark3;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return this.studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getIdCardNo() {
        return this.idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getIcCardNo() {
        return this.icCardNo;
    }

    public void setIcCardNo(String icCardNo) {
        this.icCardNo = icCardNo;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getDownloadTime() {
        return this.downloadTime;
    }

    public void setDownloadTime(String downloadTime) {
        this.downloadTime = downloadTime;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getRemark1() {
        return this.remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return this.remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getRemark3() {
        return this.remark3;
    }

    public void setRemark3(String remark3) {
        this.remark3 = remark3;
    }

    public String getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(String faceFeature) {
        this.faceFeature = faceFeature;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep
    @Generated(hash = 1192546563)
    public List<StudentItem> getStudentItemList() {
        if (studentItemList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            StudentItemDao targetDao = daoSession.getStudentItemDao();
            List<StudentItem> studentItemListNew = targetDao._queryStudent_StudentItemList(studentCode);
            synchronized (this) {
                if (studentItemList == null) {
                    studentItemList = studentItemListNew;
                }
            }
        }
        return studentItemList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 390827565)
    public synchronized void resetStudentItemList() {
        studentItemList = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    @Override
    public String toString() {
        return "Student{" +
                ", 考号='" + studentCode + '\'' +
                ", 姓名='" + studentName + '\'' +
                ", 姓别=" + sex +
                ", 身份证号='" + idCardNo + '\'' +
                "id=" + id +
                // ", daoSession=" + daoSession +
                // ", myDao=" + myDao +
                '}';
    }

    public String getLEDStuName() {
        if (!TextUtils.isEmpty(studentName) && studentName.length() > 0) {
            try {
                byte[] nameByte = studentName.getBytes("GBK");
                if (nameByte.length > 8) {
                    byte[] newName = new byte[8];
                    System.arraycopy(nameByte, nameByte.length - newName.length, newName, 0, 8);
                    return new String(newName, "GBK");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return studentName;
    }

    public Bitmap getBitmapPortrait() {
        if (TextUtils.isEmpty(portrait)) {
            return null;
        }
        byte[] bytes = Base64.decode(portrait, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

    public String getSpeakStuName() {
        return StringChineseUtil.toChinese(studentName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return sex == student.sex &&
                studentCode.equals(student.studentCode) &&
                studentName.equals(student.studentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentCode, studentName, sex);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1701634981)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getStudentDao() : null;
    }
}
