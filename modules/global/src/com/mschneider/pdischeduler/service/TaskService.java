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

package com.mschneider.pdischeduler.service;

import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.entity.TaskRunStartTrigger;

import java.util.Date;
import java.util.HashMap;

public interface TaskService {
    String NAME = "pdischeduler_TaskService";

    HashMap<String, TaskRun> getLastTaskRun(Project project);

    void taskExec(String taskUUID, TaskRunStartTrigger triggerType, boolean forceExec);

    boolean isValidCronSpec(String str);

    Date getNextDate(String timezone, String cronSpec, String cronExclDates);
    
}