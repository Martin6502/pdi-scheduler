alter table PDISCHEDULER_TASK_TRIGGER_EVENT add constraint FK_PDISCHEDULER_TASK_TRIGGER_EVENT_TASK foreign key (TASK_ID) references PDISCHEDULER_TASK(ID);
create index IDX_PDISCHEDULER_TASK_TRIGGER_EVENT_TASK on PDISCHEDULER_TASK_TRIGGER_EVENT (TASK_ID);
