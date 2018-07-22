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

package com.mschneider.pdischeduler.web.worker;

import com.haulmont.cuba.gui.components.*;
import com.mschneider.pdischeduler.entity.WorkerType;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import com.mschneider.pdischeduler.entity.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

public class WorkerEdit extends AbstractEditor<Worker> {

    private static final Logger logger = LoggerFactory.getLogger(WorkerEdit.class);

    @Inject
    private PasswordField password;
    @Inject
    private LookupField timezoneLookup;
    @Inject
    private LookupField workerTypeLookup;
    @Inject
    private VBoxLayout worker_type_0;
    @Inject
    private TextField PdiRootDir;
    @Inject
    private VBoxLayout worker_type_1;
    @Inject
    private TextField pdiReposId;
    @Inject
    private TextField pdiReposUser;
    @Inject
    private PasswordField pdiReposPassword;


    public void showPassword() {
        showNotification(password.getValue(), NotificationType.HUMANIZED);
    }

    public void showPasswordRepos() {
        showNotification(pdiReposPassword.getValue(), NotificationType.HUMANIZED);
    }

    @Override
    public void init(Map<String, Object> params) {
        logger.debug("init");
        super.init(params);

        // initialize timezone lookup
        timezoneLookup.setOptionsMap(TimeZoneUtils.getLookupList());

        // initialize workerType lookup
        /*
        Map<String, Object> mapWorkerType = new LinkedHashMap<>();
        mapWorkerType.put("PDI file based", 0);
        mapWorkerType.put("PDI repository based", 1);
        workerTypeLookup.setOptionsMap(mapWorkerType);
        */

        worker_type_0.setVisible(false);
        worker_type_1.setVisible(false);

        workerTypeLookup.addValueChangeListener(e -> {
            WorkerType wtype = workerTypeLookup.getValue();
            worker_type_0.setVisible(false);
            worker_type_1.setVisible(false);
            if (wtype == WorkerType.PdiFile) {
                worker_type_0.setVisible(true);
                pdiReposId.setValue(null);
                pdiReposUser.setValue(null);
                pdiReposPassword.setValue(null);
            } else if (wtype == WorkerType.PdiRepos) {
                worker_type_1.setVisible(true);
                PdiRootDir.setValue(null);
            }
        });

    }
}