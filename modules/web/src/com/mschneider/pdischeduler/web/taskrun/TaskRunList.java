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

package com.mschneider.pdischeduler.web.taskrun;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import com.mschneider.pdischeduler.web.DateTimezoneFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class TaskRunList extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunList.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @WindowParam(name = "currTask", required = true)
    private Task currTask;

    @Inject
    private Label headline;

    @Inject
    private Datasource<Task> taskDs;

    @Inject
    private CollectionDatasource<TaskRun, UUID> taskRunsDs;

    @Inject
    private Table<TaskRun> taskRunsTable;


    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Results - " + currProject.getName() + " - " + currTask.getName());
        setHeadline();
        taskDs.setItem(currTask);

        Formatter<Date> dateTimezoneFormatter = new DateTimezoneFormatter(currProject.getTimezone());
        taskRunsTable.getColumn("startTime").setFormatter(dateTimezoneFormatter);
        taskRunsTable.getColumn("stopTime").setFormatter(dateTimezoneFormatter);

        ItemTrackingAction displayAction = new ItemTrackingAction("display") {
            @Override
            public void actionPerform(Component component) {
                openEditor("pdischeduler$TaskRun.display",
                        taskRunsTable.getSingleSelected(),
                        WindowManager.OpenType.THIS_TAB,
                        ParamsMap.of(
                                "currProject", currProject,
                                "currTask", currTask,
                                "currTaskRun", taskRunsTable.getSingleSelected()
                        )

                );
            }
        };
        taskRunsTable.addAction(displayAction);
        taskRunsTable.setItemClickAction(displayAction);

        ItemTrackingAction taskDisplayAction = new ItemTrackingAction("taskdisplay") {
            @Override
            public void actionPerform(Component component) {
                openEditor("pdischeduler$Task.display",
                        currTask,
                        WindowManager.OpenType.THIS_TAB,
                        ParamsMap.of(
                                "currProject", currProject,
                                "currTask", currTask
                        )

                );
            }
        };
        taskRunsTable.addAction(taskDisplayAction);
    }

    private void setHeadline() {
        headline.setValue("Project: " + currProject.getName()
                + ", Task: " + currTask.getName()
                + ", Timezone: " + currProject.getTimezone()
                + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getTimezone()));
    }

    public void refreshDsByTimer(com.haulmont.cuba.gui.components.Timer timer) {
        onRefreshBtnClick();
    }

    public void onRefreshBtnClick() {
        taskRunsDs.refresh();
        setHeadline();
    }

}