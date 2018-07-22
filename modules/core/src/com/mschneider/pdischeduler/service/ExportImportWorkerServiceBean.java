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
import com.mschneider.pdischeduler.entity.Worker;
import com.mschneider.pdischeduler.entity.WorkerType;
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

@Service(ExportImportWorkerService.NAME)
public class ExportImportWorkerServiceBean implements ExportImportWorkerService {

    private static final Logger logger = LoggerFactory.getLogger(ExportImportWorkerServiceBean.class);

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private UuidSource uuidSource;

    @Override
    @Transactional
    public String exportWorkersToCsv(Collection<Worker> workers) {

        StringWriter writer = new StringWriter();
        try {
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';').withHeader(
                    "name",
                    "description",
                    "timezone",
                    "url",
                    "userid",
                    "passwordEncr",
                    "workerType",
                    "pdiRootDir",
                    "pdiReposId",
                    "pdiReposUser",
                    "pdiReposPasswordEncr",
                    "dataRootDir",
                    "active"
            ));
            for (Worker worker : workers) {
                csvPrinter.printRecord(
                        worker.getName(),
                        worker.getDescription(),
                        worker.getTimezone(),
                        worker.getUrl(),
                        worker.getUserid(),
                        worker.getPasswordEncr(),
                        worker.getWorkerType(),
                        worker.getPdiRootDir(),
                        worker.getPdiReposId(),
                        worker.getPdiReposUser(),
                        worker.getPdiReposPasswordEncr(),
                        worker.getDataRootDir(),
                        worker.getActive()
                );
            }
            csvPrinter.flush();

        } catch (Exception e) {
            logger.error("exportWorkersToCsv failed", e);
        }

        return writer.toString();
    }

    @Override
    @Transactional
    public int importWorkersFromCsv(String content) {
        int importCount = 0;
        try {

            Reader in = new StringReader(content);

            Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(in);
            for (CSVRecord record : records) {
                logger.info("importWorkersFromCsv: workerName=" + record.get("name"));
                Worker worker = metadata.create(Worker.class);

                worker.setId(uuidSource.createUuid());
                worker.setName(getUniqueName(record.get("name")));
                worker.setDescription(record.get("description"));
                worker.setTimezone(record.get("timezone"));
                worker.setUrl(record.get("url"));
                worker.setUserid(record.get("userid"));
                worker.setPasswordEncr(record.get("passwordEncr"));
                worker.setWorkerType(WorkerType.fromStr(record.get("workerType")));
                worker.setPdiRootDir(record.get("pdiRootDir"));
                worker.setPdiReposId(record.get("pdiReposId"));
                worker.setPdiReposUser(record.get("pdiReposUser"));
                worker.setPdiReposPasswordEncr(record.get("pdiReposPasswordEncr"));
                worker.setDataRootDir(record.get("dataRootDir"));
                worker.setActive(Boolean.parseBoolean(record.get("active")));

                persistence.getEntityManager().persist(worker);
                importCount++;
            }

        } catch (Exception e) {
            logger.error("importWorkersFromCsv failed", e);
        }
        return importCount;
    }

    @Override
    @Transactional
    public boolean isExistent(String workerName) {
        TypedQuery<Worker> query = persistence.getEntityManager().createQuery(
                "select w from pdischeduler$Worker w where w.name = ?1",
                Worker.class);
        query.setParameter(1, workerName);
        List<Worker> list = query.getResultList();
        return list.size() > 0;
    }

    @Override
    @Transactional
    public String getUniqueName(String workerName) {
        TypedQuery<Worker> query = persistence.getEntityManager().createQuery(
                "select w from pdischeduler$Worker w where w.name like CONCAT(?1, '%')",
                Worker.class);
        query.setParameter(1, workerName);
        List<Worker> list = query.getResultList();
        if (list.size() > 0) {
            // possible duplicates
            ArrayList<String> names = new ArrayList<>();
            for (Worker worker : list) {
                names.add(worker.getName());
            }
            if (names.contains(workerName)) {
                // find next unused number
                int i = 1;
                while (names.contains(workerName + " (" + i + ")")) {
                    i++;
                }
                return workerName + " (" + i + ")";
            }
        }
        return workerName;
    }

}