package com.feipulai.exam.activity.MiddleDistanceRace;

/**
 * created by ww on 2019/6/14.
 */
public class TimingBean {
    /**
     * 初始状态
     */
    public static final int TIMING_STATE_NOMAL = 0;
    public static final int TIMING_STATE_WAITING = 1;
    public static final int TIMING_STATE_BACK = 2;
    public static final int TIMING_STATE_COMPLETE = 3;
    public static final int TIMING_STATE_TIMING = 4;
    public static final int TIMING_START = 666;//开始计时

    public static final int GROUP_3 = 3;//空闲(Group表中字段)
    public static final int GROUP_4 = 4;//关联(Group表中字段)
    public static final int GROUP_FINISH = 5;//完成(Group表中字段)
    private int no;//组序号
    private int state;//组状态（0无1等待发令2违规返回3完成计时4正在计时状态）
    private long time;//发令时刻
    private String itemCode;
    private String itemGroupName;//组名
    private int color;//组颜色
    private int studentNo;//考生人数

    public TimingBean(int no, int state, long time, String itemCode, String itemGroupName, int color, int studentNo) {
        this.no = no;
        this.state = state;
        this.time = time;
        this.itemCode = itemCode;
        this.itemGroupName = itemGroupName;
        this.color = color;
        this.studentNo = studentNo;
    }

    public TimingBean() {
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getItemGroupName() {
        return itemGroupName;
    }

    public void setItemGroupName(String itemGroupName) {
        this.itemGroupName = itemGroupName;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(int studentNo) {
        this.studentNo = studentNo;
    }

    @Override
    public String toString() {
        return "TimingBean{" +
                "no=" + no +
                ", state=" + state +
                ", time=" + time +
                ", itemCode='" + itemCode + '\'' +
                ", itemGroupName='" + itemGroupName + '\'' +
                ", color=" + color +
                ", studentNo=" + studentNo +
                '}';
    }
}
