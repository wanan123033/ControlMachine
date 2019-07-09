package com.feipulai.exam.activity.MiddleDistanceRace.bean;

import com.feipulai.exam.entity.Group;

/**
 * created by ww on 2019/7/8.
 */
public class GroupItemBean {
    private Group group;
    private int studentNumber;
    private String groupItemName;

    public GroupItemBean(Group group, int studentNumber, String groupItemName) {
        this.group = group;
        this.studentNumber = studentNumber;
        this.groupItemName = groupItemName;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public int getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(int studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getGroupItemName() {
        return groupItemName;
    }

    public void setGroupItemName(String groupItemName) {
        this.groupItemName = groupItemName;
    }

    @Override
    public String toString() {
        return "GroupItemBean{" +
                "group=" + group +
                ", studentNumber=" + studentNumber +
                ", groupItemName='" + groupItemName + '\'' +
                '}';
    }
}
