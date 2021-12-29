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
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
@NamePattern("%s|name")
@Listeners("pdischeduler_TaskEntityListener")
@Table(name = "PDISCHEDULER_TASK", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_PDISCHEDULER_TASK_UNQ", columnNames = {"PROJECT_ID", "NAME"})
})
@Entity(name = "pdischeduler$Task")
public class Task extends StandardEntity {

    private static final long serialVersionUID = -3143011337817812768L;

    private static final int TASK_TIMEOUT_DEFAULT = 600;

    @NotNull
    @OnDeleteInverse(DeletePolicy.DENY)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID")
    protected Project project;

    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    protected Boolean active = false;

    @NotNull
    @Column(name = "PDI_FILE", nullable = false)
    protected String pdiFile;

    @Column(name = "PDI_PARAMETER")
    protected String pdiParameter;

    @NotNull
    @Column(name = "LOG_LEVEL", nullable = false)
    protected String logLevel = "Basic";

    @Column(name = "SORT_KEY")
    protected Integer sortKey = 0;

    @NotNull
    @Column(name = "TRIGGER_TYPE", nullable = false)
    protected Integer triggerType = TaskTriggerType.manual.getId();

    @Column(name = "CRON_SPEC")
    protected String cronSpec;

    @Column(name = "CRON_EXCL_DATES")
    protected String cronExclDates;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PREV_TASK_ID")
    protected Task prevTask;

    @Column(name = "TIMEOUT_SEC")
    protected Integer timeoutSec = TASK_TIMEOUT_DEFAULT;

    @Column(name = "WS_TRIGGER_NAME")
    protected String wsTriggerName;

    @Column(name = "WS_TRIGGER_SECRET_KEY")
    protected String wsTriggerSecretKey;

    @Column(name = "WS_TRIGGER_RESTART_TIME")
    protected Integer wsTriggerRestartTime = 60;

    @Transient
    @MetaProperty
    protected List<TaskRun> lastTaskRun;

    @Transient
    @MetaProperty
    protected Date nextRun;

    public void setPdiParameter(String pdiParameter) {
        this.pdiParameter = pdiParameter;
    }

    public String getPdiParameter() {
        return pdiParameter;
    }


    public void setLastTaskRun(List<TaskRun> lastTaskRun) {
        this.lastTaskRun = lastTaskRun;
    }

    public List<TaskRun> getLastTaskRun() {
        return lastTaskRun;
    }

    public void setNextRun(Date nextRun) {
        this.nextRun = nextRun;
    }

    public Date getNextRun() {
        return nextRun;
    }

    public void setTriggerType(TaskTriggerType triggerType) {
        this.triggerType = triggerType == null ? null : triggerType.getId();
    }

    public TaskTriggerType getTriggerType() {
        return triggerType == null ? null : TaskTriggerType.fromId(triggerType);
    }

    public Task getPrevTask() {
        return prevTask;
    }

    public void setPrevTask(Task prevTask) {
        this.prevTask = prevTask;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setPdiFile(String pdiFile) {
        this.pdiFile = pdiFile;
    }

    public String getPdiFile() {
        return pdiFile;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setSortKey(Integer sortKey) {
        this.sortKey = sortKey;
    }

    public Integer getSortKey() {
        return sortKey;
    }

    public void setCronSpec(String cronSpec) {
        this.cronSpec = cronSpec;
    }

    public String getCronSpec() {
        return cronSpec;
    }

    public void setCronExclDates(String cronExclDates) {
        this.cronExclDates = cronExclDates;
    }

    public String getCronExclDates() {
        return cronExclDates;
    }

    public void setTimeoutSec(Integer timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    public Integer getTimeoutSec() {
        return timeoutSec;
    }

    public String getWsTriggerName() {
        return wsTriggerName;
    }

    public void setWsTriggerName(String wsTriggerName) {
        this.wsTriggerName = wsTriggerName;
    }

    public String getWsTriggerSecretKey() {
        return wsTriggerSecretKey;
    }

    public void setWsTriggerSecretKey(String wsTriggerSecretKey) {
        this.wsTriggerSecretKey = wsTriggerSecretKey;
    }

    public Integer getWsTriggerRestartTime() {
        return wsTriggerRestartTime;
    }

    public void setWsTriggerRestartTime(Integer wsTriggerRestartTime) {
        this.wsTriggerRestartTime = wsTriggerRestartTime;
    }
}