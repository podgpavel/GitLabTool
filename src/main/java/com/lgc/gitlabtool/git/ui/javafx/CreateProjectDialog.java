package com.lgc.gitlabtool.git.ui.javafx;
import java.util.List;
import java.util.function.Consumer;

import com.lgc.gitlabtool.git.entities.Group;
import com.lgc.gitlabtool.git.entities.MessageType;
import com.lgc.gitlabtool.git.entities.Project;
import com.lgc.gitlabtool.git.listeners.stateListeners.ApplicationState;
import com.lgc.gitlabtool.git.project.nature.projecttype.ProjectType;
import com.lgc.gitlabtool.git.services.BackgroundService;
import com.lgc.gitlabtool.git.services.ConsoleService;
import com.lgc.gitlabtool.git.services.ProgressListener;
import com.lgc.gitlabtool.git.services.ProjectService;
import com.lgc.gitlabtool.git.services.ProjectTypeService;
import com.lgc.gitlabtool.git.services.ServiceProvider;
import com.lgc.gitlabtool.git.services.StateService;
import com.lgc.gitlabtool.git.services.ThemeService;
import com.lgc.gitlabtool.git.util.NameValidator;
import com.lgc.gitlabtool.git.util.NullCheckUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CreateProjectDialog extends GLTDialog<String> {

    private static final String DIALOG_TITLE = "Create Project Dialog";

    private final Label _nameLabel;
    private final Label _typeLabel;
    private final TextField _projectNameField;
    private final ComboBox<String> _typeComboBox;
    private final Button _createButton;
    private final Button _cancelButton;

    private final Group _selectGroup;
    private final Consumer<Object> _onSuccessAction;

    private final Label _progressLabel;
    private final ProgressBar _progressBar = new ProgressBar();
    private final NameValidator _validator = NameValidator.get();

    private static final ProjectTypeService _typeServices = ServiceProvider.getInstance()
            .getService(ProjectTypeService.class);

    private static final ProjectService _projectService = ServiceProvider.getInstance()
            .getService(ProjectService.class);

    private final StateService _stateService = ServiceProvider.getInstance().getService(StateService.class);

    private static final ConsoleService _consoleService = ServiceProvider.getInstance()
            .getService(ConsoleService.class);

    private static final BackgroundService _backgroundService = (BackgroundService) ServiceProvider.getInstance()
            .getService(BackgroundService.class);

    private final GridPane grid = new GridPane();

    public CreateProjectDialog(Group selectGroup, Consumer<Object> onSuccessAction) {
        super(DIALOG_TITLE);

        _selectGroup = selectGroup;
        _onSuccessAction = onSuccessAction;

        grid.setAlignment(Pos.CENTER_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        _nameLabel = new Label("Name project: ");
        grid.add(_nameLabel, 0, 2);
        _projectNameField = new TextField();
        _projectNameField.textProperty().addListener(getInputFilter());
        grid.add(_projectNameField, 1, 2, 2, 1);

        _typeLabel = new Label("Type project: ");
        grid.add(_typeLabel, 0, 3);

        List<String> idsTypes = (List<String>) _typeServices.getAllIdTypes();
        ObservableList<String> options = FXCollections.observableArrayList(idsTypes);
        _typeComboBox = new ComboBox<String>(options);
        _typeComboBox.getSelectionModel().select(options.get(idsTypes.size()-1));
        grid.add(_typeComboBox, 1, 3);

        _progressLabel = new Label("...");

        _createButton = new Button("Create Project");
        _createButton.setOnAction(this::onCreateButton);
        _createButton.setDefaultButton(true);
        _createButton.setDisable(true);
        _projectNameField.setStyle("-fx-border-color: red;");

        _cancelButton = new Button("Cancel");
        _cancelButton.setOnAction(event -> {
            closeDialog();
        });

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().addAll(_createButton, _cancelButton);
        grid.add(hbBtn, 2, 7);

        setDialogContent(grid);

        getStage().setOnCloseRequest(event -> {
            if (_stateService.isActiveState(ApplicationState.CREATE_PROJECT)) {
                event.consume();
            }
        });

    }

    private void onCreateButton(ActionEvent event) {
        _createButton.setDisable(true);
        _cancelButton.setDisable(true);
        _typeComboBox.setDisable(true);
        _projectNameField.setDisable(true);

        addProgressBarOnPanel();
        _backgroundService.runInBackgroundThread(() -> {
            CreateProjectProgressListener listener = new CreateProjectProgressListener();
            String idType = _typeComboBox.getSelectionModel().getSelectedItem();
            ProjectType projectType = _typeServices.getTypeById(idType);
            _projectService.createProject(_selectGroup, _projectNameField.getText(), projectType, listener);
        });
    }

    class CreateProjectProgressListener implements ProgressListener {

        public CreateProjectProgressListener() {
            _stateService.stateON(ApplicationState.CREATE_PROJECT);
        }

        @Override
        public void onSuccess(Object... t) {
            NullCheckUtil.acceptConsumer(_onSuccessAction, null);
        }

        @Override
        public void onError(Object... t) {}

        @Override
        public void onStart(Object... t) {
            if (t[0] instanceof String) {
                updateProgressLabel((String)t[0]);
            }
        }

        @Override
        public void onFinish(Object... t) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Project project = t[0] == null ? null : (Project) t[0];
                    String contentMessage = (String) t[1];

                    boolean isSuccess = !(project == null);
                    String headerMessage = isSuccess ? "Success creating project in the " + _selectGroup.getName() + " group."
                            : "Error creating project in the " + _selectGroup.getName() + " group.";
                    closeDialog();
                    StatusDialog statusDialog = new StatusDialog(
                            "Status of creating project", headerMessage, contentMessage);
                    statusDialog.showAndWait();
                    _consoleService.addMessage(contentMessage, MessageType.determineMessageType(isSuccess));
                    _stateService.stateOFF(ApplicationState.CREATE_PROJECT);
                }
            });
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.close();
    }

    private void addProgressBarOnPanel() {
        grid.add(_progressBar, 0, 4);
        grid.add(_progressLabel, 1, 4, 3, 1);
    }

    private void updateProgressLabel(String progressLabel) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                StringProperty projectProperty = new SimpleStringProperty("");
                projectProperty.set(progressLabel);
                _progressLabel.textProperty().bind(projectProperty);
                String message = projectProperty.getValue();
                _consoleService.addMessage(message, MessageType.determineMessageType(message));
            }
        });
    }

    private ChangeListener<? super String> getInputFilter() {
        return (observable, oldValue, newValue) -> {
            if (!_projectNameField.getText().isEmpty() && isInputValid(_projectNameField.getText())) {
                _createButton.setDisable(false);
                _projectNameField.setStyle("-fx-border-color: green;");
            } else {
                _createButton.setDisable(true);
                _projectNameField.setStyle("-fx-border-color: red;");
            }
        };
    }

    private boolean isInputValid(String input) {
        return _validator.validateProjectName(input);
    }
}
