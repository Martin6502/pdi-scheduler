/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.web.taskrun;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskRun;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class TaskRunDisplay extends AbstractEditor<TaskRun> {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunDisplay.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @WindowParam(name = "currTask", required = true)
    private Task currTask;

    @WindowParam(name = "currTaskRun", required = true)
    private TaskRun currTaskRun;

    @Inject
    private ComponentsFactory componentsFactory;

    @Inject
    private Label headline;

    @Inject
    private Label headline2;

    @Inject
    private TextArea logText;

    @Inject
    private BrowserFrame resultHTML;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Project: " + currProject.getName() + ", Task: " + currTask.getName());

        headline.setValue("Timezone: " + currProject.getTimezone()
                + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getTimezone()));


        if ((currTaskRun.getLogText() != null || currTaskRun.getResultHTML() != null)
                && !currProject.getTimezone().equals(currProject.getWorker().getTimezone())) {
            headline2.setVisible(true);
            headline2.setValue("Worker Timezone: " + currProject.getWorker().getTimezone()
                    + ", Current Time: " + TimeZoneUtils.dateNowStr(currProject.getWorker().getTimezone()));
        } else {
            headline2.setVisible(false);
        }

        if (currTaskRun.getLogText() != null) {
            logText.setVisible(true);
        } else {
            logText.setVisible(false);
        }

        if (currTaskRun.getResultHTML() != null) {
            byte[] bytes = currTaskRun.getResultHTML().getBytes(StandardCharsets.UTF_8);

            resultHTML.setSource(StreamResource.class)
                    .setStreamSupplier(() -> new ByteArrayInputStream(bytes))
                    .setMimeType("text/html");
            resultHTML.setVisible(true);
        } else {
            resultHTML.setVisible(false);
        }

    }

    public Component generateStartTimeField(Datasource datasource, String fieldId) {
        TextField textField = componentsFactory.createComponent(TextField.class);
        textField.setValue(
                TimeZoneUtils.strFromUtcDateConvertToTimezone(currTaskRun.getStartTime(),
                        currProject.getTimezone()));
        return textField;
    }

    public Component generateStopTimeField(Datasource datasource, String fieldId) {
        TextField textField = componentsFactory.createComponent(TextField.class);
        if (currTaskRun.getStopTime() != null) {
            textField.setValue(
                    TimeZoneUtils.strFromUtcDateConvertToTimezone(currTaskRun.getStopTime(),
                            currProject.getTimezone()));
        } else {
            textField.setValue("");
        }
        return textField;
    }

    public void onCloseBtnClick(Component source) {
        close(CLOSE_ACTION_ID);
    }

}