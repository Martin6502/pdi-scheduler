package com.mschneider.pdischeduler.web.screens.task;

import com.google.common.io.Files;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.service.ExportImportTaskService;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@SuppressWarnings("NullableProblems")
@UiController("pdischeduler$Task.browse")
@UiDescriptor("task-browse.xml")
@LookupComponent("tasksTable")
@LoadDataBeforeShow
public class TaskBrowse extends StandardLookup<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskBrowse.class);

    @Inject
    private CollectionLoader<Task> tasksDl;
    @Inject
    private Table<Task> tasksTable;
    @Inject
    private MessageBundle messageBundle;
    @Inject
    private Metadata metadata;
    @Inject
    private UuidSource uuidSource;
    @Inject
    private ExportImportTaskService exportImportTaskService;
    @Inject
    private DataManager dataManager;
    @Inject
    private ExportDisplay exportDisplay;
    @Inject
    private FileUploadField importCsvUpload;
    @Inject
    private FileUploadingAPI fileUploadingAPI;
    @Inject
    private Notifications notifications;
    @Inject
    private Label<String> headline;


    // screen parameter
    private Project currProject;

    public void setCurrProject(Project project) {
        currProject = project;
    }


    @Install(to = "tasksTable.create", subject = "screenConfigurer")
    protected void tasksTableCreateScreenConfigurer(Screen screen) {
        ((TaskEdit) screen).setCurrProject(currProject);
    }

    @Install(to = "tasksTable.edit", subject = "screenConfigurer")
    protected void tasksTableEditScreenConfigurer(Screen screen) {
        ((TaskEdit) screen).setCurrProject(currProject);
    }

    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        logger.debug("onBeforeShow started");
        if (currProject == null)
            throw new IllegalStateException("project parameter is null");

        getWindow().getFrame().setCaption("Task Admin - " + currProject.getName());
        headline.setValue("Project: " + currProject.getName()
                + ", Timezone: " + currProject.getTimezone() + "");
        tasksDl.setParameter("currProjectId", currProject.getId());
        tasksDl.load();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        // super.set
        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                if (tasksTable.getSelected().size() == 1) {
                    Task currTask = tasksTable.getSingleSelected();
                    if (currTask != null) {
                        CommitContext context = new CommitContext();
                        Task copy = metadata.getTools().copy(currTask);
                        copy.setId(uuidSource.createUuid());
                        copy.setName(exportImportTaskService.getUniqueName(currProject, currTask.getName()));
                        context.addInstanceToCommit(copy);
                        dataManager.commit(copy);
                        tasksDl.load();
                    }
                }
            }
        };
        tasksTable.addAction(copyAction);

        ItemTrackingAction exportCsvAction = new ItemTrackingAction("exportCsv") {
            @Override
            public void actionPerform(Component component) {
                exportCsv();
            }
        };
        tasksTable.addAction(exportCsvAction);

        importCsvUpload.addFileUploadSucceedListener(uploadSucceedEvent  -> importCsv());

    }

    private void exportCsv() {
        Set<Task> selected = tasksTable.getSelected();
        if (!selected.isEmpty()) {
            try {
                byte[] bom = new byte[]{(byte) 239, (byte) 187, (byte) 191};
                byte[] data = exportImportTaskService.exportTasksToCsv(selected)
                        .getBytes(StandardCharsets.UTF_8);
                byte[] dataWithBom = new byte[bom.length + data.length];

                System.arraycopy(bom, 0, dataWithBom, 0, bom.length);
                System.arraycopy(data, 0, dataWithBom, bom.length, data.length);

                exportDisplay.show(new ByteArrayDataProvider(dataWithBom), "Tasks_" + TimeZoneUtils.dateNowFileNameStr(), ExportFormat.CSV);
            } catch (Exception e) {
                notifications.create()
                        .withType(Notifications.NotificationType.ERROR)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(messageBundle.getMessage("exportFailed"))
                        .withDescription(e.getMessage())
                        .show();
                logger.error("Task export failed", e);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void importCsv() {
        File file = fileUploadingAPI.getFile(importCsvUpload.getFileId());
        if (file == null) {
            String errorMsg = String.format("Entities import upload error. File with id %s not found", importCsvUpload.getFileId());
            throw new RuntimeException(errorMsg);
        }

        String content;
        try (InputStream is = new BOMInputStream(new FileInputStream(file))) {
            StringWriter sw = new StringWriter();
            IOUtils.copy(is, sw, StandardCharsets.UTF_8);
            content = sw.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file", e);
        }

        try {
            int importedTasksCount;
            if ("csv".equals(Files.getFileExtension(importCsvUpload.getFileName()))) {
                importedTasksCount = exportImportTaskService.importTasksFromCsv(currProject, content);
                notifications.create()
                        .withType(Notifications.NotificationType.HUMANIZED)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(importedTasksCount + messageBundle.getMessage("importCount"))
                        .show();
            }
            tasksDl.load();
        } catch (Exception e) {
            notifications.create()
                    .withType(Notifications.NotificationType.ERROR)
                    .withContentMode(ContentMode.TEXT)
                    .withCaption(messageBundle.getMessage("importError"))
                    .withDescription(e.getMessage())
                    .show();
        }

        try {
            fileUploadingAPI.deleteFile(importCsvUpload.getFileId());
        } catch (FileStorageException e) {
            logger.error("Unable to delete temp file", e);
        }
    }

}