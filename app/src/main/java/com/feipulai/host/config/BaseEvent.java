package com.feipulai.host.config;


public class BaseEvent {
    protected int tagInt = -999;
    protected Object data = null;
    protected Object eventData = null;
    protected String tagString = null;

    public BaseEvent(int tagInt) {
        this.tagInt = tagInt;
    }

    public BaseEvent(Object data, int tagInt) {
        this.tagInt = tagInt;
        this.data = data;
    }

    public BaseEvent(Object data, String tagString) {
        this.data = data;
        this.tagString = tagString;
    }

    public BaseEvent(Object data, int tagInt, String tagString) {
        this.tagInt = tagInt;
        this.data = data;
        this.tagString = tagString;
    }

    public BaseEvent(Object data, Object eventData, String tagString, int tagInt) {
        this.tagInt = tagInt;
        this.data = data;
        this.eventData = eventData;
        this.tagString = tagString;
    }

    public BaseEvent(Object data, Object eventData, int tagInt) {
        this.tagInt = tagInt;
        this.data = data;
        this.eventData = eventData;
    }

    public int getTagInt() {
        return tagInt;
    }

    public void setTagInt(int tagInt) {
        this.tagInt = tagInt;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }
}
