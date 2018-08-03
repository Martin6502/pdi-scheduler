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
import com.mschneider.pdischeduler.entity.Worker;
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
import java.util.List;

@Service(ExportImportProjectService.NAME)
public class ExportImportProjectServiceBean implements ExportImportProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportProjectServiceBean.class);

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private UuidSource uuidSource;

    @Override
    @Transactional
    public String exportProjectsToCsv(Collection<Project> projects) {

        StringWriter writer = new StringWriter();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';').withHeader(
                    "name",
                    "description",
                    "groupAccess",
                    "workerName",
                    "timezone",
                    "mailReceiverInfo",
                    "mailReceiverError",
                    "dataSubDir",
                    "pdiSubDir",
                    "pdiParameter",
                    "active"
            ));
            for (Project project : projects) {
                csvPrinter.printRecord(
                        project.getName(),
                        project.getDescription(),
                        project.getGroupAccess(),
                        project.getWorker().getName(),
                        project.getTimezone(),
                        project.getMailReceiverInfo(),
                        project.getMailReceiverError(),
                        project.getDataSubDir(),
                        project.getPdiSubDir(),
                        project.getPdiParameter(),
                        project.getActive()
                );
            }
            csvPrinter.flush();

        } catch (Exception e) {
            logger.error("exportProjectsToCsv failed", e);
        }

        return writer.toString();
    }

    @Override
    @Transactional
    public int importProjectsFromCsv(String content) {
        int importCount = 0;
        try {

            Reader in = new StringReader(content);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(in);
            for (CSVRecord record : records) {
                logger.info("importProjectsFromCsv: projectName=" + record.get("name"));
                Project project = metadata.create(Project.class);

                project.setId(uuidSource.createUuid());
                project.setName(getUniqueName(record.get("name")));
                project.setDescription(record.get("description"));
                project.setGroupAccess(record.get("groupAccess"));
                project.setTimezone(record.get("timezone"));
                project.setMailReceiverInfo(record.get("mailReceiverInfo"));
                project.setMailReceiverError(record.get("mailReceiverError"));
                project.setDataSubDir(record.get("dataSubDir"));
                project.setPdiSubDir(record.get("pdiSubDir"));
                project.setPdiParameter(record.get("pdiParameter"));
                project.setActive(Boolean.parseBoolean(record.get("active")));

                // handle lookup of workerName
                TypedQuery<Worker> query = persistence.getEntityManager().createQuery(
                        "select w from pdischeduler$Worker w where w.name = ?1",
                        Worker.class);
                query.setParameter(1, record.get("workerName"));
                Worker worker = query.getFirstResult();
                project.setWorker(worker);

                persistence.getEntityManager().persist(project);
                importCount++;
            }

        } catch (Exception e) {
            logger.error("importProjectsFromCsv failed", e);
        }
        return importCount;
    }

    @Override
    @Transactional
    public boolean isExistent(String projectName) {
        TypedQuery<Project> query = persistence.getEntityManager().createQuery(
                "select p from pdischeduler$Project p where p.name = ?1",
                Project.class);
        query.setParameter(1, projectName);
        List<Project> list = query.getResultList();
        return list.size() > 0;
    }

    @Override
    @Transactional
    public String getUniqueName(String projectName) {
        TypedQuery<Project> query = persistence.getEntityManager().createQuery(
                "select p from pdischeduler$Project p where p.name like CONCAT(?1, '%')",
                Project.class);
        query.setParameter(1, projectName);
        List<Project> list = query.getResultList();
        if (list.size() > 0) {
            // possible duplicates
            ArrayList<String> names = new ArrayList<>();
            for (Project project : list) {
                names.add(project.getName());
            }
            if (names.contains(projectName)) {
                // find next unused number
                int i = 1;
                while (names.contains(projectName + " (" + i + ")")) {
                    i++;
                }
                return projectName + " (" + i + ")";
            }
        }
        return projectName;
    }

}