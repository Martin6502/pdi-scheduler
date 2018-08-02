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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.EmailInfo;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.app.Authenticated;
import com.mschneider.pdischeduler.entity.*;
import com.mschneider.pdischeduler.utils.CarteCommand;
import com.mschneider.pdischeduler.utils.DateExclude;
import com.mschneider.pdischeduler.utils.TaskScheduler;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Component(TaskProcessing.NAME)
public class TaskProcessing {

    public static final String NAME = "pdischeduler_TaskProcessing";

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessing.class);

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private EmailerAPI emailerAPI;

    @Authenticated
    public void taskExec(String taskUUID, TaskRunStartTrigger triggerType) {
        logger.info("taskExec: taskUUID = " + taskUUID + ", triggerType = " + triggerType);

        Transaction tx = persistence.createTransaction();
        try {
            Task task = persistence.getEntityManager().find(Task.class, UUID.fromString(taskUUID));
            if (task != null
                    && task.getActive()
                    && task.getProject().getActive()
                    && task.getProject().getWorker().getActive()
                    && (!DateExclude.checkExcluded(TimeZoneUtils.dateNow(task.getProject().getTimezone()), task.getCronExclDates())
                    || triggerType == TaskRunStartTrigger.manualSingle || triggerType == TaskRunStartTrigger.manualSeq)) {

                Date currTimeUtc = TimeZoneUtils.dateNowUtc();
                TaskRun lastTaskRun = findLastTaskRun(persistence.getEntityManager(), task);
                if (lastTaskRun == null
                        || lastTaskRun.getStatus() != TaskRunStatus.started
                        || (currTimeUtc.getTime() - lastTaskRun.getStartTime().getTime()) > task.getTimeoutSec() * 1000 ) {

                    logger.info("taskExec: start project=" + task.getProject().getName() + ", task=" + task.getName());

                    String user = task.getProject().getWorker().getUserid();
                    String pass = task.getProject().getWorker().getPassword();
                    String baseUrl = task.getProject().getWorker().getUrl();
                    String jobDir = CarteCommand.createFullPath(
                            task.getProject().getWorker().getPdiRootDir(),
                            task.getProject().getPdiSubDir());
                    String jobFullPath = CarteCommand.createFullPath(
                            jobDir,
                            task.getPdiFile());
                    String logLevel = task.getLogLevel();
                    String dataDir = CarteCommand.createFullPath(
                            task.getProject().getWorker().getDataRootDir(),
                            task.getProject().getPdiSubDir());
                    String reposId = task.getProject().getWorker().getPdiReposId();
                    String reposUser = task.getProject().getWorker().getPdiReposUser();
                    String reposPassword = task.getProject().getWorker().getPdiReposPassword();

                    String jobParam = task.getProject().getPdiParameter();
                    String jobParam2 = task.getPdiParameter();
                    if (jobParam != null) {
                        if (jobParam2 != null) {
                            jobParam = jobParam + "&" + jobParam2;
                        }
                    } else {
                        jobParam = jobParam2;
                    }

                    String workerCode = null;
                    TaskRunStatus status;
                    Date stopTime = null;
                    Integer durationSec = null;
                    String logText = null;
                    String resultCode = null;

                    HashMap<String, String> map;

                    if (task.getProject().getWorker().getWorkerType() == WorkerType.PdiFile) {
                        map = CarteCommand.jobExec(user, pass, baseUrl, jobFullPath, logLevel, jobDir, dataDir, jobParam);
                    } else {
                        map = CarteCommand.jobExecRepos(user, pass, baseUrl, reposId, reposUser, reposPassword, jobFullPath, logLevel, dataDir, jobParam);
                    }
                    logger.debug("taskExec: jobFullPath = " + jobFullPath + ", result = " + map.get("result"));
                    if ("OK".equals(map.get("result")) && "OK".equals(map.get("webresult_result"))) {
                        workerCode = map.get("webresult_id");
                        status = TaskRunStatus.started;
                    } else {
                        status = TaskRunStatus.error;
                        stopTime = currTimeUtc;
                        durationSec = 0;
                        logText = "result=" + map.get("result") + "\n"
                                + "webresult=" + map.get("webresult_result") + "\n"
                                + "workerBaseUrl=" + baseUrl + "\n"
                                + "pdiJob=" + jobFullPath;
                        resultCode = "FATAL";
                    }

                    // create new TaskRun entity for this Task if there is no active one pending
                    TaskRun taskRun = metadata.create(TaskRun.class);
                    taskRun.setTask(task);
                    taskRun.setStatus(status);
                    taskRun.setStartTrigger(triggerType);
                    taskRun.setStartTime(currTimeUtc);
                    taskRun.setStopTime(stopTime);
                    taskRun.setDurationSec(durationSec);
                    taskRun.setWorkerCode(workerCode);
                    taskRun.setLogText(logText);
                    taskRun.setResultCode(resultCode);
                    taskRun.setResultHTML(null);
                    persistence.getEntityManager().persist(taskRun);

                    // if task started, create monitor job for updating status
                    if (status == TaskRunStatus.started) {
                        TaskScheduler.scheduleStatus(taskRun);
                    }

                    if (status == TaskRunStatus.error
                            && taskRun.getTask().getProject().getMailReceiverError() != null) {
                        sendErrorNotification(taskRun);
                    }

                } else {
                    logger.info("taskExec: skip due to still active project=" + task.getProject().getName() + ", task=" + task.getName());
                }
            }
            tx.commit();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            tx.end();
        }
    }

    @Authenticated
    @SuppressWarnings("ConstantConditions")
    public void taskStatus(String taskRunUUID) {
        Transaction tx = persistence.createTransaction();
        try {
            TaskRun taskRun = persistence.getEntityManager().find(TaskRun.class, UUID.fromString(taskRunUUID));
            if (taskRun != null) {
                logger.debug("taskStatus: " + taskRun.getTask().getProject().getName() + " - " + taskRun.getTask().getName());

                String user = taskRun.getTask().getProject().getWorker().getUserid();
                String pass = taskRun.getTask().getProject().getWorker().getPassword();
                String baseUrl = taskRun.getTask().getProject().getWorker().getUrl();
                String jobFullPath = CarteCommand.createFullPath(
                        taskRun.getTask().getProject().getWorker().getPdiRootDir(),
                        taskRun.getTask().getProject().getPdiSubDir(),
                        taskRun.getTask().getPdiFile());
                String jobName = CarteCommand.getJobName(jobFullPath);
                String workerCode = taskRun.getWorkerCode();

                HashMap<String, String> map;
                map = CarteCommand.jobStatus(user, pass, baseUrl, jobName, workerCode);
                // CarteCommand.printMap(map);

                TaskRunStatus status = taskRun.getStatus();
                String logText = null;
                long timeoutSec = taskRun.getTask().getTimeoutSec();

                ZonedDateTime startZDT = TimeZoneUtils.zonedDateTimeFromDateWithTimezone(taskRun.getStartTime(), "UTC");
                ZonedDateTime currZDT = ZonedDateTime.now(ZoneId.of("UTC"));
                long currExecSec = currZDT.toEpochSecond() - startZDT.toEpochSecond();
                if (currExecSec < 0) currExecSec = 0;

                boolean deleteStatusJob = false;
                boolean taskFinal = false;
                String statusDesc = map.get("jobstatus_status_desc");
                String pdiLog = map.get("jobstatus_logging_string");
                ZonedDateTime logStartZDT = scanDate(map.get("jobstatus_log_date"), taskRun.getTask().getProject().getWorker().getTimezone());
                String resultCode = null;

                if ("OK".equals(map.get("result"))) {
                    if ("Running".equals(statusDesc)) {
                        // check for timeout
                        if (currExecSec >= timeoutSec + TaskRun.TASKRUN_REFRESH_SEC) {
                            // should not occur but Carte did not respond to stop command
                            status = TaskRunStatus.timeout;
                            taskFinal = true;
                            logText = "ERROR: Carte Stop did not work";
                            logger.error("timeout problem" + taskRun.getTask().getProject().getName() + " - " + taskRun.getTask().getName());
                            deleteStatusJob = true;
                        } else if (currExecSec >= timeoutSec) {
                            // timeout reached, send stop command to Carte
                            HashMap<String, String> map2;
                            map2 = CarteCommand.jobStop(user, pass, baseUrl, jobName, workerCode);
                            logger.debug("marked for timeout" + taskRun.getTask().getProject().getName() + " - " + taskRun.getTask().getName() + " : " + map2.get("result"));
                        }

                    } else if ("Finished".equals(statusDesc)) {
                        // successful finished
                        status = TaskRunStatus.finished;
                        taskFinal = true;
                        logText = "statusDesc=" + statusDesc + "\n" + pdiLog;
                        deleteStatusJob = true;
                        resultCode = "INFO";

                    } else if ("Stopped".equals(statusDesc)) {
                        // after stop request
                        status = TaskRunStatus.timeout;
                        taskFinal = true;
                        logText = "statusDesc=" + statusDesc + "\n" + pdiLog;
                        deleteStatusJob = true;
                        resultCode = "FATAL";

                    } else {
                        // final error
                        status = TaskRunStatus.error;
                        taskFinal = true;
                        logText = "statusDesc=" + statusDesc + "\n" + pdiLog;
                        deleteStatusJob = true;
                        resultCode = "FATAL";

                    }

                } else {
                    logText = "result=" + map.get("result") + "\n"
                            + "workerBaseUrl=" + baseUrl + "\n"
                            + "pdiJob=" + jobFullPath
                    ;
                }

                /* scan logText for included special infos
                    @@@@@ResultCode=INFO
                    @@@@@ResultDate=2018-04-23 11:00:00
                    @@@@@ResultHTMLStart
                    ...
                    @@@@@ResultHTMLStop
                */
                String lastLogLine = null;
                String resultDateStr = null;
                if (logText != null) {
                    boolean copyHTML = false;
                    StringBuffer logClean = new StringBuffer();
                    StringBuffer htmlClean = new StringBuffer();
                    String[] lines = logText.split("\\r?\\n");
                    int logStartIndex = -1;
                    for (String line : lines) {
                        if (logStartIndex < 0) {
                            // get start index for commands and content within log line
                            int i = line.indexOf("@@@@@Result");
                            if (i >= 0) {
                                logStartIndex = i;
                            }
                        }
                        String lineContent = (logStartIndex >= 0 && line.length() > logStartIndex) ? line.substring(logStartIndex) : line;
                        if (copyHTML) {
                            if (lineContent.startsWith("@@@@@ResultHTMLStop")) {
                                copyHTML = false;
                                taskRun.setResultHTML(htmlClean.toString());
                            } else {
                                htmlClean.append(lineContent).append("\n");
                            }
                        } else if (lineContent.startsWith("@@@@@ResultHTMLStart")) {
                            copyHTML = true;
                        } else if (lineContent.startsWith("@@@@@ResultCode=")) {
                            resultCode = lineContent.substring(16);
                        } else if (lineContent.startsWith("@@@@@ResultDate=")) {
                            resultDateStr = lineContent.substring(16);
                        } else {
                            if (line.startsWith("20")) {
                                lastLogLine = line;
                            }
                            logClean.append(line).append("\n");
                        }
                    }
                    taskRun.setLogText(logClean.toString());
                }
                taskRun.setResultCode(resultCode);
                ZonedDateTime logLastZDT = scanDate(lastLogLine, taskRun.getTask().getProject().getWorker().getTimezone());
                if (logLastZDT == null && resultDateStr != null) {
                    logLastZDT = scanDate(resultDateStr, taskRun.getTask().getProject().getWorker().getTimezone());
                }

                taskRun.setStatus(status);
                if (taskFinal) {
                    if (logStartZDT != null && logLastZDT != null) {
                        // use time stamps from log (timezone of worker)
                        long durationSec = logLastZDT.toEpochSecond() - logStartZDT.toEpochSecond();
                        if (durationSec < 0) durationSec = 0;
                        taskRun.setStopTime(TimeZoneUtils.dateFromZonedDateTimeWithTimezone(startZDT.plusSeconds(durationSec), "UTC"));
                        taskRun.setDurationSec((int) durationSec);
                        logger.debug("taskStatus: (from log) getStartTime =" + taskRun.getStartTime() + ", getStopTime=" + taskRun.getStopTime() + ", getDurationSec=" + taskRun.getDurationSec());
                    } else {
                        // simply use current date instead of extracted one from worker
                        taskRun.setStopTime(TimeZoneUtils.dateFromZonedDateTimeWithTimezone(currZDT, "UTC"));
                        taskRun.setDurationSec((int) currExecSec);
                        logger.debug("taskStatus: (current) getStartTime =" + taskRun.getStartTime() + ", getStopTime=" + taskRun.getStopTime() + ", getDurationSec=" + taskRun.getDurationSec());
                    }

                    if ((status == TaskRunStatus.error || status == TaskRunStatus.timeout)
                            && taskRun.getTask().getProject().getMailReceiverError() != null) {
                        sendErrorNotification(taskRun);
                    } else if (status == TaskRunStatus.finished
                            && taskRun.getTask().getProject().getMailReceiverInfo() != null) {
                        sendSuccessNotification(taskRun);
                    }
                }

                // delete Quartz job/trigger for status update
                if (deleteStatusJob) {
                    TaskScheduler.unScheduleStatus(taskRun);
                }

                // call possible other tasks dependent from this
                if (taskFinal) {
                    triggerNextTasks(persistence.getEntityManager(), taskRun.getTask(), taskRun.getStatus(), taskRun.getStartTrigger());
                }
            }
            tx.commit();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            tx.end();
        }
    }

    @Authenticated
    public void taskInitialize() {
        logger.info("Start taskInitialize");

        List<Task> activeTasks = null;
        ArrayList<String> activeTasksUUID = new ArrayList<>();

        List<TaskRun> activeTaskRuns = null;

        ArrayList<String> activeQjobsUUID = new ArrayList<>();
        ArrayList<String> activeQjobsStatusUUID = new ArrayList<>();

        Transaction tx = persistence.createTransaction();
        try {
            // get all active task
            TypedQuery<Task> query1 = persistence.getEntityManager().createQuery(
                    "select t from pdischeduler$Task t where t.active = TRUE and t.project.active = TRUE and t.project.worker.active = TRUE", Task.class);
            query1.setViewName("task-view");
            activeTasks = query1.getResultList();

            // get all active taskRun
            TypedQuery<TaskRun> query2 = persistence.getEntityManager().createQuery(
                    "select tr from pdischeduler$TaskRun tr where tr.status = 0", TaskRun.class);
            activeTaskRuns = query2.getResultList();

            tx.commit();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            tx.end();
        }

        try {
            // Grab the Scheduler instance from the Factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            // get all exec jobs from Quartz scheduler
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("exec"))) {
                activeQjobsUUID.add(jobKey.getName());
            }

            // get all status jobs from Quartz scheduler
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("status"))) {
                activeQjobsStatusUUID.add(jobKey.getName());
            }

            // create Quartz scheduling for all active tasks
            if (activeTasks != null) {
                for (Task task : activeTasks) {
                    activeTasksUUID.add(task.getId().toString());
                    TaskScheduler.scheduleTask(task, true);
                }
            }

            // delete unused Quartz jobs (and therefore all assigned trigger)
            ArrayList<String> unusedQjobsUUID = new ArrayList<>(activeQjobsUUID);
            unusedQjobsUUID.removeAll(activeTasksUUID);
            for (String uuid : unusedQjobsUUID) {
                scheduler.deleteJob(JobKey.jobKey(uuid, "exec"));
            }

            // loop over all active taskRun
            if (activeTaskRuns != null) {
                for (TaskRun taskRun : activeTaskRuns) {
                    // now check for active lastTaskRun for this task
                    if (!activeQjobsStatusUUID.contains(taskRun.getId().toString())) {
                        TaskScheduler.scheduleStatus(taskRun);
                    }
                }
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @Authenticated
    public void taskResetForWorker(Worker worker, boolean activateWorker) {
        logger.info("Start taskResetForWorker");
        List<Task> allTasks;
        boolean activate = activateWorker && worker.getActive();
        Transaction tx = persistence.createTransaction();
        try {
            // get all active task
            TypedQuery<Task> query1 = persistence.getEntityManager().createQuery(
                    "select t from pdischeduler$Task t where t.project.worker.id = ?1", Task.class);
            query1.setParameter(1, worker.getId());
            query1.setViewName("task-view");
            allTasks = query1.getResultList();
            for (Task task : allTasks) {
                TaskScheduler.scheduleTask(task, activate && task.getProject().getActive());
            }
            tx.commit();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            tx.end();
        }
    }

    @Authenticated
    public void taskResetForProject(Project project, boolean activateProject) {
        logger.info("Start taskResetForProject");
        List<Task> allTasks;
        boolean activate = activateProject && project.getActive() && project.getWorker().getActive();
        Transaction tx = persistence.createTransaction();
        try {
            // get all active task
            TypedQuery<Task> query1 = persistence.getEntityManager().createQuery(
                    "select t from pdischeduler$Task t where t.project.id = ?1", Task.class);
            query1.setParameter(1, project.getId());
            query1.setViewName("task-view");
            allTasks = query1.getResultList();
            for (Task task : allTasks) {
                TaskScheduler.scheduleTask(task, activate);
            }
            tx.commit();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            tx.end();
        }
    }

    private TaskRun findLastTaskRun(EntityManager entityManager, Task task) {
        try {
            TypedQuery<TaskRun> query = entityManager.createQuery(
                    "select tr from pdischeduler$TaskRun tr where tr.task.id = ?1 order by tr.startTime desc", TaskRun.class);
            query.setParameter(1, task);
            query.setMaxResults(1);
            List<TaskRun> taskRunList = query.getResultList();
            if (taskRunList.size() < 1) {
                return null;
            } else {
                return taskRunList.get(0);
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return null;
    }

    private void triggerNextTasks(EntityManager entityManager, Task prevTask, TaskRunStatus prevStatus, TaskRunStartTrigger prevTrigger) {
        try {
            if ((prevTrigger == TaskRunStartTrigger.cron
                    || prevTrigger == TaskRunStartTrigger.prevTask
                    || prevTrigger == TaskRunStartTrigger.manualSeq)
                    && prevStatus != TaskRunStatus.started) {

                TypedQuery<Task> query = entityManager.createQuery(
                        "select t from pdischeduler$Task t where t.active = TRUE " +
                                "and t.triggerType in (" + TaskTriggerType.prevTaskAll.getId() + ", " + TaskTriggerType.prevTaskOk.getId() + ", " + TaskTriggerType.prevTaskErr.getId() + ") " +
                                "and t.prevTask.id = ?1", Task.class);
                query.setParameter(1, prevTask);
                query.setMaxResults(1);
                List<Task> nextTaskList = query.getResultList();
                for (Task task : nextTaskList) {

                    if (task.getTriggerType() == TaskTriggerType.prevTaskAll
                            || (task.getTriggerType() == TaskTriggerType.prevTaskOk && prevStatus == TaskRunStatus.finished)
                            || (task.getTriggerType() == TaskTriggerType.prevTaskErr && (prevStatus == TaskRunStatus.error || prevStatus == TaskRunStatus.timeout))) {

                        TaskScheduler.scheduleTaskNow(task);
                    }
                }
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private ZonedDateTime scanDate(String str, String timezone) {
        ZonedDateTime zonedDateTime = null;

        if (str != null) {
            try {
                if (str.matches("20[0-9][0-9]-[01][0-9]-[0123][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9].*")) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    zonedDateTime = LocalDateTime.parse(str.substring(0, 19), dtf).atZone(ZoneId.of(timezone));

                } else if (str.matches("20[0-9][0-9]/[01][0-9]/[0123][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9].*")) {
                    // 2018/03/10 14:17:37.668
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    zonedDateTime = LocalDateTime.parse(str.substring(0, 19), dtf).atZone(ZoneId.of(timezone));

                }
            } catch (Exception se) {
                return null;
            }
        }
        return zonedDateTime;
    }

    private void sendErrorNotification(TaskRun taskRun) {
        logger.debug("sendErrorNotification started: " + taskRun.getTask().getName());
        try {
            String receiver = taskRun.getTask().getProject().getMailReceiverError();
            if (receiver != null && receiver.trim().length() > 0) {
                String subject = "PDI Scheduler ERROR: " + taskRun.getTask().getProject().getName()
                        + " - " + taskRun.getTask().getName();
                Map<String, Serializable> params = new HashMap<>();
                params.put("projectName", taskRun.getTask().getProject().getName());
                params.put("taskName", taskRun.getTask().getName());
                params.put("taskrunStart", TimeZoneUtils.strFromUtcDateConvertToTimezone(
                        taskRun.getStartTime(),
                        taskRun.getTask().getProject().getTimezone()));
                params.put("taskrunStop", TimeZoneUtils.strFromUtcDateConvertToTimezone(
                        taskRun.getStopTime(),
                        taskRun.getTask().getProject().getTimezone()));
                params.put("taskrunDuration", taskRun.getDurationSec());
                params.put("taskrunLog", text2html(taskRun.getLogText()));

                logger.debug("´params: " + params);

                EmailInfo emailInfo = new EmailInfo(
                        receiver,
                        subject,
                        null,
                        EmailInfo.HTML_CONTENT_TYPE,
                        "com/mschneider/pdischeduler/templates/mail_failure_en.html",
                        params);

                emailerAPI.sendEmailAsync(emailInfo);
            }
        } catch (Exception e) {
            logger.error("sendErrorNotification failure", e);
        }
    }

    private void sendSuccessNotification(TaskRun taskRun) {
        logger.debug("sendErrorNotification started: " + taskRun.getTask().getName());
        try {
            String receiver = taskRun.getTask().getProject().getMailReceiverInfo();
            if (receiver != null && receiver.trim().length() > 0) {
                String subject = "PDI Scheduler INFO: " + taskRun.getTask().getProject().getName()
                        + " - " + taskRun.getTask().getName();
                Map<String, Serializable> params = new HashMap<>();
                params.put("projectName", taskRun.getTask().getProject().getName());
                params.put("taskName", taskRun.getTask().getName());
                params.put("taskrunStart", TimeZoneUtils.strFromUtcDateConvertToTimezone(
                        taskRun.getStartTime(),
                        taskRun.getTask().getProject().getTimezone()));
                params.put("taskrunStop", TimeZoneUtils.strFromUtcDateConvertToTimezone(
                        taskRun.getStopTime(),
                        taskRun.getTask().getProject().getTimezone()));
                params.put("taskrunDuration", taskRun.getDurationSec());
                params.put("taskrunLog", text2html(taskRun.getLogText()));

                EmailInfo emailInfo = new EmailInfo(
                        receiver,
                        subject,
                        null,
                        EmailInfo.HTML_CONTENT_TYPE,
                        "com/mschneider/pdischeduler/templates/mail_success_en.html",
                        params);

                emailerAPI.sendEmailAsync(emailInfo);
            }
        } catch (Exception e) {
            logger.error("sendSuccessNotification failure", e);
        }
    }

    private String text2html(String str) {
        if (str != null) {
            String retStr = str.replaceAll("<", "&lt;");
            retStr = retStr.replaceAll(">", "&gt;");
            retStr = retStr.replaceAll("(\r\n|\r|\n|\n\r)", "<br>");
            return retStr;
        }
        return "";
    }


}
