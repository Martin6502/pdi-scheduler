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
    URL varchar(255) not null,
    USERID varchar(255) not null,
    PASSWORD varchar(255) not null,
    TIMEZONE varchar(255) not null,
    PDI_ROOT_DIR varchar(255) not null,
    DATA_ROOT_DIR varchar(255) not null,
    --
    primary key (ID)
);
