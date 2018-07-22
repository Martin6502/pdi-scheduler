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

package com.mschneider.pdischeduler.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.AppContext;
import com.mschneider.pdischeduler.TaskProcessing;
import com.mschneider.pdischeduler.utils.TaskScheduler;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Registers an entity listener for the User entity on application startup.
 */
@Component("pdischeduler_AppLifecycle")
public class AppLifecycle implements AppContext.Listener {

    private static final Logger logger = LoggerFactory.getLogger(AppLifecycle.class);

    public AppLifecycle() {
        AppContext.addListener(this);
    }

    @Override
    public void applicationStarted() {
        logger.info("PDI_Scheduler application started");
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            logger.info("Quartz scheduler started");

            // setup all quartz jobs
            AppBeans.get(TaskProcessing.class).taskInitialize();

            // setup a special quartz job for reinitialization every day
            // as failsafe approach if something goes wrong
            TaskScheduler.scheduleReInitialize();

        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @Override
    public void applicationStopped() {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.shutdown();
            logger.info("Quartz scheduler stopped");

        } catch (Exception se) {
            se.printStackTrace();
        }
        logger.info("PDI_Scheduler application stopped");
    }

}
