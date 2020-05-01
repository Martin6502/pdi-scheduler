package com.mschneider.pdischeduler.web.screens.project;

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
import com.mschneider.pdischeduler.service.ExportImportProjectService;
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
@UiController("pdischeduler$Project.browse")
@UiDescriptor("project-browse.xml")
@LookupComponent("projectsTable")
@LoadDataBeforeShow
public class ProjectBrowse extends StandardLookup<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectBrowse.class);

    @Inject
    private CollectionLoader<Project> projectsDl;
    @Inject
    private Table<Project> projectsTable;
    @Inject
    private MessageBundle messageBundle;
    @Inject
    private Metadata metadata;
    @Inject
    private UuidSource uuidSource;
    @Inject
    private ExportImportProjectService exportImportProjectService;
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


    @Subscribe
    protected void onInit(InitEvent event) {
        logger.debug("onInit started");
        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                if (projectsTable.getSelected().size() == 1) {
                    Project currProject = projectsTable.getSingleSelected();
                    if (currProject != null) {
                        CommitContext context = new CommitContext();
                        Project copy = metadata.getTools().copy(currProject);
                        copy.setId(uuidSource.createUuid());
                        copy.setName(exportImportProjectService.getUniqueName(currProject.getName()));
                        context.addInstanceToCommit(copy);
                        dataManager.commit(copy);
                        projectsDl.load();
                    }
                }
            }
        };
        projectsTable.addAction(copyAction);

        ItemTrackingAction exportCsvAction = new ItemTrackingAction("exportCsv") {
            @Override
            public void actionPerform(Component component) {
                exportCsv();
            }
        };
        projectsTable.addAction(exportCsvAction);

        importCsvUpload.addFileUploadSucceedListener(uploadSucceedEvent  -> importCsv());

    }

    private void exportCsv() {
        Set<Project> selected = projectsTable.getSelected();
        if (!selected.isEmpty()) {
            try {
                byte[] bom = new byte[]{(byte) 239, (byte) 187, (byte) 191};
                byte[] data = exportImportProjectService.exportProjectsToCsv(selected)
                        .getBytes(StandardCharsets.UTF_8);
                byte[] dataWithBom = new byte[bom.length + data.length];

                System.arraycopy(bom, 0, dataWithBom, 0, bom.length);
                System.arraycopy(data, 0, dataWithBom, bom.length, data.length);

                exportDisplay.show(new ByteArrayDataProvider(dataWithBom), "Projects_" + TimeZoneUtils.dateNowFileNameStr(), ExportFormat.CSV);
            } catch (Exception e) {
                notifications.create()
                        .withType(Notifications.NotificationType.ERROR)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(messageBundle.getMessage("exportFailed"))
                        .withDescription(e.getMessage())
                        .show();
                logger.error("Project export failed", e);
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
            int importedProjectsCount;
            if ("csv".equals(Files.getFileExtension(importCsvUpload.getFileName()))) {
                importedProjectsCount = exportImportProjectService.importProjectsFromCsv(content);
                notifications.create()
                        .withType(Notifications.NotificationType.HUMANIZED)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(importedProjectsCount + messageBundle.getMessage("importCount"))
                        .show();
            }
            projectsDl.load();
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