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

import javax.persistence.Entity;
import javax.persistence.Table;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.chile.core.annotations.NamePattern;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import com.haulmont.chile.core.annotations.MetaProperty;

import javax.persistence.Transient;

import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.mschneider.pdischeduler.utils.CryptUtils;

import javax.persistence.Lob;

@SuppressWarnings({"unused", "WeakerAccess"})
@NamePattern("%s|name")
@Listeners("pdischeduler_WorkerEntityListener")
@Table(name = "PDISCHEDULER_WORKER")
@Entity(name = "pdischeduler$Worker")
public class Worker extends StandardEntity {
    private static final long serialVersionUID = -987404849862113193L;

    @NotNull
    @Column(name = "NAME", nullable = false, unique = true)
    protected String name;

    @Lob
    @Column(name = "DESCRIPTION")
    protected String description;

    @NotNull
    @Column(name = "ACTIVE", nullable = false)
    protected Boolean active = true;

    @NotNull
    @Column(name = "URL", nullable = false)
    protected String url;

    @NotNull
    @Column(name = "USERID", nullable = false)
    protected String userid;

    @Transient
    @MetaProperty(mandatory = true)
    @NotNull
    protected String password;

    @NotNull
    @Column(name = "PASSWORD_ENCR", nullable = false)
    protected String passwordEncr;

    @NotNull
    @Column(name = "TIMEZONE", nullable = false)
    protected String timezone;

    @NotNull
    @Column(name = "DATA_ROOT_DIR", nullable = false)
    protected String dataRootDir;

    @NotNull
    @Column(name = "WORKER_TYPE", nullable = false)
    protected Integer workerType = WorkerType.PdiFile.getId();

    @Column(name = "PDI_ROOT_DIR")
    protected String pdiRootDir;

    @Column(name = "PDI_REPOS_ID")
    protected String pdiReposId;

    @Column(name = "PDI_REPOS_USER")
    protected String pdiReposUser;

    @Transient
    @MetaProperty
    protected String pdiReposPassword;

    @Column(name = "PDI_REPOS_PASSWORD_ENCR")
    protected String pdiReposPasswordEncr;

    public WorkerType getWorkerType() {
        return workerType == null ? null : WorkerType.fromId(workerType);
    }

    public void setWorkerType(WorkerType workerType) {
        this.workerType = workerType == null ? null : workerType.getId();
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

    public void setPdiReposId(String pdiReposId) {
        this.pdiReposId = pdiReposId;
    }

    public String getPdiReposId() {
        return pdiReposId;
    }

    public void setPdiReposUser(String pdiReposUser) {
        this.pdiReposUser = pdiReposUser;
    }

    public String getPdiReposUser() {
        return pdiReposUser;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }


    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setPdiRootDir(String pdiRootDir) {
        this.pdiRootDir = pdiRootDir;
    }

    public String getPdiRootDir() {
        return pdiRootDir;
    }

    public void setDataRootDir(String dataRootDir) {
        this.dataRootDir = dataRootDir;
    }

    public String getDataRootDir() {
        return dataRootDir;
    }

    /*
     * worker password handling
     */
    public void setPasswordEncr(String passwordEncr) {
        this.passwordEncr = passwordEncr;
        if (passwordEncr == null) {
            this.password = null;
        } else {
            this.password = CryptUtils.decryptDefault(passwordEncr);
        }
    }

    public String getPasswordEncr() {
        return passwordEncr;
    }

    public void setPassword(String password) {
        this.password = password;
        if (password == null) {
            this.passwordEncr = null;
        } else {
            this.passwordEncr = CryptUtils.encryptDefault(password);
        }
    }

    public String getPassword() {
        if (password == null) {
            if (passwordEncr != null) {
                password = CryptUtils.decryptDefault(passwordEncr);
            }
        }
        return password;
    }

    /*
     * worker password handling
     */
    public void setPdiReposPasswordEncr(String pdiReposPasswordEncr) {
        this.pdiReposPasswordEncr = pdiReposPasswordEncr;
        if (pdiReposPasswordEncr == null) {
            this.pdiReposPassword = null;
        } else {
            this.pdiReposPassword = CryptUtils.decryptDefault(pdiReposPasswordEncr);
        }
    }

    public String getPdiReposPasswordEncr() {
        return pdiReposPasswordEncr;
    }

    public void setPdiReposPassword(String pdiReposPassword) {
        this.pdiReposPassword = pdiReposPassword;
        if (pdiReposPassword == null) {
            this.pdiReposPasswordEncr = null;
        } else {
            this.pdiReposPasswordEncr = CryptUtils.encryptDefault(pdiReposPassword);
        }
    }

    public String getPdiReposPassword() {
        if (pdiReposPassword == null) {
            if (pdiReposPasswordEncr != null) {
                pdiReposPassword = CryptUtils.decryptDefault(pdiReposPasswordEncr);
            }
        }
        return pdiReposPassword;
    }


}