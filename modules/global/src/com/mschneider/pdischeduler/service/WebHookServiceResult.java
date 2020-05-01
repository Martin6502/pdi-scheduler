package com.mschneider.pdischeduler.service;

import java.io.Serializable;

public class WebHookServiceResult implements Serializable {
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
