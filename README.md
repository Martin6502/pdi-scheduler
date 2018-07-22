# PDI-Scheduler
A Web-based Scheduler for PDI (Pentaho Data Integration)

In the past I always missed a good way for scheduling PDI Jobs used together with their Community Edition.
You may find more information about PDI and also the downloads at
https://sourceforge.net/projects/pentaho/

So I started to create an own Web-based Scheduler which works together with one or more PDI Carte Server. They are executing the PDI Jobs and provides status and result information back to the Scheduler. The big advantage for this approach is, that you have a clean separation and no problems with PDI version upgrades. You may even run different versions in parallel.

Following a short overview about implemented features:
* Multiple Worker (= Carte Server) support
* Multiple Project support (a Project contains multiple Tasks and is bound to one Worker)
* Tasks which may be triggered either by cron expression, previous task or manually
* Timezone handling
* User based Role concept
* Status and Logs of executed Tasks (= PDI Jobs) are imported back to Scheduler
* Timeout handling for Tasks
* Extended monitoring

The application is developed with CUBA.platform framework ( https://github.com/cuba-platform/cuba )
and uses internally the Quartz Job Scheduler ( http://www.quartz-scheduler.org/ ) 
and also a PostgreSQL DB ( https://www.postgresql.org/ )


For Installation of PDI Scheduler please have look [here](doc/INSTALL.md)
