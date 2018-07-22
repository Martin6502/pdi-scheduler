alter table PDISCHEDULER_TASK rename column last_task_run_id to last_task_run_id__u12349 ;
drop index IDX_PDISCHEDULER_TASK_LAST_TASK_RUN ;
alter table PDISCHEDULER_TASK drop constraint FK_PDISCHEDULER_TASK_LAST_TASK_RUN ;
