alter table PDISCHEDULER_TASK rename column prev_task_condition to prev_task_condition__u30089 ;
alter table PDISCHEDULER_TASK add column TRIGGER_TYPE integer ^
update PDISCHEDULER_TASK set TRIGGER_TYPE = 0 where TRIGGER_TYPE is null ;
alter table PDISCHEDULER_TASK alter column TRIGGER_TYPE set not null ;
