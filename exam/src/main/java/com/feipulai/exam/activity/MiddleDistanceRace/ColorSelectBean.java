package com.feipulai.exam.activity.MiddleDistanceRace;

/**
 * created by ww on 2019/6/25.
 */
public class ColorSelectBean {
    private int colorId;
    private boolean isSelect;

    public ColorSelectBean() {
    }

    public ColorSelectBean(int colorId, boolean isSelect) {
        this.colorId = colorId;
        this.isSelect = isSelect;
    }

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public String toString() {
        return "ColorSelectBean{" +
                "colorId=" + colorId +
                ", isSelect=" + isSelect +
                '}';
    }
}
