package com.mschneider.pdischeduler.web.screens.project;

import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

@UiController("pdischeduler$Project.edit")
@UiDescriptor("project-edit.xml")
@EditedEntityContainer("projectDc")
@LoadDataBeforeShow
@PrimaryEditorScreen(Project.class)
public class ProjectEdit extends StandardEditor<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectEdit.class);

    @Inject
    private LookupField<String> timezoneLookup;

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        // initialize timezone lookup
        timezoneLookup.setOptionsMap(TimeZoneUtils.getLookupList());

    }

}