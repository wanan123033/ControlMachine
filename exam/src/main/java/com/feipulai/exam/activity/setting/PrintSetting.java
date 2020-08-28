package com.feipulai.exam.activity.setting;

import android.text.TextUtils;

import com.feipulai.exam.MyApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzs on  2020/8/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintSetting implements Serializable {

    private String tableHeadleJson;

    private String signatureJson;
    /**
     * 成绩打印
     * 0 最好成绩
     * 1 最好成绩+ 轮次成绩
     */
    private int printResultType = 0;

    public int getPrintResultType() {
        return printResultType;
    }

    public void setPrintResultType(int printResultType) {
        this.printResultType = printResultType;
    }

    public String getTableHeadleJson() {
        return tableHeadleJson;
    }

    public void setTableHeadleJson(String tableHeadleJson) {
        this.tableHeadleJson = tableHeadleJson;
    }

    public String getSignatureJson() {
        return signatureJson;
    }

    public void setSignatureJson(String signatureJson) {
        this.signatureJson = signatureJson;
    }


    public List<PrintItem> getTableHeadleList() {
        List<PrintItem> list;
        if (TextUtils.isEmpty(tableHeadleJson)) {
            list = new ArrayList<>();
            String[] signature = new String[]{"序号", "考号", "姓名", "学校", "成绩", "得分", "名次", "签名"};
            for (String s : signature) {
                list.add(new PrintItem(true, s));
            }
            return list;
        }
        list = new Gson().fromJson(tableHeadleJson, new TypeToken<List<PrintItem>>() {
        }.getType());
        return list;
    }

    public List<PrintItem> getSignatureList() {
        List<PrintItem> list;
        if (TextUtils.isEmpty(signatureJson)) {
            list = new ArrayList<>();
            String[] signature = new String[]{"操作员", "记录员", "主考官", "纪检员"};
            for (String s : signature) {
                list.add(new PrintItem(true, s));
            }
            return list;
        }
        list = new Gson().fromJson(signatureJson, new TypeToken<List<PrintItem>>() {
        }.getType());
        return list;
    }

    public String[] getTableString() {
        List<String> tableList = new ArrayList<>();
        for (PrintItem printItem : getTableHeadleList()) {
            if (printItem.isUse) {
                tableList.add(printItem.getName());
            } else {
                tableList.add(null);
            }
        }
        return tableList.toArray(new String[tableList.size()]);
    }
    public String[] getSignString() {
        List<String> tableList = new ArrayList<>();
        for (PrintItem printItem : getSignatureList()) {
            if (printItem.isUse) {
                tableList.add(printItem.getName());
            } else {
                tableList.add(null);
            }
        }
        return tableList.toArray(new String[tableList.size()]);
    }

    public static class PrintItem implements Serializable {

        private boolean isUse;
        private String name;

        public PrintItem(boolean isUse, String name) {
            this.isUse = isUse;
            this.name = name;
        }

        public boolean isUse() {
            return isUse;
        }

        public void setUse(boolean use) {
            isUse = use;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
