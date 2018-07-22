alter table PDISCHEDULER_TASK_RUN rename column worker_id to worker_id__u53837 ;
alter table PDISCHEDULER_TASK_RUN rename column uuid to uuid__u40767 ;
alter table PDISCHEDULER_TASK_RUN add column WORKER_CODE varchar(255) ;
