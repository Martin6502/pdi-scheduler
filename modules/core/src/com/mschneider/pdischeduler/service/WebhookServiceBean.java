package com.mschneider.pdischeduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service(WebhookService.NAME)
public class WebhookServiceBean implements WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookServiceBean.class);

    @Override
    public WebHookServiceResult parseServer(String triggerName, String object, String master, String log, String headers, String ip, String context, String user, String installationId) {

        logger.info("parseServer: "  + triggerName);

        WebHookServiceResult result = new WebHookServiceResult();
        result.setSuccess(true);
        return result;
    }

}