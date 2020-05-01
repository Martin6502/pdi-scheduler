package com.mschneider.pdischeduler.web.screens.taskrun;

import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Timer;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.entity.TaskRunStatus;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import com.mschneider.pdischeduler.web.screens.project.ProjectDisplay;
import com.mschneider.pdischeduler.web.screens.task.TaskDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@SuppressWarnings("NullableProblems")
@UiController("pdischeduler$TaskRun.listproject")
@UiDescriptor("task-run-list-project.xml")
@LookupComponent("taskRunsTable")
public class TaskRunListProject extends StandardLookup<TaskRun> {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunListProject.class);

    @Inject
    private CollectionLoader<TaskRun> taskRunsDl;
    @Inject
    private Table<TaskRun> taskRunsTable;
    @Inject
    private ScreenBuilders screenBuilders;

    @Inject
    private Label<String> headline;


    // screen parameter
    private Project currProject;
    public void setCurrProject(Project project) {
        currProject = project;
    }


    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        logger.debug("onBeforeShow started");
        if (currProject == null)
            throw new IllegalStateException("project parameter is null");

        getWindow().getFrame().setCaption("Results - " + currProject.getName());

        onRefreshBtnClick();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        taskRunsTable.setStyleProvider((entity, property) -> {
            if ("status".equals(property) || "resultCode".equals(property)) {
                if (entity.getStatus() == TaskRunStatus.error
                        || entity.getStatus() == TaskRunStatus.timeout
                        || "FATAL".equals(entity.getResultCode())
                        || "ERROR".equals(entity.getResultCode())) {
                    return "error";
                } else if ("WARN".equals(entity.getResultCode())) {
                    return "warn";
                }
            }
            return null;
        });

        /*
         **  add special actions
         */
        ItemTrackingAction taskRunDisplayAction = new ItemTrackingAction("taskrundisplay") {
            @Override
            public void actionPerform(Component component) {
                if (taskRunsTable.getSelected().size() == 1) {
                    TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                    if (currTaskRun != null) {
                        taskRunDisplayCall(currTaskRun);
                    }
                }
            }
        };
        taskRunsTable.addAction(taskRunDisplayAction);
        taskRunsTable.setItemClickAction(taskRunDisplayAction);

        ItemTrackingAction taskDisplayAction = new ItemTrackingAction("taskdisplay") {
            @Override
            public void actionPerform(Component component) {
                if (taskRunsTable.getSelected().size() == 1) {
                    TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                    if (currTaskRun != null) {
                        taskDisplayCall(currTaskRun.getTask());
                    }
                }
            }
        };
        taskRunsTable.addAction(taskDisplayAction);

        ItemTrackingAction projectDisplayAction = new ItemTrackingAction("projectdisplay") {
            @Override
            public void actionPerform(Component component) {
                if (taskRunsTable.getSelected().size() == 1) {
                    TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                    if (currTaskRun != null) {
                        projectDisplayCall(currTaskRun.getTask().getProject());
                    }
                }
            }
        };
        taskRunsTable.addAction(projectDisplayAction);

        ItemTrackingAction editFollowUpAction = new ItemTrackingAction("editfollowup") {
            @Override
            public void actionPerform(Component component) {
                if (taskRunsTable.getSelected().size() == 1) {
                    TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                    if (currTaskRun != null) {
                        editFollowUpCall(currTaskRun);
                    }
                }
            }
        };
        taskRunsTable.addAction(editFollowUpAction);

    }

    /*
     **  field generator
     */
    public Component generateStartTimeField(TaskRun entity) {
        if (entity.getStartTime() != null) {
            String str = TimeZoneUtils.strFromUtcDateConvertToTimezone(entity.getStartTime(), currProject.getTimezone());
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    public Component generateStopTimeField(TaskRun entity) {
        if (entity.getStopTime() != null) {
            String str = TimeZoneUtils.strFromUtcDateConvertToTimezone(entity.getStopTime(), currProject.getTimezone());
            return new Table.PlainTextCell(str);
        }
        return null;
    }

    /*
     **  screen caller
     */
    private void taskRunDisplayCall(TaskRun taskRun) {
        TaskRunDisplay screen = screenBuilders.editor(TaskRun.class, this)
                .editEntity(taskRun)
                .withScreenClass(TaskRunDisplay.class)
                .build();
        screen.setCurrProject(currProject);
        screen.setCurrTask(taskRun.getTask());
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

    private void editFollowUpCall(TaskRun taskRun) {
        TaskRunEditFollowUp screen = screenBuilders.editor(TaskRun.class, this)
                .editEntity(taskRun)
                .withScreenClass(TaskRunEditFollowUp.class)
                .withAfterCloseListener(afterScreenCloseEvent -> {
                    if (afterScreenCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        onRefreshBtnClick();
                    }
                })
                .build();
        screen.setCurrProject(taskRun.getTask().getProject());
        screen.setCurrTask(taskRun.getTask());
        screen.show();
    }

    /*
     **  refresh
     */
    public void refreshTimerCall(Timer timer) {
        onRefreshBtnClick();
    }

    public void onRefreshBtnClick() {
        // logger.info("onRefreshBtnClick called");
        taskRunsDl.setParameter("currProjectId", currProject.getId());
        taskRunsDl.load();

        setHeadline();
    }

    private void setHeadline() {
        headline.setValue("Project: " + currProject.getName()
                + ", Timezone: " + currProject.getTimezone()
                + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getTimezone()));
    }

}
