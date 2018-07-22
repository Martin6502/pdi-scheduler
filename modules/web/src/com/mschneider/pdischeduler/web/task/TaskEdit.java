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

package com.mschneider.pdischeduler.web.task;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import com.mschneider.pdischeduler.entity.TaskTriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskEdit extends AbstractEditor<Task> {

    private static final Logger logger = LoggerFactory.getLogger(TaskEdit.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @Inject
    private LookupField logLevelLookup;
    @Inject
    private LookupField triggerTypeLookup;
    @Inject
    private VBoxLayout trigger_type_cron;
    @Inject
    private VBoxLayout trigger_type_prev;
    @Inject
    private TextField cronspec;
    @Inject
    private TextField cronexcldates;
    @Inject
    private LookupField prevtask;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Task Edit - " + currProject.getName());

        // initialize logLevelLookup
        List<String> logLevelList = new ArrayList<>();
        logLevelList.add("Nothing");
        logLevelList.add("Error");
        logLevelList.add("Minimal");
        logLevelList.add("Basic");
        logLevelList.add("Detailed");
        logLevelList.add("Debug");
        logLevelList.add("Row level");
        logLevelLookup.setOptionsList(logLevelList);

        trigger_type_cron.setVisible(false);
        trigger_type_prev.setVisible(false);

        triggerTypeLookup.addValueChangeListener(e -> {
            TaskTriggerType wtype = triggerTypeLookup.getValue();
            trigger_type_cron.setVisible(false);
            trigger_type_prev.setVisible(false);
            if (wtype == TaskTriggerType.manual) {
                prevtask.setValue(null);
                cronspec.setValue(null);
                cronexcldates.setValue(null);

            } else if (wtype == TaskTriggerType.cron) {
                trigger_type_cron.setVisible(true);
                prevtask.setValue(null);

            } else if (wtype == TaskTriggerType.prevTaskAll
                    || wtype == TaskTriggerType.prevTaskOk
                    || wtype == TaskTriggerType.prevTaskErr) {
                trigger_type_prev.setVisible(true);
                cronspec.setValue(null);
                cronexcldates.setValue(null);
            }
        });
    }

    @Override
    protected void initNewItem(Task item) {
        item.setProject(currProject);
    }

}