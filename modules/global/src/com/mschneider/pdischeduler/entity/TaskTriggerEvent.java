package com.mschneider.pdischeduler.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Table(name = "PDISCHEDULER_TASK_TRIGGER_EVENT")
@Entity(name = "pdischeduler_TaskTriggerEvent")
public class TaskTriggerEvent extends BaseUuidEntity {

    private static final long serialVersionUID = -2969527893525740738L;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "RECEIVED", nullable = false)
    protected Date received;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TASK_ID")
    protected Task task;

    @Column(name = "EXTERNAL_REFERENCE")
    protected String externalReference;

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
}