alter table PDISCHEDULER_PROJECT add constraint FK_PDISCHEDULER_PROJECT_WORKER foreign key (WORKER_ID) references PDISCHEDULER_WORKER(ID);
create index IDX_PDISCHEDULER_PROJECT_WORKER on PDISCHEDULER_PROJECT (WORKER_ID);
