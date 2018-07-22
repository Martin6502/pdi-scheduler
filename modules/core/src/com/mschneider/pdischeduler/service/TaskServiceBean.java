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

package com.mschneider.pdischeduler.service;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.AppBeans;
import com.mschneider.pdischeduler.TaskProcessing;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.entity.TaskRunStartTrigger;
import com.mschneider.pdischeduler.utils.DateExclude;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

@Service(TaskService.NAME)
public class TaskServiceBean implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceBean.class);

    @Inject
    private Persistence persistence;

    @Override
    @Transactional
    public HashMap<String, TaskRun> getLastTaskRun(Project project) {
        logger.debug("getLastTaskRun started");
        HashMap<String, TaskRun> result = new HashMap<>();

        TypedQuery<TaskRun> query = persistence.getEntityManager().createNativeQuery(
                "select tr.* " +
                        "  from pdischeduler_task_run tr " +
                        "inner join ( " +
                        "  select tr2.task_id, max(tr2.start_time) as max_start_time " +
                        "  from pdischeduler_task_run tr2 " +
                        "  inner join pdischeduler_task t2 on t2.id = tr2.task_id " +
                        "  where t2.project_id = ?1 " +
                        "  group by tr2.task_id " +
                        ") trmax on trmax.task_id = tr.task_id and tr.start_time = trmax.max_start_time ",
                TaskRun.class);
        query.setParameter(1, project.getId());
        List<TaskRun> list = query.getResultList();
        for (TaskRun taskRun : list) {
            result.put(taskRun.getTask().getId().toString(), taskRun);
        }
        return result;

    }

    @Override
    public void taskExec(String taskUUID, TaskRunStartTrigger triggerType) {
        AppBeans.get(TaskProcessing.class).taskExec(taskUUID, triggerType);
    }

    @Override
    public boolean isValidCronSpec(String str) {
        return CronExpression.isValidExpression(str);
    }

    @Override
    public Date getNextDate(String timezone, String cronSpec, String cronExclDates) {
        try {
            Date currTime = TimeZoneUtils.dateNow(timezone);
            CronExpression expr = new CronExpression(cronSpec);
            expr.setTimeZone(TimeZone.getTimeZone(timezone));
            Date nextFireAt = expr.getNextValidTimeAfter(currTime);
            int maxCount = 1000;
            while (DateExclude.checkExcluded(nextFireAt, cronExclDates) && maxCount > 0) {
                nextFireAt = expr.getNextValidTimeAfter(nextFireAt);
                maxCount--;
            }
            if (maxCount > 0) {
                return nextFireAt;
            }
        } catch (Exception se) {
            logger.error("getNextDate failed", se);
        }

        return null;
    }

}