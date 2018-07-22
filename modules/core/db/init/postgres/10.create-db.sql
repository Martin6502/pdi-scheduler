-- begin PDISCHEDULER_WORKER
create table PDISCHEDULER_WORKER (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    DESCRIPTION text,
    ACTIVE boolean not null,
    URL varchar(255) not null,
    USERID varchar(255) not null,
    PASSWORD_ENCR varchar(255) not null,
    TIMEZONE varchar(255) not null,
    DATA_ROOT_DIR varchar(255) not null,
    WORKER_TYPE integer not null,
    PDI_ROOT_DIR varchar(255),
    PDI_REPOS_ID varchar(255),
    PDI_REPOS_USER varchar(255),
    PDI_REPOS_PASSWORD_ENCR varchar(255),
    --
    primary key (ID)
)^
-- end PDISCHEDULER_WORKER
-- begin PDISCHEDULER_PROJECT
create table PDISCHEDULER_PROJECT (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255) not null,
    DESCRIPTION text,
    GROUP_ACCESS varchar(255),
    WORKER_ID uuid not null,
    PDI_SUB_DIR varchar(255),
    PDI_PARAMETER varchar(255),
    ACTIVE boolean not null,
    TIMEZONE varchar(255) not null,
    MAIL_RECEIVER_INFO varchar(4095),
    MAIL_RECEIVER_ERROR varchar(4095),
    CLEANUP_AFTER_DAYS integer,
    --
    primary key (ID)
)^
-- end PDISCHEDULER_PROJECT
-- begin PDISCHEDULER_TASK
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
    PDI_PARAMETER varchar(255),
    LOG_LEVEL varchar(255) not null,
    SORT_KEY integer,
    TRIGGER_TYPE integer not null,
    CRON_SPEC varchar(255),
    CRON_EXCL_DATES varchar(255),
    PREV_TASK_ID uuid,
    TIMEOUT_SEC integer,
    --
    primary key (ID)
)^
-- end PDISCHEDULER_TASK
-- begin PDISCHEDULER_TASK_RUN
create table PDISCHEDULER_TASK_RUN (
    ID uuid,
    --
    TASK_ID uuid not null,
    STATUS integer not null,
    START_TRIGGER integer not null,
    START_TIME timestamp not null,
    STOP_TIME timestamp,
    DURATION_SEC integer,
    WORKER_CODE varchar(255),
    LOG_TEXT text,
    RESULT_CODE varchar(255),
    RESULT_HTML text,
    --
    primary key (ID)
)^
-- end PDISCHEDULER_TASK_RUN
