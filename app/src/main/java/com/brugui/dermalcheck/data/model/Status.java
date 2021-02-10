package com.brugui.dermalcheck.data.model;

import java.io.Serializable;

public class Status  implements Serializable {
    public static final String PENDING_STATUS_REFERENCE = "2MPSfDTgcdQ77IPZAblt";
    public static final String PENDING_STATUS_NAME = "Pending";
    public static final String DIAGNOSED_STATUS_NAME = "Diagnosed";
    private String name;

    public Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Status(){}

    @Override
    public String toString() {
        return "Status{" +
                "name='" + name + '\'' +
                '}';
    }
}
