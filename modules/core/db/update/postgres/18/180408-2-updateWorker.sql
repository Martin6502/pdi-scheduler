alter table PDISCHEDULER_WORKER add column DESCRIPTION text ;
alter table PDISCHEDULER_WORKER add column ACTIVE boolean ^
update PDISCHEDULER_WORKER set ACTIVE = false where ACTIVE is null ;
alter table PDISCHEDULER_WORKER alter column ACTIVE set not null ;
alter table PDISCHEDULER_WORKER add column WORKER_TYPE integer ^
update PDISCHEDULER_WORKER set WORKER_TYPE = 0 where WORKER_TYPE is null ;
alter table PDISCHEDULER_WORKER alter column WORKER_TYPE set not null ;
alter table PDISCHEDULER_WORKER add column PDI_REPOS_ID varchar(255) ;
alter table PDISCHEDULER_WORKER add column PDI_REPOS_USER varchar(255) ;
alter table PDISCHEDULER_WORKER add column PDI_REPOS_PASSWORD_ENCR varchar(255) ;
alter table PDISCHEDULER_WORKER alter column PDI_ROOT_DIR drop not null ;
