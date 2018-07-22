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

package com.mschneider.pdischeduler.tasks;

import com.haulmont.cuba.core.global.AppBeans;
import com.mschneider.pdischeduler.DataCleanup;
import com.mschneider.pdischeduler.TaskProcessing;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzReInitialize implements Job {

    private static final Logger logger = LoggerFactory.getLogger(QuartzReInitialize.class);

    public QuartzReInitialize() {
    }

    public void execute(JobExecutionContext context) {
        logger.info("ReInitialize Quartz");
        AppBeans.get(TaskProcessing.class).taskInitialize();

        logger.info("Daily DataCleanup");
        AppBeans.get(DataCleanup.class).taskRunClean();
        AppBeans.get(DataCleanup.class).messageClean();

    }

}
