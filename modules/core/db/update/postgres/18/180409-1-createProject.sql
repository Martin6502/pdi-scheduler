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
    ACTIVE boolean not null,
    TIMEZONE varchar(255) not null,
    MAIL_RECEIVER_INFO varchar(4095),
    MAIL_RECEIVER_ERROR varchar(4095),
    --
    primary key (ID)
);
