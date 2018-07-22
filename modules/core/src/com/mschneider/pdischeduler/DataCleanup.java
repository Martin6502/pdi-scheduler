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

package com.mschneider.pdischeduler;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
@Component(DataCleanup.NAME)
public class DataCleanup {

    public static final String NAME = "pdischeduler_DataCleanup";

    private static final Logger logger = LoggerFactory.getLogger(DataCleanup.class);

    @Inject
    private Persistence persistence;


    @Authenticated
    public void taskRunClean() {
        logger.info("taskRunClean");

        Transaction tx = persistence.createTransaction();
        try {
            Query query = persistence.getEntityManager().createNativeQuery(
                    "delete from pdischeduler_task_run where id in (\n" +
                            "  select tr.id \n" +
                            "  from pdischeduler_task_run tr\n" +
                            "  inner join pdischeduler_task t on t.id = tr.task_id\n" +
                            "  inner join pdischeduler_project p on p.id = t.project_id\n" +
                            "  where p.cleanup_after_days is not null \n" +
                            "  and p.cleanup_after_days > 0\n" +
                            "  and tr.start_time < current_date - make_interval(days => p.cleanup_after_days)\n" +
                            ")");
            query.executeUpdate();

            tx.commit();
        } catch (Exception se) {
            logger.error("taskRunClean: ", se);
        } finally {
            tx.end();
        }
    }

    @Authenticated
    public void messageClean() {
        logger.info("messageClean");
        Transaction tx = persistence.createTransaction();
        try {
            String cleanupAfterDays = AppContext.getProperty("cuba.email.cleanupAfterDays");
            if (cleanupAfterDays != null) {
                Query query1 = persistence.getEntityManager().createNativeQuery(
                        "delete from sys_sending_message where create_ts < current_date - make_interval(days => " + cleanupAfterDays + ")");
                query1.executeUpdate();

                Query query2 = persistence.getEntityManager().createNativeQuery(
                        "delete from sys_sending_attachment where create_ts < current_date - make_interval(days => " + cleanupAfterDays + ")");
                query2.executeUpdate();
            }
            tx.commit();
        } catch (Exception se) {
            logger.error("taskRunClean: ", se);
        } finally {
            tx.end();
        }
    }

}
