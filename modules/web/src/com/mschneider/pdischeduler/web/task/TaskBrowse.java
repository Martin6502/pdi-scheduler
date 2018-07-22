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

package com.mschneider.pdischeduler.web.task;

import com.google.common.io.Files;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.mschneider.pdischeduler.service.ExportImportTaskService;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskBrowse extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(TaskBrowse.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private UuidSource uuidSource;

    @Inject
    private ExportImportTaskService exportImportTaskService;

    @Inject
    private ExportDisplay exportDisplay;

    @Inject
    private FileUploadField importCsvUpload;

    @Inject
    private FileUploadingAPI fileUploadingAPI;

    @Inject
    private Label headline;

    @Inject
    private Table<Task> tasksTable;

    @Named("tasksTable.edit")
    private EditAction tasksTableEdit;

    @Named("tasksTable.create")
    private CreateAction tasksTableCreate;

    @Inject
    private Datasource<Project> currProjectDs;

    @Inject
    private CollectionDatasource<Task, UUID> currProjectTasksDs;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);

        super.setCaption("Task Admin - " + currProject.getName());
        headline.setValue("Project: " + currProject.getName()
                + ", Timezone: " + currProject.getTimezone() + "");
        tasksTableEdit.setWindowParams(ParamsMap.of("currProject", currProject));
        tasksTableCreate.setWindowParams(ParamsMap.of("currProject", currProject));

        currProjectDs.setItem(currProject);

        ItemTrackingAction copyAction = new ItemTrackingAction("copy") {
            @Override
            public void actionPerform(Component component) {
                Task currTask = tasksTable.getSingleSelected();
                if (currTask != null) {
                    CommitContext context = new CommitContext();
                    Task copy = metadata.getTools().copy(currTask);
                    copy.setId(uuidSource.createUuid());
                    copy.setName(exportImportTaskService.getUniqueName(currProject, currTask.getName()));
                    context.addInstanceToCommit(copy);
                    dataManager.commit(copy);
                    currProjectTasksDs.refresh();
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

        importCsvUpload.addFileUploadSucceedListener(event -> importCsv());

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
                showNotification(getMessage("exportFailed"), e.getMessage(), NotificationType.ERROR);
                logger.error("Task export failed", e);
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
            int importedTasksCount;
            if ("csv".equals(Files.getFileExtension(importCsvUpload.getFileName()))) {
                importedTasksCount = exportImportTaskService.importTasksFromCsv(currProject, content);
                showNotification(importedTasksCount + " tasks imported", NotificationType.HUMANIZED);
            }
            currProjectTasksDs.refresh();
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