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
import com.mschneider.pdischeduler.entity.Project;
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
public class TaskRunListProject extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunList.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @Inject
    private Label headline;

    @Inject
    private CollectionDatasource<TaskRun, UUID> taskRunsDs;

    @Inject
    private Table<TaskRun> taskRunsTable;


    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Results - " + currProject.getName());
        setHeadline();

        Formatter<Date> dateTimezoneFormatter = new DateTimezoneFormatter(currProject.getTimezone());
        taskRunsTable.getColumn("startTime").setFormatter(dateTimezoneFormatter);
        taskRunsTable.getColumn("stopTime").setFormatter(dateTimezoneFormatter);

        ItemTrackingAction displayAction = new ItemTrackingAction("display") {
            @Override
            public void actionPerform(Component component) {
                TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                if (currTaskRun != null) {
                    openEditor("pdischeduler$TaskRun.display",
                            currTaskRun,
                            WindowManager.OpenType.THIS_TAB,
                            ParamsMap.of(
                                    "currProject", currProject,
                                    "currTask", currTaskRun.getTask(),
                                    "currTaskRun", currTaskRun
                            )
                    );
                }
            }
        };
        taskRunsTable.addAction(displayAction);
        taskRunsTable.setItemClickAction(displayAction);

        ItemTrackingAction taskDisplayAction = new ItemTrackingAction("taskdisplay") {
            @Override
            public void actionPerform(Component component) {
                TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                if (currTaskRun != null) {
                    openEditor("pdischeduler$Task.display",
                            currTaskRun.getTask(),
                            WindowManager.OpenType.THIS_TAB,
                            ParamsMap.of(
                                    "currProject", currProject,
                                    "currTask", currTaskRun.getTask()
                            )

                    );
                }
            }
        };
        taskRunsTable.addAction(taskDisplayAction);

    }

    private void setHeadline() {
        headline.setValue("Project: " + currProject.getName()
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