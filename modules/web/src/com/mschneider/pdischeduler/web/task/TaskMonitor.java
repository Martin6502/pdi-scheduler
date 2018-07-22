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

package com.mschneider.pdischeduler.web.task;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.security.entity.EntityOp;
import com.mschneider.pdischeduler.entity.*;
import com.mschneider.pdischeduler.service.TaskService;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class TaskMonitor extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(TaskMonitor.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @Inject
    private Label headline;

    @Inject
    private Table<Task> tasksTable;

    @Inject
    private Datasource<Project> currProjectDs;

    @Inject
    private CollectionDatasource<Task, UUID> currProjectTasksDs;

    @Inject
    private TaskService taskService;

    @Inject
    private Messages messages;

    @Inject
    private Security security;

    @Inject
    private Button execSingleBtn;

    @Inject
    private Button execSequenceBtn;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        super.setCaption("Task Monitor - " + currProject.getName());
        setHeadline();

        currProjectDs.setItem(currProject);

        // enhance datasource with latest taskRun infos
        currProjectTasksDs.addStateChangeListener(event -> {
            if (event.getState() == Datasource.State.VALID) {
                HashMap<String, TaskRun> lastTaskRuns = taskService.getLastTaskRun(currProject);
                for (Task task : currProjectTasksDs.getItems()) {
                    if (lastTaskRuns.containsKey(task.getId().toString())) {
                        List<TaskRun> lastRunList = new ArrayList<>();
                        lastRunList.add(lastTaskRuns.get(task.getId().toString()));
                        task.setLastTaskRun(lastRunList);
                    }
                    // logger.debug("fulfill currProjectTasksDs with lastTaskRun: task=" + task.getName());
                }
                ((DatasourceImplementation) currProjectTasksDs).valid();
            }
        });

        ItemTrackingAction taskrunAction = new ItemTrackingAction("taskrun") {
            @Override
            public void actionPerform(Component component) {
                openWindow("pdischeduler$TaskRun.list",
                        WindowManager.OpenType.NEW_TAB,
                        ParamsMap.of(
                                "currProject", currProject,
                                "currTask", tasksTable.getSingleSelected()
                        )

                );
            }
        };
        tasksTable.addAction(taskrunAction);
        tasksTable.setItemClickAction(taskrunAction);

        ItemTrackingAction displayAction = new ItemTrackingAction("display") {
            @Override
            public void actionPerform(Component component) {
                openEditor("pdischeduler$Task.display",
                        tasksTable.getSingleSelected(),
                        WindowManager.OpenType.THIS_TAB,
                        ParamsMap.of(
                                "currProject", currProject,
                                "currTask", tasksTable.getSingleSelected()
                        )

                );
            }
        };
        tasksTable.addAction(displayAction);

        ItemTrackingAction execSingleAction = new ItemTrackingAction("execSingle") {
            @Override
            public void actionPerform(Component component) {
                logger.info("execSingleAction");
                if (tasksTable.getSingleSelected() != null) {
                    taskService.taskExec(tasksTable.getSingleSelected().getId().toString(), TaskRunStartTrigger.manualSingle);
                }
                onRefreshBtnClick();
            }
        };
        tasksTable.addAction(execSingleAction);

        ItemTrackingAction execSequenceAction = new ItemTrackingAction("execSequence") {
            @Override
            public void actionPerform(Component component) {
                logger.info("execSequenceAction");
                if (tasksTable.getSingleSelected() != null) {
                    taskService.taskExec(tasksTable.getSingleSelected().getId().toString(), TaskRunStartTrigger.manualSeq);
                }
                onRefreshBtnClick();
            }
        };
        tasksTable.addAction(execSequenceAction);

        // disable manual execution of task if no permission
        if (!security.isEntityOpPermitted(TaskRun.class, EntityOp.CREATE)) {
            execSingleBtn.setVisible(false);
            execSingleAction.setEnabled(false);
            execSingleAction.setVisible(false);

            execSequenceBtn.setVisible(false);
            execSequenceAction.setEnabled(false);
            execSequenceAction.setVisible(false);
        }

    }

    public Component generateNextRunCell(Task entity) {
        if (entity != null && entity.getActive() && entity.getTriggerType() == TaskTriggerType.cron) {
            try {
                Date nextFireAt = taskService.getNextDate(currProject.getTimezone(), entity.getCronSpec(), entity.getCronExclDates());
                String str;
                if (nextFireAt != null) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
                    ZonedDateTime zdt = TimeZoneUtils.zonedDateTimeFromDateWithTimezone(nextFireAt, currProject.getTimezone());
                    str = dtf.format(zdt);
                } else {
                    str = "unknown";
                }
                return new Table.PlainTextCell(str);
            } catch (Exception se) {
                logger.error("generateNextRunCell failed", se);
            }
        }
        return null;
    }


    public Component generateLtrStatusCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null) {
            String str = messages.getMessage(entity.getLastTaskRun().get(0).getStatus());
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStartTriggerCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null) {
            String str = messages.getMessage(entity.getLastTaskRun().get(0).getStartTrigger());
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStartTimeCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null
                && entity.getLastTaskRun().get(0).getStartTime() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
            String str = dtf.format(TimeZoneUtils.zonedDateTimeFromUtcDateConvertToTimezone(
                    entity.getLastTaskRun().get(0).getStartTime(), currProject.getTimezone()));
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStopTimeCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null
                && entity.getLastTaskRun().get(0).getStopTime() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
            String str = dtf.format(TimeZoneUtils.zonedDateTimeFromUtcDateConvertToTimezone(
                    entity.getLastTaskRun().get(0).getStopTime(), currProject.getTimezone()));
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrDurationSecCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null
                && entity.getLastTaskRun().get(0).getDurationSec() != null) {
            String str = entity.getLastTaskRun().get(0).getDurationSec().toString();
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrLogTextCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null) {
            String str = entity.getLastTaskRun().get(0).getLogText();
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrResultCodeCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null) {
            String str = entity.getLastTaskRun().get(0).getResultCode();
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrResultTextCell(Task entity) {
        if (entity != null && entity.getLastTaskRun() != null) {
            String str = entity.getLastTaskRun().get(0).getResultHTML();
            return new Table.PlainTextCell(str);
        }
        return null;
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
        currProjectTasksDs.refresh();
        setHeadline();
    }

}