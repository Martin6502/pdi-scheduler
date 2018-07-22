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
import com.mschneider.pdischeduler.entity.Task;

import java.util.Collection;

@SuppressWarnings("unused")
public interface ExportImportTaskService {
    String NAME = "pdischeduler_ExportImportTaskService";

    String exportTasksToCsv(Collection<Task> tasks);

    int importTasksFromCsv(Project project, String content);

    boolean isExistent(Project project, String taskName);

    Task getByName(Project project, String taskName);

    String getUniqueName(Project project, String taskName);

}