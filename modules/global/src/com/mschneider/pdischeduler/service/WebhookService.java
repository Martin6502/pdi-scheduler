package com.mschneider.pdischeduler.service;

public interface WebhookService {
    String NAME = "pdiControl";

    public WebHookServiceResult taskTrigger(String wsTriggerName, String wsTriggerSecretKey, String externalReference);

}
