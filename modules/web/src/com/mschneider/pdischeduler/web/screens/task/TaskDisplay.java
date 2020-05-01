package com.mschneider.pdischeduler.web.screens.task;

import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UiController("pdischeduler$Task.display")
@UiDescriptor("task-display.xml")
@EditedEntityContainer("taskDc")
public class TaskDisplay extends StandardEditor<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskDisplay.class);

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

        getScreenData().loadAll();

        headline.setValue("Project: " + currProject.getName() + ", Timezone: " + currProject.getTimezone()
                + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getTimezone()));
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
    }



}