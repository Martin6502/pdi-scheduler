alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column deleted_by to deleted_by__u44681 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column delete_ts to delete_ts__u84268 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column updated_by to updated_by__u79408 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column update_ts to update_ts__u90022 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column created_by to created_by__u97107 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column create_ts to create_ts__u47071 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT rename column version to version__u21106 ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT alter column version__u21106 drop not null ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT add column RECEIVED timestamp ^
update PDISCHEDULER_TASK_TRIGGER_EVENT set RECEIVED = current_timestamp where RECEIVED is null ;
alter table PDISCHEDULER_TASK_TRIGGER_EVENT alter column RECEIVED set not null ;
