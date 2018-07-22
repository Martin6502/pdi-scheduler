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

package com.mschneider.pdischeduler.utils;

import com.mschneider.pdischeduler.entity.*;
import com.mschneider.pdischeduler.tasks.QuartzReInitialize;
import com.mschneider.pdischeduler.tasks.QuartzTaskExec;
import com.mschneider.pdischeduler.tasks.QuartzTaskStatus;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class TaskScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);


    public static void scheduleReInitialize() {
        logger.info("Start scheduleReInitialize");

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // Create Quartz job daily reInitialize
            // Normally there should not be any problems with this
            // but this is a failsafe approach
            JobDetail job = newJob(QuartzReInitialize.class)
                    .withIdentity("QuartzReInitialize", "init")
                    .build();
            Trigger trigger = newTrigger()
                    .withIdentity("QuartzReInitialize", "init")
                    .withSchedule(cronSchedule("0 55 23 * * ?").inTimeZone(TimeZone.getDefault()))
                    .build();
            scheduler.scheduleJob(job, trigger);

        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void scheduleTask(Task task, boolean activate) {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            if (task.getActive() && activate) {
                // activate task
                logger.debug("scheduleTask add job: " + task.getProject().getName() + " - " + task.getName());
                JobDetail job = newJob(QuartzTaskExec.class)
                        .withIdentity(task.getId().toString(), "exec")
                        .storeDurably()
                        .build();
                scheduler.addJob(job, true);

                // check for cron type task
                if (task.getTriggerType() == TaskTriggerType.cron) {
                    // create new trigger
                    logger.debug("scheduleTask add trigger: " + task.getProject().getName() + " - " + task.getName());
                    scheduler.unscheduleJob(TriggerKey.triggerKey(task.getId().toString(), TaskRunStartTrigger.cron.name()));

                    Trigger cronTrigger = newTrigger()
                            .withIdentity(task.getId().toString(), TaskRunStartTrigger.cron.name())
                            .startNow()
                            .withSchedule(cronSchedule(task.getCronSpec()).inTimeZone(TimeZone.getTimeZone(task.getProject().getTimezone())))
                            .forJob(JobKey.jobKey(task.getId().toString(), "exec"))
                            .build();
                    scheduler.scheduleJob(cronTrigger);
                } else {
                    // remove probably existing trigger
                    scheduler.unscheduleJob(TriggerKey.triggerKey(task.getId().toString(), TaskRunStartTrigger.cron.name()));
                }
            } else {
                // delete quartz job and trigger
                logger.debug("scheduleTask delete: " + task.getProject().getName() + " - " + task.getName());
                scheduler.deleteJob(JobKey.jobKey(task.getId().toString(), "exec"));
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void scheduleTaskNow(Task task) {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            logger.debug("scheduleTaskNow: " + task.getProject().getName() + " - " + task.getName());

            // now start this task by creating a one time trigger
            Trigger nowTrigger = newTrigger()
                    .withIdentity(task.getId().toString(), "prevTask")
                    .startNow()
                    .forJob(JobKey.jobKey(task.getId().toString(), "exec"))
                    .build();
            scheduler.scheduleJob(nowTrigger);

        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void scheduleStatus(TaskRun taskRun) {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            if (taskRun.getStatus() == TaskRunStatus.started) {

                logger.debug("scheduleStatus: " + taskRun.getTask().getProject().getName() + " - " + taskRun.getTask().getName());

                // Create Quartz job for this status task
                JobDetail job = newJob(QuartzTaskStatus.class)
                        .withIdentity(taskRun.getId().toString(), "status")
                        .build();
                Trigger trigger = newTrigger()
                        .withIdentity(taskRun.getId().toString(), "status")
                        // .startNow()
                        .startAt(futureDate(5, DateBuilder.IntervalUnit.SECOND))
                        .withSchedule(simpleSchedule()
                                .withIntervalInSeconds(TaskRun.TASKRUN_REFRESH_SEC)
                                .repeatForever())
                        .build();
                scheduler.scheduleJob(job, trigger);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void unScheduleStatus(TaskRun taskRun) {
        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.deleteJob(JobKey.jobKey(taskRun.getId().toString(), "status"));
        } catch (Exception se) {
            se.printStackTrace();
        }
    }


}