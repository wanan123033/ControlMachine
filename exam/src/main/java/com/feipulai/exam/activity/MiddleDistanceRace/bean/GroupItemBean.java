package com.feipulai.exam.activity.MiddleDistanceRace.bean;

import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.GroupItem;

import java.util.List;

/**
 * created by ww on 2019/7/8.
 */
public class GroupItemBean {
    private Group group;
    private List<GroupItem> groupItems;
    private String groupItemName;
    private String itemName;

    public GroupItemBean() {
    }

    public GroupItemBean(Group group, List<GroupItem> groupItems, String groupItemName, String itemName) {
        this.group = group;
        this.groupItems = groupItems;
        this.groupItemName = groupItemName;
        this.itemName = itemName;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<GroupItem> getGroupItems() {
        return groupItems;
    }

    public void setGroupItems(List<GroupItem> groupItems) {
        this.groupItems = groupItems;
    }

    public String getGroupItemName() {
        return groupItemName;
    }

    public void setGroupItemName(String groupItemName) {
        this.groupItemName = groupItemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return "GroupItemBean{" +
                "group=" + group +
                ", groupItems=" + groupItems +
                ", groupItemName='" + groupItemName + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
