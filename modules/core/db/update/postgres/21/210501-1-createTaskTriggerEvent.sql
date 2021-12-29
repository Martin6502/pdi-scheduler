create table PDISCHEDULER_TASK_TRIGGER_EVENT (
    ID uuid,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    TASK_ID uuid not null,
    EXTERNAL_REFERENCE varchar(255),
    --
    primary key (ID)
);