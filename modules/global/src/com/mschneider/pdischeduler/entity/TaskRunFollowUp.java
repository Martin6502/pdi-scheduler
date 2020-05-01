package com.mschneider.pdischeduler.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum TaskRunFollowUp implements EnumClass<Integer> {

    working(1),
    done(2),
    ignore(3);

    private Integer id;

    TaskRunFollowUp(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TaskRunFollowUp fromId(Integer id) {
        for (TaskRunFollowUp at : TaskRunFollowUp.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    @Nullable
    public static TaskRunFollowUp fromStr(String str) {
        for (TaskRunFollowUp at : TaskRunFollowUp.values()) {
            if (at.name().equals(str)) {
                return at;
            }
        }
        return null;
    }
}