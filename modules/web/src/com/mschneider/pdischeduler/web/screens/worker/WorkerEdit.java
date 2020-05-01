package com.mschneider.pdischeduler.web.screens.worker;


import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.screen.*;
import com.mschneider.pdischeduler.entity.Worker;
import com.mschneider.pdischeduler.entity.WorkerType;
import com.mschneider.pdischeduler.utils.TimeZoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;

@SuppressWarnings("NullableProblems")
@UiController("pdischeduler$Worker.edit")
@UiDescriptor("worker-edit.xml")
@LoadDataBeforeShow
@EditedEntityContainer("workerDc")
@PrimaryEditorScreen(Worker.class)
public class WorkerEdit extends StandardEditor<Worker> {

    private static final Logger logger = LoggerFactory.getLogger(WorkerEdit.class);

    @Inject
    private PasswordField password;
    @Inject
    private LookupField<String> timezoneLookup;
    @Inject
    private LookupField<WorkerType> workerTypeLookup;
    @Inject
    private VBoxLayout worker_type_0;
    @Inject
    private TextField<String> PdiRootDir;
    @Inject
    private VBoxLayout worker_type_1;
    @Inject
    private TextField<String> pdiReposId;
    @Inject
    private TextField<String> pdiReposUser;
    @Inject
    private PasswordField pdiReposPassword;
    @Inject
    private Notifications notifications;


    public void showPassword() {
        notifications.create()
                .withType(Notifications.NotificationType.HUMANIZED)
                .withContentMode(ContentMode.TEXT)
                .withCaption(Objects.requireNonNull(password.getValue()))
                .show();
    }

    public void showPasswordRepos() {
        notifications.create()
                .withType(Notifications.NotificationType.HUMANIZED)
                .withContentMode(ContentMode.TEXT)
                .withCaption(Objects.requireNonNull(pdiReposPassword.getValue()))
                .show();
    }

    @Subscribe
    protected void onInit(InitEvent event) {

        logger.debug("onInit started");

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