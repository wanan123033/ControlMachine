package com.feipulai.exam.activity.person;

import com.feipulai.common.utils.LogUtil;

import java.util.List;

public class SelectResult {
    private boolean isSelect = false;
    private String result;
    private boolean isIndex = false;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isIndex() {
        return isIndex;
    }

    public void setIndex(boolean index) {
        isIndex = index;
    }

    @Override
    public String toString() {
        return "SelectResult{" +
                "isSelect=" + isSelect +
                ", result='" + result + '\'' +
                ", isIndex=" + isIndex +
                '}';
    }

    public static List<SelectResult> copeList(List<SelectResult> list, String[] data) {
        for (int i = 0; i < data.length; i++) {
            if (list.size() > i) {
                SelectResult result = list.get(i);
                result.result = data[i];
            } else {
                SelectResult result = new SelectResult();
                result.result = data[i];
                list.add(result);
            }
        }
        LogUtil.logDebugMessage("cope" + list.toString());
        return list;
    }
}
