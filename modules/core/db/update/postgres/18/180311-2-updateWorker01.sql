update PDISCHEDULER_WORKER set PASSWORD_ENCR = '' where PASSWORD_ENCR is null ;
alter table PDISCHEDULER_WORKER alter column PASSWORD_ENCR set not null ;
