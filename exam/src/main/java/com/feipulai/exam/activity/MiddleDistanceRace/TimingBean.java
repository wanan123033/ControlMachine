package com.feipulai.exam.activity.MiddleDistanceRace;

/**
 * created by ww on 2019/6/14.
 */
public class TimingBean {
    public static final int TIMING_STATE_NOMAL = 0;
    public static final int TIMING_STATE_WAITING = 1;
    public static final int TIMING_STATE_BACK = 2;
    public static final int TIMING_STATE_COMPLETE = 3;
    public static final int TIMING_STATE_TIMING = 4;
    private int no;//组序号
    private int state;//组状态（0无1等待发令2违规返回3完成计时4正在计时状态）
    private long time;//发令时刻
    private String itemGroupName;

    public TimingBean(int no, int state, long time, String itemGroupName) {
        this.no = no;
        this.state = state;
        this.time = time;
        this.itemGroupName = itemGroupName;
    }

    public TimingBean() {
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

    @Override
    public String toString() {
        return "TimingBean{" +
                "no=" + no +
                ", state=" + state +
                ", time=" + time +
                ", itemGroupName='" + itemGroupName + '\'' +
                '}';
    }
}
