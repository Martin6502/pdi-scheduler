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

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@SuppressWarnings({"WeakerAccess", "unused"})
@NamePattern("%s|name")
@Table(name = "PDISCHEDULER_PROJECT")
@Entity(name = "pdischeduler$Project")
public class Project extends StandardEntity {
    private static final long serialVersionUID = 1167418761769852277L;

    private static final int CLEANUP_AFTER_DAYS_DEFAULT = 90;

    @NotNull
    @Column(name = "NAME", nullable = false, unique = true)
    protected String name;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @Column(name = "GROUP_ACCESS")
    protected String groupAccess;

    @OnDeleteInverse(DeletePolicy.DENY)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "WORKER_ID")
    protected Worker worker;

    @Column(name = "PDI_SUB_DIR")
    protected String pdiSubDir;

    @Column(name = "DATA_SUB_DIR")
    protected String dataSubDir;

    @Column(name = "PDI_PARAMETER")
    protected String pdiParameter;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    protected Boolean active = false;

    @NotNull
    @Column(name = "TIMEZONE", nullable = false)
    protected String timezone;

    @Column(name = "MAIL_RECEIVER_INFO", length = 4095)
    protected String mailReceiverInfo;

    @Column(name = "MAIL_RECEIVER_ERROR", length = 4095)
    protected String mailReceiverError;

    @Column(name = "CLEANUP_AFTER_DAYS")
    protected Integer cleanupAfterDays = CLEANUP_AFTER_DAYS_DEFAULT;

    public void setDataSubDir(String dataSubDir) {
        this.dataSubDir = dataSubDir;
    }

    public String getDataSubDir() {
        return dataSubDir;
    }


    public void setCleanupAfterDays(Integer cleanupAfterDays) {
        this.cleanupAfterDays = cleanupAfterDays;
    }

    public Integer getCleanupAfterDays() {
        return cleanupAfterDays;
    }


    public void setPdiParameter(String pdiParameter) {
        this.pdiParameter = pdiParameter;
    }

    public String getPdiParameter() {
        return pdiParameter;
    }


    public void setPdiSubDir(String pdiSubDir) {
        this.pdiSubDir = pdiSubDir;
    }

    public String getPdiSubDir() {
        return pdiSubDir;
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

    public void setGroupAccess(String groupAccess) {
        this.groupAccess = groupAccess;
    }

    public String getGroupAccess() {
        return groupAccess;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setMailReceiverInfo(String mailReceiverInfo) {
        this.mailReceiverInfo = mailReceiverInfo;
    }

    public String getMailReceiverInfo() {
        return mailReceiverInfo;
    }

    public void setMailReceiverError(String mailReceiverError) {
        this.mailReceiverError = mailReceiverError;
    }

    public String getMailReceiverError() {
        return mailReceiverError;
    }


}