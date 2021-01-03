package com.brugui.dermalcheck.data.model;

import java.io.Serializable;

public class Status  implements Serializable {
    public static final String PENDING_STATUS_REFERENCE = "2MPSfDTgcdQ77IPZAblt";
    public static final String PENDING_STATUS_NAME = "Pendiente";
    public static final String ACCEPTED_STATUS_NAME = "Aceptada";
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