package com.feipulai.exam.activity.MiddleDistanceRace.bean;

import com.feipulai.exam.entity.Schedule;

public class ServiceTcpBean {
    private Schedule schedule;
    private String itemName;

    public ServiceTcpBean(Schedule schedule, String itemName) {
        this.schedule = schedule;
        this.itemName = itemName;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return "ServiceTcpBean{" +
                "schedule=" + schedule +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
