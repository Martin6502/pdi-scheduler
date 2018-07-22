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

package com.mschneider.pdischeduler.web.project;

import com.google.common.io.Files;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProjectBrowse extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(ProjectBrowse.class);

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private UuidSource uuidSource;

    @Inject
    private ExportImportProjectService exportImportProjectService;

    @Inject
    private ExportDisplay exportDisplay;

    @Inject
    private FileUploadField importCsvUpload;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private CollectionDatasource<Project, UUID> projectsDs;

    @Inject
    private Table<Project> projectsTable;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);

        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                Project currProject = projectsTable.getSingleSelected();

                if (currProject != null) {
                    CommitContext context = new CommitContext();

                    Project copy = metadata.getTools().copy(currProject);
                    copy.setId(uuidSource.createUuid());
                    copy.setName(exportImportProjectService.getUniqueName(currProject.getName()));

                    context.addInstanceToCommit(copy);
                    dataManager.commit(copy);

                    projectsDs.refresh();
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

        importCsvUpload.addFileUploadSucceedListener(event -> importCsv());
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
                showNotification(getMessage("exportFailed"), e.getMessage(), NotificationType.ERROR);
                logger.error("Project export failed", e);
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
            int importedProjectsCount;
            if ("csv".equals(Files.getFileExtension(importCsvUpload.getFileName()))) {
                importedProjectsCount = exportImportProjectService.importProjectsFromCsv(content);
                showNotification(importedProjectsCount + " projects imported", NotificationType.HUMANIZED);
            }
            projectsDs.refresh();
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