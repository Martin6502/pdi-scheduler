package com.mschneider.pdischeduler.web.taskrun;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Formatter;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.global.UserSession;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.entity.TaskRunStatus;
import com.mschneider.pdischeduler.web.DateTimezoneFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class TaskRunListAll extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunListAll.class);

    @Inject
    private CollectionDatasource<TaskRun, UUID> taskRunsDs;

    @Inject
    private Table<TaskRun> taskRunsTable;

    @Inject
    private UserSession userSession;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Results - ALL");

        String tzStr;
        if (userSession.getTimeZone() != null) {
            tzStr = userSession.getTimeZone().getID();
            logger.debug("TaskRunListAll: use user timezone=" + tzStr);
        } else {
            tzStr = TimeZone.getDefault().getID();
            logger.debug("TaskRunListAll: use system timezone=" + tzStr);
        }

        Formatter<Date> dateTimezoneFormatter = new DateTimezoneFormatter(tzStr);
        taskRunsTable.getColumn("startTime").setFormatter(dateTimezoneFormatter);
        taskRunsTable.getColumn("stopTime").setFormatter(dateTimezoneFormatter);

        ItemTrackingAction displayAction = new ItemTrackingAction("display") {
            @Override
            public void actionPerform(Component component) {
                TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                if (currTaskRun != null) {
                    openEditor("pdischeduler$TaskRun.display",
                            currTaskRun,
                            WindowManager.OpenType.THIS_TAB,
                            ParamsMap.of(
                                    "currProject", currTaskRun.getTask().getProject(),
                                    "currTask", currTaskRun.getTask(),
                                    "currTaskRun", currTaskRun
                            )
                    );
                }
            }
        };
        taskRunsTable.addAction(displayAction);
        taskRunsTable.setItemClickAction(displayAction);

        ItemTrackingAction taskDisplayAction = new ItemTrackingAction("taskdisplay") {
            @Override
            public void actionPerform(Component component) {
                TaskRun currTaskRun = taskRunsTable.getSingleSelected();
                if (currTaskRun != null) {
                    openEditor("pdischeduler$Task.display",
                            currTaskRun.getTask(),
                            WindowManager.OpenType.THIS_TAB,
                            ParamsMap.of(
                                    "currProject", currTaskRun.getTask().getProject(),
                                    "currTask", currTaskRun.getTask()
                            )

                    );
                }
            }
        };
        taskRunsTable.addAction(taskDisplayAction);

        taskRunsTable.setStyleProvider((entity, property) -> {
            if ("status".equals(property) || "resultCode".equals(property)) {
                if (entity.getStatus() == TaskRunStatus.error
                        || entity.getStatus() == TaskRunStatus.timeout
                        || "FATAL".equals(entity.getResultCode())
                        || "ERROR".equals(entity.getResultCode())) {
                    return "error";
                } else if ("WARN".equals(entity.getResultCode())) {
                    return "warn";
                }
            }
            return null;
        });

    }

    public void refreshDsByTimer(com.haulmont.cuba.gui.components.Timer timer) {
        onRefreshBtnClick();
    }

    public void onRefreshBtnClick() {
        taskRunsDs.refresh();
    }

}