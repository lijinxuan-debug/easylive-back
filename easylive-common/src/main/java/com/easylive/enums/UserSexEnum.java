package com.easylive.enums;

public enum UserSexEnum {
    WOMAN(0, "女"),
    MAN(1, "男"),
    UNKNOWN(2, "未知");

    private Integer type;

    private String desc;

    UserSexEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getCode() {
        return type;
    }

    public String getMsg() {
        return desc;
    }
}
