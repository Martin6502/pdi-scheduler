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

import com.mschneider.pdischeduler.entity.Task;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeUpdateEntityListener;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.mschneider.pdischeduler.utils.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("pdischeduler_TaskEntityListener")
public class TaskEntityListener implements
        BeforeInsertEntityListener<Task>,
        BeforeUpdateEntityListener<Task>,
        BeforeDeleteEntityListener<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskEntityListener.class);

    @Override
    public void onBeforeInsert(Task entity, EntityManager entityManager) {
        logger.info("onBeforeInsert: " + entity.getName());
        TaskScheduler.scheduleTask(entity, true);
    }

    @Override
    public void onBeforeUpdate(Task entity, EntityManager entityManager) {
        logger.info("onBeforeUpdate: " + entity.getName());
        TaskScheduler.scheduleTask(entity, true);
    }

    @Override
    public void onBeforeDelete(Task entity, EntityManager entityManager) {
        logger.info("onBeforeDelete: " + entity.getName());
        TaskScheduler.scheduleTask(entity, false);
    }

}
