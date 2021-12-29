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

package com.mschneider.pdischeduler.service;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UuidSource;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskTriggerType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service(ExportImportTaskService.NAME)
public class ExportImportTaskServiceBean implements ExportImportTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportTaskServiceBean.class);

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private UuidSource uuidSource;

    @Override
    @Transactional
    public String exportTasksToCsv(Collection<Task> tasks) {

        StringWriter writer = new StringWriter();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';').withHeader(
                    "projectName",
                    "name",
                    "description",
                    "sortKey",
                    "pdiFile",
                    "pdiParameter",
                    "logLevel",
                    "timeoutSec",
                    "triggerType",
                    "cronSpec",
                    "cronExclDates",
                    "prevTaskName",
                    "active",
                    "wsTriggerName",
                    "wsTriggerSecretKey",
                    "wsTriggerRestartTime"
            ));
            for (Task task : tasks) {
                csvPrinter.printRecord(
                        task.getProject().getName(),
                        task.getName(),
                        task.getDescription(),
                        task.getSortKey(),
                        task.getPdiFile(),
                        task.getPdiParameter(),
                        task.getLogLevel(),
                        task.getTimeoutSec(),
                        task.getTriggerType(),
                        task.getCronSpec(),
                        task.getCronExclDates(),
                        task.getPrevTask() != null ? task.getPrevTask().getName() : null,
                        task.getActive(),
                        task.getWsTriggerName(),
                        task.getWsTriggerSecretKey(),
                        task.getWsTriggerRestartTime()
                );
            }
            csvPrinter.flush();

        } catch (Exception e) {
            logger.error("exportTasksToCsv failed", e);
        }

        return writer.toString();
    }

    @Override
    @Transactional
    public int importTasksFromCsv(Project project, String content) {
        int importCount = 0;
        try {
            final Reader in = new StringReader(content);
            Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(in);
            HashMap<String, String> mapNameToRealName = new HashMap<>();
            HashMap<String, String> savePrevNameSettings = new HashMap<>();
            HashMap<String, Task> saveTasks = new HashMap<>();
            try {
                for (CSVRecord record : records) {
                    logger.info("importTasksFromCsv: project=" + project.getName() + ", taskName=" + record.get("name"));
                    Task task = metadata.create(Task.class);

                    task.setProject(project);
                    task.setId(uuidSource.createUuid());

                    String name = record.get("name");
                    String newName = getUniqueName(project, name);
                    mapNameToRealName.put(name, newName);
                    task.setName(newName);

                    task.setDescription(record.get("description"));
                    task.setPdiFile(record.get("pdiFile"));
                    task.setLogLevel(record.get("logLevel"));
                    task.setPdiParameter(record.get("pdiParameter"));
                    task.setTriggerType(TaskTriggerType.fromStr(record.get("triggerType")));
                    task.setCronSpec(record.get("cronSpec"));
                    task.setCronExclDates(record.get("cronExclDates"));
                    task.setActive(Boolean.parseBoolean(record.get("active")));

                    try {
                        int i = Integer.parseInt(record.get("sortKey"));
                        task.setSortKey(i);
                    } catch (NumberFormatException e) {
                        logger.debug("importTasksFromCsv: sortKey not a number");
                    }

                    try {
                        int i = Integer.parseInt(record.get("timeoutSec"));
                        task.setTimeoutSec(i);
                    } catch (NumberFormatException e) {
                        logger.debug("importTasksFromCsv: timeoutSec not a number");
                    }

                    task.setWsTriggerName(record.get("wsTriggerName"));
                    task.setWsTriggerSecretKey(record.get("wsTriggerSecretKey"));

                    try {
                        int i = Integer.parseInt(record.get("wsTriggerRestartTime"));
                        task.setWsTriggerRestartTime(i);
                    } catch (NumberFormatException e) {
                        logger.debug("importTasksFromCsv: wsTriggerRestartTime not a number");
                    }

                    // save entries with prevTaskName for second pass (don't know about sequence)
                    if (record.get("prevTaskName") != null &&
                            (task.getTriggerType() == TaskTriggerType.prevTaskAll
                                    || task.getTriggerType() == TaskTriggerType.prevTaskOk
                                    || task.getTriggerType() == TaskTriggerType.prevTaskErr)) {

                        savePrevNameSettings.put(newName, record.get("prevTaskName"));

                    }

                    persistence.getEntityManager().persist(task);
                    saveTasks.put(newName, task);
                    importCount++;
                }

                // after importing the new tasks, set corrected prevTask entries
                for (HashMap.Entry<String, String> entry : savePrevNameSettings.entrySet()) {
                    String taskName = entry.getKey();
                    String taskPrevName = entry.getValue();
                    Task currTask = saveTasks.get(taskName);

                    if (mapNameToRealName.containsKey(taskPrevName)) {
                        taskPrevName = mapNameToRealName.get(taskPrevName);
                    }

                    Task currTaskPrevTask;
                    if (saveTasks.containsKey(taskPrevName)) {
                        currTaskPrevTask = saveTasks.get(taskPrevName);
                    } else {
                        currTaskPrevTask = getByName(project, taskPrevName);
                    }

                    if (currTaskPrevTask != null) {
                        // ok, update task with this prevTask setting
                        currTask.setPrevTask(currTaskPrevTask);
                        persistence.getEntityManager().persist(currTask);
                    }

                }

            } finally {
                in.close();
            }

        } catch (Exception e) {
            logger.error("importTasksFromCsv failed", e);
        }
        return importCount;
    }

    @Override
    @Transactional
    public boolean isExistent(Project project, String taskName) {
        TypedQuery<Task> query = persistence.getEntityManager().createQuery(
                "select t from pdischeduler$Task t where t.project.id = ?1 and t.name = ?2",
                Task.class);
        query.setParameter(1, project.getId());
        query.setParameter(2, taskName);
        List<Task> list = query.getResultList();
        return list.size() > 0;
    }

    @Override
    @Transactional
    public Task getByName(Project project, String taskName) {
        TypedQuery<Task> query = persistence.getEntityManager().createQuery(
                "select t from pdischeduler$Task t where t.project.id = ?1 and t.name = ?2",
                Task.class);
        query.setParameter(1, project.getId());
        query.setParameter(2, taskName);
        List<Task> list = query.getResultList();
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public String getUniqueName(Project project, String taskName) {
        TypedQuery<Task> query = persistence.getEntityManager().createQuery(
                "select t from pdischeduler$Task t where t.project.id = ?1 and t.name like CONCAT(?2, '%')",
                Task.class);
        query.setParameter(1, project.getId());
        query.setParameter(2, taskName);
        List<Task> list = query.getResultList();
        if (list.size() > 0) {
            // possible duplicates
            ArrayList<String> names = new ArrayList<>();
            for (Task task : list) {
                names.add(task.getName());
            }
            if (names.contains(taskName)) {
                // find next unused number
                int i = 1;
                while (names.contains(taskName + " (" + i + ")")) {
                    i++;
                }
                return taskName + " (" + i + ")";
            }
        }
        return taskName;
    }

}