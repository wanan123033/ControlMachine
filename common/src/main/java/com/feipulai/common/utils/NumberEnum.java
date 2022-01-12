package com.feipulai.common.utils;

public enum  NumberEnum {


    ZERO(0,"⓪"),

    ONE(1,"①"),

    TWO(2,"②"),

    THREE(3,"③"),

    FOUR(4,"④"),

    FIVE(5,"⑤"),

    SIX(6,"⑥"),

    SEVEN(7,"⑦"),

    EIGHT(8,"⑧"),

    NINE(9,"⑨"),

    TEN(10,"⑩");
    private int number;
    private String value;

    NumberEnum(int number, String value) {
        this.number = number;
        this.value = value;
    }
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static NumberEnum valueOfTo(int val) {

        for (NumberEnum m : NumberEnum.values()) {
            if (val == m.getNumber())
                return m;
        }
        return null;
    }


}