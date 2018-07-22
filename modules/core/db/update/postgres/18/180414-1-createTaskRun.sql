create table PDISCHEDULER_TASK_RUN (
    ID bigint,
    UUID uuid,
    --
    TASK_ID uuid not null,
    STATUS integer not null,
    START_TRIGGER integer not null,
    START_TIME timestamp not null,
    STOP_TIME timestamp,
    DURATION_SEC integer,
    WORKER_ID varchar(255),
    LOG_TEXT text,
    RESULT_CODE varchar(255),
    RESULT_HTML text,
    --
    primary key (ID)
);
