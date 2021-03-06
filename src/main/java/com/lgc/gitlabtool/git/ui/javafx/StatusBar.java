package com.lgc.gitlabtool.git.ui.javafx;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.lgc.gitlabtool.git.listeners.stateListeners.ApplicationState;
import com.lgc.gitlabtool.git.listeners.stateListeners.StateListener;
import com.lgc.gitlabtool.git.services.ServiceProvider;
import com.lgc.gitlabtool.git.services.StateService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

/**
 * Modified HBox component that shows current application status
 * Status bar contains progress indicator, that will be shown if some operation runs and won't show otherwise
 *
 * @author Igor Khlaponin
 *
 */
public class StatusBar extends HBox implements StateListener {

    private static final StateService _stateService = ServiceProvider.getInstance().getService(StateService.class);

    private static final double STATUS_BAR_HEIGHT = 15.0;
    private static final int ELEMENTS_SPACING = 5;
    /** Default application state */
    private static final String READY_TO_USE_STATE = StringUtils.EMPTY;

    @FXML
    private final ProgressIndicator _statusIndicator = new ProgressIndicator();
    @FXML
    private final Label _currentStatus = new Label();

    /**
     * Constructor that creates the instance of this class
     */
    public StatusBar() {
        initUI();
        initListeners();
    }

    private void initUI() {
        this.setAlignment(Pos.CENTER_RIGHT);
        this.setMaxHeight(STATUS_BAR_HEIGHT);
        this.setHeight(STATUS_BAR_HEIGHT);
        _currentStatus.setText(READY_TO_USE_STATE);
        _statusIndicator.setVisible(false);

        getStyleClass().clear();
        getStyleClass().add("status-bar");
        getChildren().addAll(_currentStatus, _statusIndicator);
        setSpacing(ELEMENTS_SPACING);
    }

    private void initListeners() {
        for (ApplicationState state : ApplicationState.values()) {
            _stateService.addStateListener(state, this);
        }
    }

    @Override
    public void handleEvent(ApplicationState changedState, boolean isActivate) {
        showStatus();
    }

    /**
     * Shows status if some operations are running
     */
    private void showStatus() {
        Platform.runLater(() -> _currentStatus.setText(getStatusChain()));
        _statusIndicator.setVisible(_stateService.isBusy());
    }

    private String getStatusChain() {
        List<ApplicationState> activeStates = _stateService.getActiveStates();
        return activeStates.stream()
                           .map(ApplicationState::toString)
                           .collect(Collectors.joining(", "));
    }
}
