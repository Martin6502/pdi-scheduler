package com.mschneider.pdischeduler.web.screens.task;

import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskTriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@UiController("pdischeduler$Task.edit")
@UiDescriptor("task-edit.xml")
@EditedEntityContainer("taskDc")
@PrimaryEditorScreen(Task.class)
public class TaskEdit extends StandardEditor<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskEdit.class);

    @Inject
    private CollectionLoader<Project> projectLookupDl;
    @Inject
    private CollectionLoader<Task> prevtaskLookupDl;
    @Inject
    private LookupField<String> logLevelLookup;
    @Inject
    private LookupField<TaskTriggerType> triggerTypeLookup;
    @Inject
    private VBoxLayout trigger_type_cron;
    @Inject
    private VBoxLayout trigger_type_prev;
    @Inject
    private TextField<String> cronspec;
    @Inject
    private TextField<String> cronexcldates;
    @Inject
    private LookupField<Project> projectLookup;
    @Inject
    private LookupField<Task> prevTaskLookup;

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

        getWindow().getFrame().setCaption("Task Edit 2 - " + currProject.getName());
        projectLookupDl.setParameter("currProjectId", currProject.getId());
        prevtaskLookupDl.setParameter("currProjectId", currProject.getId());
        getScreenData().loadAll();
        if (projectLookup.isEmpty()) {
            projectLookup.setValue(currProject);
        }
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        // initialize logLevelLookup
        List<String> logLevelList = new ArrayList<>();
        logLevelList.add("Nothing");
        logLevelList.add("Error");
        logLevelList.add("Minimal");
        logLevelList.add("Basic");
        logLevelList.add("Detailed");
        logLevelList.add("Debug");
        logLevelList.add("Row level");
        logLevelLookup.setOptionsList(logLevelList);

        trigger_type_cron.setVisible(false);
        trigger_type_prev.setVisible(false);

        triggerTypeLookup.addValueChangeListener(e -> {
            TaskTriggerType wtype = triggerTypeLookup.getValue();
            trigger_type_cron.setVisible(false);
            trigger_type_prev.setVisible(false);
            if (wtype == TaskTriggerType.manual) {
                prevTaskLookup.setValue(null);
                cronspec.setValue(null);
                cronexcldates.setValue(null);

            } else if (wtype == TaskTriggerType.cron) {
                trigger_type_cron.setVisible(true);
                prevTaskLookup.setValue(null);

            } else if (wtype == TaskTriggerType.prevTaskAll
                    || wtype == TaskTriggerType.prevTaskOk
                    || wtype == TaskTriggerType.prevTaskErr) {
                trigger_type_prev.setVisible(true);
                cronspec.setValue(null);
                cronexcldates.setValue(null);
            }
        });
    }

}