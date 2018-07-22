create table PDISCHEDULER_TASK (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    PROJECT_ID uuid not null,
    NAME varchar(255) not null,
    DESCRIPTION text,
    ACTIVE boolean not null,
    PDI_FILE varchar(255) not null,
    LOG_LEVEL varchar(255) not null,
    SORT_KEY integer,
    PREV_TASK_ID uuid,
    PREV_TASK_CONDITION integer,
    CRON_SPEC varchar(255),
    CRON_EXCL_DATES varchar(255),
    TIMEOUT_SEC integer,
    --
    primary key (ID)
);
