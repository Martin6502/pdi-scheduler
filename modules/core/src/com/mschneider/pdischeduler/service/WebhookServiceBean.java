package com.mschneider.pdischeduler.service;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UuidSource;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.entity.TaskTriggerEvent;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Service(WebhookService.NAME)
public class WebhookServiceBean implements WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookServiceBean.class);

    @Inject
    private Persistence persistence;

    @Inject
    private Metadata metadata;

    @Inject
    private UuidSource uuidSource;

    @Override
    public WebHookServiceResult taskTrigger(String wsTriggerName, String wsTriggerSecretKey, String externalReference) {

        logger.info("taskTrigger: "  + wsTriggerName + " - " + wsTriggerSecretKey + " - " + externalReference);

        WebHookServiceResult result = new WebHookServiceResult();
        result.setSuccess(false);

        Transaction tx = persistence.createTransaction();
        try {
            // lookup of task
            TypedQuery<Task> query = persistence.getEntityManager().createQuery(
                    "select t from pdischeduler$Task t where t.active = TRUE and t.project.active = TRUE and t.project.worker.active = TRUE and t.wsTriggerName = ?1 and t.wsTriggerSecretKey = ?2",
                    Task.class);
            query.setViewName("task-view");
            query.setParameter(1, wsTriggerName);
            query.setParameter(2, wsTriggerSecretKey);
            Task task = query.getFirstResult();

            if (task != null) {
                // valid task

                // create TaskTriggerEvent
                Date currTimeUtc = TimeZoneUtils.dateNowUtc();

                TaskTriggerEvent event = metadata.create(TaskTriggerEvent.class);
                event.setId(uuidSource.createUuid());
                event.setReceived(currTimeUtc);
                event.setTask(task);
                event.setExternalReference(externalReference);
                persistence.getEntityManager().persist(event);

                result.setSuccess(true);
            }
           tx.commit();

        } catch (Exception se) {
            logger.error("taskTrigger: ", se);
        } finally {
            tx.end();
        }

        return result;
    }

}