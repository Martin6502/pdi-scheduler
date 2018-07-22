alter table PDISCHEDULER_WORKER rename column password to password__UNUSED ;
alter table PDISCHEDULER_WORKER alter column password__UNUSED drop not null ;
alter table PDISCHEDULER_WORKER add column PASSWORD_ENCR varchar(255) ;
