package com.mschneider.pdischeduler.service;

public interface WebhookService {
    String NAME = "pdischeduler_WebhookService";

    public WebHookServiceResult parseServer(String triggerName, String object, String master, String log, String headers, String ip, String context, String user, String installationId);

}
