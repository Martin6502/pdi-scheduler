package com.mschneider.pdischeduler.web.screens.taskrun;

import com.haulmont.cuba.gui.components.BrowserFrame;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.StreamResource;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.global.UserSession;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@UiController("pdischeduler$TaskRun.editfollowup")
@UiDescriptor("task-run-edit-follow-up.xml")
@EditedEntityContainer("taskRunDc")
public class TaskRunEditFollowUp extends StandardEditor<TaskRun> {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunEditFollowUp.class);

    @Inject
    private InstanceContainer<TaskRun> taskRunDc;
    @Inject
    private Label<String> headline;
    @Inject
    private UserSession userSession;
    @Inject
    private TextArea<String> logText;
    @Inject
    private BrowserFrame resultHTML;

    // screen parameter
    private Project currProject;
    public void setCurrProject(Project project) {
        currProject = project;
    }

    private Task currTask;
    public void setCurrTask(Task task) {
        currTask = task;
    }


    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        logger.debug("onBeforeShow started");
        if (currProject == null)
            throw new IllegalStateException("project parameter is null");

        if (currTask == null)
            throw new IllegalStateException("task parameter is null");

        getScreenData().loadAll();

        TaskRun currTaskRun = taskRunDc.getItem();

        headline.setValue("Project: " + currProject.getName() + ", Task: " + currTask.getName() + ", Timezone: " + currProject.getTimezone());

        // set start/stop time formatted
        String startTimeFormatted = null;
        if (currTaskRun.getStartTime() != null) {
            startTimeFormatted = TimeZoneUtils.strFromUtcDateConvertToTimezone(currTaskRun.getStartTime(), currProject.getTimezone());
        }
        String stopTimeFormatted = null;
        if (currTaskRun.getStopTime() != null) {
            stopTimeFormatted = TimeZoneUtils.strFromUtcDateConvertToTimezone(currTaskRun.getStopTime(), currProject.getTimezone());
        }
        currTaskRun.setStartTimeFormatted(startTimeFormatted);
        currTaskRun.setStopTimeFormatted(stopTimeFormatted);

        if (currTaskRun.getLogText() != null) {
            logText.setVisible(true);
        } else {
            logText.setVisible(false);
        }

        if (currTaskRun.getResultHTML() != null) {
            byte[] bytes = currTaskRun.getResultHTML().getBytes(StandardCharsets.UTF_8);
            resultHTML.setSource(StreamResource.class)
                    .setStreamSupplier(() -> new ByteArrayInputStream(bytes))
                    .setMimeType("text/html");
            resultHTML.setVisible(true);
        } else {
            resultHTML.setVisible(false);
        }

        currTaskRun.setFollowUpUser(userSession.getUser().getName());
    }


    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");

    }

    @Override
    protected void preventUnsavedChanges(BeforeCloseEvent event) {
        // do nothing
    }

}