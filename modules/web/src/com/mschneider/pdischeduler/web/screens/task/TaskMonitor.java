package com.mschneider.pdischeduler.web.screens.task;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.security.entity.EntityOp;
import com.mschneider.pdischeduler.entity.*;
import com.mschneider.pdischeduler.service.TaskService;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import com.mschneider.pdischeduler.web.screens.project.ProjectDisplay;
import com.mschneider.pdischeduler.web.screens.taskrun.TaskRunList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import javax.inject.Inject;

@SuppressWarnings("NullableProblems")
@UiController("pdischeduler$Task.monitor")
@UiDescriptor("task-monitor.xml")
@LookupComponent("tasksTable")
public class TaskMonitor extends StandardLookup<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskMonitor.class);

    @Inject
    private CollectionLoader<Task> tasksDl;
    @Inject
    private Table<Task> tasksTable;
    @Inject
    protected Messages messages;
    @Inject
    private ScreenBuilders screenBuilders;
    @Inject
    private TaskService taskService;
    @Inject
    private Button execSingleBtn;
    @Inject
    private Button execSequenceBtn;
    @Inject
    private Button stopBtn;
    @Inject
    private Security security;

    @Inject
    private Label<String> headline;


    // screen parameter
    private Project currProject;
    public void setCurrProject(Project project) {
        currProject = project;
    }

    private HashMap<String, TaskRun> lastTaskRuns;

    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        logger.debug("onBeforeShow started");
        if (currProject == null)
            throw new IllegalStateException("project parameter is null");

        getWindow().getFrame().setCaption("Task Monitor - " + currProject.getName());
        onRefreshBtnClick();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        tasksTable.setStyleProvider((entity, property) -> {
            if (("ltrStatus".equals(property) || "ltrResultCode".equals(property))) {
                TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
                if (taskRun != null) {
                    String str = taskRun.getResultCode();
                    if ("FATAL".equals(str) || "ERROR".equals(str)) {
                        return "error";
                    } else if ("WARN".equals(str)) {
                        return "warn";
                    }
                }
            }
            return null;
        });

        /*
         **  add special actions
         */
        ItemTrackingAction taskrunAction = new ItemTrackingAction("taskrun") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        taskRunCall(currTask);
                    }
                }
            }
        };
        tasksTable.addAction(taskrunAction);
        tasksTable.setItemClickAction(taskrunAction);

        ItemTrackingAction taskdisplayAction = new ItemTrackingAction("taskdisplay") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        taskDisplayCall(currTask);
                    }
                }
            }
        };
        tasksTable.addAction(taskdisplayAction);

        ItemTrackingAction projectdisplayAction = new ItemTrackingAction("projectdisplay") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        projectDisplayCall(currProject);
                    }
                }
            }
        };
        tasksTable.addAction(projectdisplayAction);

        ItemTrackingAction execSingleAction = new ItemTrackingAction("execSingle") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        execSingleTaskCall(currTask);
                    }
                }
            }
        };
        tasksTable.addAction(execSingleAction);

        ItemTrackingAction execSequenceAction = new ItemTrackingAction("execSequence") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        execSequenceTaskCall(currTask);
                    }
                }
            }
        };
        tasksTable.addAction(execSequenceAction);

        ItemTrackingAction stopAction = new ItemTrackingAction("stop") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        stopTaskCall(currTask);
                    }
                }
            }
        };
        tasksTable.addAction(stopAction);

        // disable manual execution of task if no permission
        if (!security.isEntityOpPermitted(TaskRun.class, EntityOp.CREATE)) {
            execSingleBtn.setVisible(false);
            execSingleAction.setEnabled(false);
            execSingleAction.setVisible(false);

            execSequenceBtn.setVisible(false);
            execSequenceAction.setEnabled(false);
            execSequenceAction.setVisible(false);

            stopBtn.setVisible(false);
            stopAction.setEnabled(false);
            stopAction.setVisible(false);
        }

    }

    /*
    **  field generator
    */
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
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getStatus() != null) {
            String str = messages.getMessage(taskRun.getStatus());
            logger.debug("generateLtrStatusCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStartTriggerCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getStartTrigger() != null) {
            String str = messages.getMessage(taskRun.getStartTrigger());
            logger.debug("generateLtrStartTriggerCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStartTimeCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getStartTime() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
            String str = dtf.format(TimeZoneUtils.zonedDateTimeFromUtcDateConvertToTimezone(
                    taskRun.getStartTime(), currProject.getTimezone()));
            logger.debug("generateLtrStartTimeCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrStopTimeCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getStopTime() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx");
            String str = dtf.format(TimeZoneUtils.zonedDateTimeFromUtcDateConvertToTimezone(
                    taskRun.getStopTime(), currProject.getTimezone()));
            logger.debug("generateLtrStopTimeCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrDurationSecCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getDurationSec() != null) {
            String str = taskRun.getDurationSec().toString();
            logger.debug("generateLtrStopTimeCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrLogTextCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getLogText() != null) {
            String str = taskRun.getLogText();
            logger.debug("generateLtrLogTextCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrResultCodeCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getResultCode() != null) {
            String str = taskRun.getResultCode();
            logger.debug("generateLtrResultCodeCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateLtrResultTextCell(Task entity) {
        TaskRun taskRun = lastTaskRuns.get(entity.getId().toString());
        if (taskRun != null && taskRun.getResultHTML() != null) {
            String str = taskRun.getResultHTML();
            logger.debug("generateLtrResultTextCell: " + entity.getName() + ", status: "+ str);
            return new Table.PlainTextCell(str);
        }
        return null;
    }


    /*
     **  screen caller
     */
    private void taskRunCall(Task task) {
        TaskRunList screen = screenBuilders.lookup(TaskRun.class, this)
                .withScreenClass(TaskRunList.class)
                .withOpenMode(OpenMode.NEW_TAB)
                .build();
        screen.setCurrProject(currProject);
        screen.setCurrTask(task);
        screen.show();
    }

    private void taskDisplayCall(Task task) {
        TaskDisplay screen = screenBuilders.editor(Task.class, this)
                .editEntity(task)
                .withScreenClass(TaskDisplay.class)
                .build();
        screen.setCurrProject(currProject);
        screen.show();
    }

    private void projectDisplayCall(Project project) {
        ProjectDisplay screen = screenBuilders.editor(Project.class, this)
                .editEntity(project)
                .withScreenClass(ProjectDisplay.class)
                .build();
        screen.show();
    }

    private void execSingleTaskCall(Task task) {
        logger.debug("execSingleTaskCall started");
        taskService.taskExec(task.getId().toString(), TaskRunStartTrigger.manualSingle, false);
        onRefreshBtnClick();
    }

    private void execSequenceTaskCall(Task task) {
        logger.debug("execSequenceTaskCall started");
        taskService.taskExec(task.getId().toString(), TaskRunStartTrigger.manualSeq, false);
        onRefreshBtnClick();
    }

    private void stopTaskCall(Task task) {
        logger.debug("stopTaskCall started");
        taskService.taskStop(task.getId().toString());
        onRefreshBtnClick();
    }

    /*
     **  refresh
     */
    public void refreshTimerCall(Timer timer) {
        onRefreshBtnClick();
    }

    public void onRefreshBtnClick() {
        // get latest taskRun
        lastTaskRuns = taskService.getLastTaskRun(currProject);
        setHeadline();

        tasksDl.setParameter("currProjectId", currProject.getId());
        tasksDl.load();
    }

    private void setHeadline() {
        headline.setValue("Project: " + currProject.getName()
                + ", Timezone: " + currProject.getTimezone()
                + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getTimezone()));
    }



}