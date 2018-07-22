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

package com.mschneider.pdischeduler.web.project;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ItemTrackingAction;
import com.haulmont.cuba.security.entity.EntityOp;
import com.mschneider.pdischeduler.entity.Project;
import com.mschneider.pdischeduler.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

public class ProjectList extends AbstractLookup {

    private static final Logger logger = LoggerFactory.getLogger(ProjectList.class);

    @Inject
    private Table<Project> projectListTable;

    @Inject
    private Security security;

    @Inject
    private Button taskbrowseBtn;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);

        ItemTrackingAction taskmonitorAction = new ItemTrackingAction("taskmonitor") {
            @Override
            public void actionPerform(Component component) {
                openWindow("pdischeduler$Task.monitor",
                        WindowManager.OpenType.NEW_TAB,
                        ParamsMap.of(
                                "currProject", projectListTable.getSingleSelected()
                        )
                );
            }
        };
        projectListTable.addAction(taskmonitorAction);
        projectListTable.setItemClickAction(taskmonitorAction);

        ItemTrackingAction taskbrowseAction = new ItemTrackingAction("taskbrowse") {
            @Override
            public void actionPerform(Component component) {
                openWindow("pdischeduler$Task.browse",
                        WindowManager.OpenType.NEW_TAB,
                        ParamsMap.of(
                                "currProject", projectListTable.getSingleSelected()
                        )
                );
            }
        };
        projectListTable.addAction(taskbrowseAction);

        // disable project admin if no permission
        if (!security.isEntityOpPermitted(Task.class, EntityOp.CREATE)) {
            taskbrowseBtn.setVisible(false);
            taskbrowseAction.setEnabled(false);
            taskbrowseAction.setVisible(false);
        }

        ItemTrackingAction taskrunmonitorAction = new ItemTrackingAction("taskrunmonitor") {
            @Override
            public void actionPerform(Component component) {
                openWindow("pdischeduler$TaskRun.listProject",
                        WindowManager.OpenType.NEW_TAB,
                        ParamsMap.of(
                                "currProject", projectListTable.getSingleSelected()
                        )
                );
            }
        };
        projectListTable.addAction(taskrunmonitorAction);

        ItemTrackingAction projectDisplayAction = new ItemTrackingAction("projectdisplay") {
            @Override
            public void actionPerform(Component component) {
                openEditor("pdischeduler$Project.display",
                        projectListTable.getSingleSelected(),
                        WindowManager.OpenType.THIS_TAB,
                        ParamsMap.of(
                                "currProject", projectListTable.getSingleSelected()
                        )
                );
            }
        };
        projectListTable.addAction(projectDisplayAction);

    }

}