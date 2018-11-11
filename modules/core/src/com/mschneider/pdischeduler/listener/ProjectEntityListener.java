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

package com.mschneider.pdischeduler.listener;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeUpdateEntityListener;
import com.mschneider.pdischeduler.TaskProcessing;
import com.mschneider.pdischeduler.entity.Project;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("pdischeduler_ProjectEntityListener")
public class ProjectEntityListener implements
        BeforeInsertEntityListener<Project>,
        BeforeUpdateEntityListener<Project>,
        BeforeDeleteEntityListener<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectEntityListener.class);

    @Override
    public void onBeforeInsert(Project entity, EntityManager entityManager) {
        logger.info("onBeforeInsert: " + entity.getName());
        AppBeans.get(TaskProcessing.class).taskResetForProject(entity, true);
    }

    @Override
    public void onBeforeUpdate(Project entity, EntityManager entityManager) {
        logger.info("onBeforeUpdate: " + entity.getName());
        AppBeans.get(TaskProcessing.class).taskResetForProject(entity, true);
    }

    @Override
    public void onBeforeDelete(Project entity, EntityManager entityManager) {
        logger.info("onBeforeDelete: " + entity.getName());
        AppBeans.get(TaskProcessing.class).taskResetForProject(entity, false);
    }

}

