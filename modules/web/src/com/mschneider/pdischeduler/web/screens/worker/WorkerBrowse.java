package com.mschneider.pdischeduler.web.screens.worker;

import com.google.common.io.Files;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.model.CollectionLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.mschneider.pdischeduler.entity.Worker;
import com.mschneider.pdischeduler.service.ExportImportWorkerService;
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
@UiController("pdischeduler$Worker.browse")
@UiDescriptor("worker-browse.xml")
@LookupComponent("workersTable")
@LoadDataBeforeShow
public class WorkerBrowse extends StandardLookup<Worker> {

    private static final Logger logger = LoggerFactory.getLogger(WorkerBrowse.class);

    @Inject
    private CollectionLoader<Worker> workersDl;
    @Inject
    private Table<Worker> workersTable;
    @Inject
    private UiComponents uiComponents;
    @Inject
    private MessageBundle messageBundle;
    @Inject
    private Metadata metadata;
    @Inject
    private UuidSource uuidSource;
    @Inject
    private ExportImportWorkerService exportImportWorkerService;
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

        workersTable.addGeneratedColumn("carteStatus", entity -> {
            if (entity.getUrl() != null && entity.getActive()) {
                Link field = uiComponents.create(Link.NAME);
                field.setCaption("open Carte status");
                field.setUrl((entity).getUrl() + "/kettle/status/");
                field.setTarget("_blank");
                return field;
            }
            return null;
        });
        workersTable.getColumn("carteStatus").setCaption(messageBundle.getMessage("carteStatus"));

        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                if (workersTable.getSelected().size() == 1) {
                    Worker currWorker = workersTable.getSingleSelected();
                    if (currWorker != null) {
                        CommitContext context = new CommitContext();
                        Worker copy = metadata.getTools().copy(currWorker);
                        copy.setId(uuidSource.createUuid());
                        copy.setName(exportImportWorkerService.getUniqueName(currWorker.getName()));
                        context.addInstanceToCommit(copy);
                        dataManager.commit(copy);
                        workersDl.load();
                    }
                }
            }
        };
        workersTable.addAction(copyAction);

        ItemTrackingAction exportCsvAction = new ItemTrackingAction("exportCsv") {
            @Override
            public void actionPerform(Component component) {
                exportCsv();
            }
        };
        workersTable.addAction(exportCsvAction);

        importCsvUpload.addFileUploadSucceedListener(uploadSucceedEvent  -> importCsv());
        
    }

    private void exportCsv() {
        Set<Worker> selected = workersTable.getSelected();
        if (!selected.isEmpty()) {
            try {
                byte[] bom = new byte[]{(byte) 239, (byte) 187, (byte) 191};
                byte[] data = exportImportWorkerService.exportWorkersToCsv(selected)
                        .getBytes(StandardCharsets.UTF_8);
                byte[] dataWithBom = new byte[bom.length + data.length];

                System.arraycopy(bom, 0, dataWithBom, 0, bom.length);
                System.arraycopy(data, 0, dataWithBom, bom.length, data.length);

                exportDisplay.show(new ByteArrayDataProvider(dataWithBom), "Workers_" + TimeZoneUtils.dateNowFileNameStr(), ExportFormat.CSV);
            } catch (Exception e) {
                notifications.create()
                        .withType(Notifications.NotificationType.ERROR)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(messageBundle.getMessage("exportFailed"))
                        .withDescription(e.getMessage())
                        .show();
                logger.error("Worker export failed", e);
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
            int importedWorkersCount;
            if ("csv".equals(Files.getFileExtension(importCsvUpload.getFileName()))) {
                importedWorkersCount = exportImportWorkerService.importWorkersFromCsv(content);
                notifications.create()
                        .withType(Notifications.NotificationType.HUMANIZED)
                        .withContentMode(ContentMode.TEXT)
                        .withCaption(importedWorkersCount + messageBundle.getMessage("importCount"))
                        .show();
            }
            workersDl.load();
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