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

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Component;
import com.mschneider.pdischeduler.entity.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProjectDisplay extends AbstractEditor<Project> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDisplay.class);

    @WindowParam(name = "currProject", required = true)
    private Project currProject;

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init: " + params);
        super.init(params);
        super.setCaption("Project: " + currProject.getName());
    }

    public void onCloseBtnClick(Component source) {
        close(CLOSE_ACTION_ID);
    }

}