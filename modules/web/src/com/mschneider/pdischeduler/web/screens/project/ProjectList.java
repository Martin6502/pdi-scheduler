package com.mschneider.pdischeduler.web.screens.project;

import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.entity.EntityOp;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.web.screens.task.TaskBrowse;
import com.mschneider.pdischeduler.web.screens.task.TaskMonitor;
import com.mschneider.pdischeduler.web.screens.taskrun.TaskRunListProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@SuppressWarnings("NullableProblems")
@UiController("pdischeduler$Project.list")
@UiDescriptor("project-list.xml")
@LookupComponent("projectsTable")
@LoadDataBeforeShow
public class ProjectList extends StandardLookup<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectList.class);

    @Inject
    private Table<Project> projectsTable;
    @Inject
    private ScreenBuilders screenBuilders;
    @Inject
    private Button taskbrowseBtn;
    @Inject
    private Security security;

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        ItemTrackingAction taskmonitorAction = new ItemTrackingAction("taskmonitor") {
            @Override
            public void actionPerform(Component component) {
                if (projectsTable.getSelected().size() == 1) {
                    Project currProject = projectsTable.getSingleSelected();
                    if (currProject != null) {
                        taskmonitorCall(currProject);
                    }
                }
            }
        };
        projectsTable.addAction(taskmonitorAction);
        projectsTable.setItemClickAction(taskmonitorAction);

        ItemTrackingAction taskbrowseAction = new ItemTrackingAction("taskbrowse") {
            @Override
            public void actionPerform(Component component) {
                if (projectsTable.getSelected().size() == 1) {
                    Project currProject = projectsTable.getSingleSelected();
                    if (currProject != null) {
                        taskbrowseCall(currProject);
                    }
                }
            }
        };
        projectsTable.addAction(taskbrowseAction);

        ItemTrackingAction taskrunmonitorAction = new ItemTrackingAction("taskrunmonitor") {
            @Override
            public void actionPerform(Component component) {
                if (projectsTable.getSelected().size() == 1) {
                    Project currProject = projectsTable.getSingleSelected();
                    if (currProject != null) {
                        taskrunmonitorCall(currProject);
                    }
                }
            }
        };
        projectsTable.addAction(taskrunmonitorAction);

        ItemTrackingAction projectdisplayAction = new ItemTrackingAction("projectdisplay") {
            @Override
            public void actionPerform(Component component) {
                if (projectsTable.getSelected().size() == 1) {
                    Project currProject = projectsTable.getSingleSelected();
                    if (currProject != null) {
                        displayProjectCall(currProject);
                    }
                }
            }
        };
        projectsTable.addAction(projectdisplayAction);

        // disable manual execution of task if no permission
        if (!security.isEntityOpPermitted(Task.class, EntityOp.CREATE)) {
            taskbrowseBtn.setVisible(false);
            taskbrowseAction.setEnabled(false);
            taskbrowseAction.setVisible(false);
        }
    }

    private void taskmonitorCall(Project project) {
        TaskMonitor screen = screenBuilders.lookup(Task.class, this)
                .withScreenClass(TaskMonitor.class)
                .withOpenMode(OpenMode.NEW_TAB)
                .build();
        screen.setCurrProject(project);
        screen.show();
    }

    private void taskbrowseCall(Project project) {
        TaskBrowse screen = screenBuilders.lookup(Task.class, this)
                .withScreenClass(TaskBrowse.class)
                .withOpenMode(OpenMode.NEW_TAB)
                .build();
        screen.setCurrProject(project);
        screen.show();
    }

    private void taskrunmonitorCall(Project project) {
        TaskRunListProject screen = screenBuilders.lookup(TaskRun.class, this)
                .withScreenClass(TaskRunListProject.class)
                .withOpenMode(OpenMode.NEW_TAB)
                .build();
        screen.setCurrProject(project);
        screen.show();
    }

    private void displayProjectCall(Project project) {
        screenBuilders.editor(Project.class, this)
                .editEntity(project)
                .withScreenClass(ProjectDisplay.class)
                .build()
                .show();
    }

}