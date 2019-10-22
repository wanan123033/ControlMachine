package com.feipulai.host.exl;

import android.text.TextUtils;

import com.feipulai.common.exl.ExlListener;
import com.feipulai.common.exl.ExlReader;
import com.feipulai.common.exl.ExlReaderUtil;
import com.feipulai.common.exl.GetReaderDataListener;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.MyApplication;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.db.MachineItemCodeUtil;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.entity.StudentItem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class StuItemExLReader extends ExlReader {

    private List<String> mNecessaryCols;
    private Map<String, Integer> mColNums;
    //	private String path;
    private boolean mHasIDCardCol;// 导入时是否有身份证信息,身份证信息时可选项
    private String mItemName;
    private String mItemCode;
    private UsbFile file;
    private ExlReaderUtil reader;

    public StuItemExLReader(ExlListener listener) {
        super(listener);
        String[] cols = {"性别", "学籍号", "姓名", "项目", "项目代码"};
        mNecessaryCols = Arrays.asList(cols);
    }

    @Override
    protected void read(UsbFile file) {

        this.file = file;
        mColNums = new HashMap<>();
        final List<ExelReadBean> result = new ArrayList<>();
        reader = new ExlReaderUtil.Builder(file).setCellLength(7).setReaderDataListener(new GetReaderDataListener() {
            @Override
            public void readerLineData(int rowNum, List<String> data) {
                if (rowNum == 0) {
                    boolean isSucceed = readFirstRow(data);
                    reader.setStop(!isSucceed);
                } else {
                    ExelReadBean bean = generateBeanFromRow(data);
                    if (bean == null) {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                        Logger.i(TestConfigs.df.format(new Date()) + "---> " + "Excel读取解析失败,第" + rowNum + 1 + "行读取失败");
                        return;
                    }
                    result.add(bean);
                }
            }
        }).setExlListener(listener).build();
        reader.read();
        if (result == null || result.size() == 0) {
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "导入文档数据为空");
            return;
        }

        boolean success = insertIntoDB(result);
        if (success) {
            listener.onExlResponse(ExlListener.EXEL_READ_SUCCESS, "Excel导入成功!");
        }
    }

    private boolean insertIntoDB(List<ExelReadBean> result) {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_HW) {
            for (ExelReadBean bean : result) {
                bean.setItemCode(TestConfigs.HEIGHT_ITEM_CODE);
            }
            insertDB(result);
            return true;
        }

        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        // 项目代码还是默认的,需要更新当前的项目的项目代码,并且将之前有的 报名信息 和 成绩 的项目代码更改
        Item nameItem = DBManager.getInstance().queryItemByName(mItemName);
        if (itemCode == null) {
            try {
                Logger.i(mItemCode + " :  " + mItemName);
                if (nameItem == null) {
                    TestConfigs.sCurrentItem.setItemCode(mItemCode);
                    TestConfigs.sCurrentItem.setItemName(mItemName);
                    DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息
                } else {
                    if (TestConfigs.sCurrentItem.getMachineCode() == nameItem.getMachineCode()) {
                        TestConfigs.sCurrentItem.setItemCode(mItemCode);
                        TestConfigs.sCurrentItem.setItemName(mItemName);
                        DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息
                    } else {
                        listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目名已存在,拒绝导入");
                        Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目名已存在,拒绝导入");
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目代码已存在,拒绝导入");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,拒绝导入" + e.getMessage());
                return false;
            }
            List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
            List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, mItemCode);
            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, mItemCode);
        } else if (!itemCode.equals(mItemCode)) {
            // 已经有了项目代码,那么导入时的项目项目代码必须与已有的相同
            listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目代码与已有项目代码不同,拒绝导入");
            Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目代码与已有项目代码不同,拒绝导入");
            return false;
        } else if (!mItemName.equals(TestConfigs.sCurrentItem.getItemName())) {
            if (nameItem == null) {
                TestConfigs.sCurrentItem.setItemName(mItemName);
                DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息(这里实际只更新了一个项目名)
            } else {
                if (TestConfigs.sCurrentItem.getMachineCode() == nameItem.getMachineCode()) {
                    TestConfigs.sCurrentItem.setItemName(mItemName);
                    DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息(这里实际只更新了一个项目名)
                } else {
                    listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "excel导入失败,导入项目名已存在,拒绝导入");
                    Logger.i(TestConfigs.df.format(new Date()) + "---> " + "excel导入失败,导入项目名已存在,拒绝导入");
                }
            }


        }

        TestConfigs.sCurrentItem.setItemName(mItemName);
        if (itemCode == null) {
            TestConfigs.sCurrentItem.setItemCode(mItemCode);
            SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, mItemCode);
            List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault();
            List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault();
            MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, mItemCode);
        }
        DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);// 更新项目表中信息

        insertDB(result);
        return true;
    }

    private void insertDB(List<ExelReadBean> result) {
        List<Student> stuList = new LinkedList<>();
        List<StudentItem> stuItemList = new LinkedList<>();

        for (ExelReadBean bean : result) {
            Student student = new Student();
            student.setStudentCode(bean.getStudentCode());
            student.setStudentName(bean.getStudentName());
            student.setSex(bean.getSex());
            student.setIdCardNo(bean.getIdCardNo());
            student.setIcCardNo(bean.getIcCardNo());
            stuList.add(student);

            StudentItem studentItem = new StudentItem();
            studentItem.setStudentCode(bean.getStudentCode());
            studentItem.setItemCode(bean.getItemCode());
            studentItem.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());

            stuItemList.add(studentItem);
        }

        // 插入学生信息
        DBManager.getInstance().insertStudentList(stuList);
        // 插入学生项目信息
        DBManager.getInstance().insertStuItemList(stuItemList);
    }

    // 一行数据,生成一个对象,如果读取失败直接返回null
    private ExelReadBean generateBeanFromRow(List<String> data) {

        ExelReadBean bean = new ExelReadBean();
        String stuCode = data.get(2);
        String sex = data.get(1);
        String stuName = data.get(0);
        String itemName = data.get(5);
        String itemCode = data.get(6);
        String icCodeNo = data.get(4);
        bean.setIcCardNo(icCodeNo);
        if (TextUtils.isEmpty(stuCode)) {
            return null;
        }
        bean.setStudentCode(stuCode);

        if ("男".equals(sex)) {
            bean.setSex(Student.MALE);
        } else if ("女".equals(sex)) {
            bean.setSex(Student.FEMALE);
        } else {
            // 不男不女
            return null;
        }

        if (TextUtils.isEmpty(stuName)) {
            return null;
        }
        bean.setStudentName(stuName);

        if (mItemName == null) {
            mItemName = itemName;
        }
        if (TextUtils.isEmpty(itemName) || !mItemName.equals(itemName)) {
            // 表里面所有行的项目名必须相同
            return null;
        }
        bean.setItemName(itemName);

        if (mItemCode == null) {
            mItemCode = itemCode;
        }
        if (TextUtils.isEmpty(itemCode) || !mItemCode.equals(itemCode)) {
            // 表里面所有行的项目代码必须相同
            return null;
        }
        bean.setItemCode(itemCode);

        if (mHasIDCardCol) {
            String idCardNo = data.get(3);
            if (!TextUtils.isEmpty(idCardNo)) {
                bean.setIdCardNo(idCardNo);
            }
        }
        return bean;
    }

    /**
     * 读取第一行标题，检验格式是否正确，是否有缺失必填项
     *
     * @param data 第一行
     * @return 第一行处理成功, 返回true;否则返回false
     */
    private boolean readFirstRow(List<String> data) {
        mHasIDCardCol = false;
        for (int i = 0; i < data.size(); i++) {
            // 必须有"(*)"号
            String cellValue = data.get(i);
            if (data.get(i).contains("*")) {
                cellValue = cellValue.substring(0, cellValue.indexOf("*") - 1);
            }
            mColNums.put(cellValue, i);
            if (cellValue.equals("身份证号")) {
                mHasIDCardCol = true;
            }
        }

        //检查是否有所有需要的索引
        for (int i = 0; i < mNecessaryCols.size(); i++) {
            if (!mColNums.containsKey(mNecessaryCols.get(i))) {
                listener.onExlResponse(ExlListener.EXEL_READ_FAIL, "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                Logger.i(TestConfigs.df.format(new Date()) + "---> " + "缺少必要列:" + mNecessaryCols.get(i) + ",excel读取失败");
                return false;
            }
        }
        return true;
    }


}
