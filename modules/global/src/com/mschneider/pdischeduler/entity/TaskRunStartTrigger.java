/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum TaskRunStartTrigger implements EnumClass<Integer> {

    cron(0),
    prevTask(1),
    manualSeq(2),
    manualSingle(3);

    private final Integer id;

    TaskRunStartTrigger(Integer value) {
        this.id = value;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static TaskRunStartTrigger fromId(Integer id) {
        for (TaskRunStartTrigger at : TaskRunStartTrigger.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    @Nullable
    public static TaskRunStartTrigger fromStr(String str) {
        for (TaskRunStartTrigger at : TaskRunStartTrigger.values()) {
            if (at.name().equals(str)) {
                return at;
            }
        }
        return null;
    }

}