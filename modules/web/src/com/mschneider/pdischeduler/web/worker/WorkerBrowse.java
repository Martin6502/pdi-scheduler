/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.web.worker;

import com.google.common.io.Files;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorkerBrowse extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(WorkerBrowse.class);

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private UuidSource uuidSource;

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private ExportImportWorkerService exportImportWorkerService;

    @Inject
    private ExportDisplay exportDisplay;

    @Inject
    private FileUploadField importCsvUpload;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private Table<Worker> workersTable;

    @Inject
    private CollectionDatasource<Worker, UUID> workersDs;

    @SuppressWarnings({"Convert2Lambda", "unchecked"})
    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        workersTable.addGeneratedColumn("Carte Status Page", new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Entity entity) {
                Worker worker = (Worker) entity;
                if (worker != null && worker.getUrl() != null && worker.getActive()) {
                    Link field = (Link) componentsFactory.createComponent(Link.NAME);
                    field.setCaption("open Carte status");
                    field.setUrl(worker.getUrl() + "/kettle/status/");
                    field.setTarget("_blank");
                    return field;
                }
                return null;
            }
        });

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
                        workersDs.refresh();
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

        importCsvUpload.addFileUploadSucceedListener(event -> importCsv());
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
                showNotification(getMessage("exportFailed"), e.getMessage(), NotificationType.ERROR);
                logger.error("Worker export failed", e);
            }
        }
    }

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
                showNotification(importedWorkersCount + " worker imported", NotificationType.HUMANIZED);
            }
            workersDs.refresh();
        } catch (Exception e) {
            showNotification(formatMessage("importError", e.getMessage()), NotificationType.ERROR);
        }

        try {
            fileUploadingAPI.deleteFile(importCsvUpload.getFileId());
        } catch (FileStorageException e) {
            logger.error("Unable to delete temp file", e);
        }
    }

}