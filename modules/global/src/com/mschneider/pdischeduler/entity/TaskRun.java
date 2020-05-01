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

package com.mschneider.pdischeduler.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@SuppressWarnings("WeakerAccess")
@NamePattern("%s|id")
@Table(name = "PDISCHEDULER_TASK_RUN")
@Entity(name = "pdischeduler$TaskRun")
public class TaskRun extends BaseUuidEntity {

    private static final long serialVersionUID = -1659873147196744614L;

    public static final int TASKRUN_REFRESH_SEC = 23; // 23, 53, ...

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TASK_ID")
    protected Task task;

    @NotNull
    @Column(name = "STATUS", nullable = false)
    protected Integer status;

    @NotNull
    @Column(name = "START_TRIGGER", nullable = false)
    protected Integer startTrigger;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "START_TIME", nullable = false)
    protected Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "STOP_TIME")
    protected Date stopTime;

    @Column(name = "DURATION_SEC")
    protected Integer durationSec;

    @Column(name = "WORKER_CODE")
    protected String workerCode;

    @Lob
    @Column(name = "LOG_TEXT")
    protected String logText;

    @Column(name = "RESULT_CODE")
    protected String resultCode;

    @Lob
    @Column(name = "RESULT_HTML")
    protected String resultHTML;

    @Column(name = "FOLLOW_UP_STATUS", nullable = true)
    protected Integer followUpStatus;

    @Lob
    @Column(name = "FOLLOW_UP_COMMENT")
    protected String followUpComment;

    @Lob
    @Column(name = "FOLLOW_UP_USER")
    protected String followUpUser;

    @Transient
    @MetaProperty
    protected String startTimeFormatted;

    @Transient
    @MetaProperty
    protected String stopTimeFormatted;


    public TaskRunStatus getStatus() {
        return status == null ? null : TaskRunStatus.fromId(status);
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status == null ? null : status.getId();
    }


    public TaskRunStartTrigger getStartTrigger() {
        return startTrigger == null ? null : TaskRunStartTrigger.fromId(startTrigger);
    }

    public void setStartTrigger(TaskRunStartTrigger startTrigger) {
        this.startTrigger = startTrigger == null ? null : startTrigger.getId();
    }

    public TaskRunFollowUp getFollowUpStatus() {
        return followUpStatus == null ? null : TaskRunFollowUp.fromId(followUpStatus);
    }

    public void setFollowUpStatus(TaskRunFollowUp followUpStatus) {
        this.followUpStatus = followUpStatus == null ? null : followUpStatus.getId();
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setWorkerCode(String workerCode) {
        this.workerCode = workerCode;
    }

    public String getWorkerCode() {
        return workerCode;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public String getLogText() {
        return logText;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultHTML(String resultHTML) {
        this.resultHTML = resultHTML;
    }

    public String getResultHTML() {
        return resultHTML;
    }

    public String getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public void setStartTimeFormatted(String startTimeFormatted) {
        this.startTimeFormatted = startTimeFormatted;
    }

    public String getStopTimeFormatted() {
        return stopTimeFormatted;
    }

    public void setStopTimeFormatted(String stopTimeFormatted) {
        this.stopTimeFormatted = stopTimeFormatted;
    }

    public void setFollowUpComment(String followUpComment) {
        this.followUpComment = followUpComment;
    }

    public String getFollowUpComment() {
        return followUpComment;
    }

    public String getFollowUpUser() {
        return followUpUser;
    }

    public void setFollowUpUser(String followUpUser) {
        this.followUpUser = followUpUser;
    }

}