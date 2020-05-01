package com.mschneider.pdischeduler.web.screens.project;

import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@UiController("pdischeduler$Project.display")
@UiDescriptor("project-display.xml")
@EditedEntityContainer("projectDc")
@LoadDataBeforeShow
public class ProjectDisplay extends StandardEditor<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDisplay.class);


    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");

    }
}