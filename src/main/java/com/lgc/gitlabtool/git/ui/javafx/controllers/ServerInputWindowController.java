package com.lgc.gitlabtool.git.ui.javafx.controllers;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lgc.gitlabtool.git.services.NetworkService;
import com.lgc.gitlabtool.git.services.ServiceProvider;
import com.lgc.gitlabtool.git.services.StorageService;
import com.lgc.gitlabtool.git.services.ThemeService;
import com.lgc.gitlabtool.git.ui.icon.AppIconHolder;
import com.lgc.gitlabtool.git.ui.javafx.GLTScene;
import com.lgc.gitlabtool.git.util.ScreenUtil;
import com.lgc.gitlabtool.git.util.URLManager;
import com.lgc.gitlabtool.git.xml.Server;
import com.lgc.gitlabtool.git.xml.Servers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ServerInputWindowController {

    private static final Logger logger = LogManager.getLogger(ServerInputWindowController.class);

    private final String WRONG_INPUT_MESSAGE = "Wrong input! Please try again";
    private final String WRONG_SERVER_ADDRESS_MESSAGE = "Please enter an URL of existing \nGitLab server";
    private final String SERVER_ALREADY_EXISTS_MESSAGE = "This server already exists!";
    private final String VERIFYING_MESSAGE = "URL is verifying. Please wait...";
    private final String NO_INTERNET_CONNECTION_MESSAGE = "No Internet connection";

    private final StorageService storageService = ServiceProvider.getInstance().getService(StorageService.class);
    private final NetworkService networkService = ServiceProvider.getInstance().getService(NetworkService.class);
    private final ThemeService _themeService = ServiceProvider.getInstance().getService(ThemeService.class);

    @FXML
    private Label server;

    @FXML
    private TextField serverTextField;

    @FXML
    private Button okButton;

    @FXML
    private Label message;

    @FXML
    private ComboBox<String> api;

    public ServerInputWindowController() {
    }

    @FXML
    private void initialize() {
        message.setVisible(false);

        api.getItems().addAll("v4");
        api.setValue(api.getItems().get(0));
    }

    public void loadServerInputWindow(Parent root) throws IOException {
        Stage stage = new Stage();
        stage.setScene(new GLTScene(root));
        stage.setTitle("Server selection");
        Image appIcon = AppIconHolder.getInstance().getAppIcoImage();
        stage.getIcons().add(appIcon);
        stage.initModality(Modality.APPLICATION_MODAL);

        /* Set sizing and position */
        ScreenUtil.adaptForMultiScreens(stage, 300, 100);

        stage.showAndWait();
    }

    @FXML
    public void onOkButton() {
        showMessage(VERIFYING_MESSAGE, _themeService.getCurrentTheme().getSuccessFontColorCss());
        getStage().getScene().setCursor(Cursor.WAIT);
        if (!isInputValid(serverTextField.getText())) {
            logger.warn(WRONG_INPUT_MESSAGE + ": " + serverTextField.getText());
            showMessage(WRONG_INPUT_MESSAGE, _themeService.getCurrentTheme().getErrorFontColorCss());
            getStage().getScene().setCursor(Cursor.DEFAULT);
            return;
        }
        if (isServerAlreadyExists(serverTextField.getText())) {
            logger.warn(SERVER_ALREADY_EXISTS_MESSAGE + ": " + serverTextField.getText());
            showMessage(SERVER_ALREADY_EXISTS_MESSAGE, _themeService.getCurrentTheme().getErrorFontColorCss());
            getStage().getScene().setCursor(Cursor.DEFAULT);
            return;
        }

        networkService.runURLVerification(serverTextField.getText(), (responseCode) -> {

            try {
                if (responseCode > 0 && responseCode <= HttpStatus.SC_UNAUTHORIZED) {
                    Platform.runLater(() -> {
                        updateServersList();
                        getStage().close();
                    });
                } else if (responseCode < 0 || responseCode > HttpStatus.SC_UNAUTHORIZED) {
                    Platform.runLater(() -> {
                        logger.warn(WRONG_SERVER_ADDRESS_MESSAGE + ": " + serverTextField.getText());
                        showMessage(WRONG_SERVER_ADDRESS_MESSAGE, _themeService.getCurrentTheme().getErrorFontColorCss());
                    });
                } else {
                    Platform.runLater(() -> {
                        logger.warn(NO_INTERNET_CONNECTION_MESSAGE);
                        showMessage(NO_INTERNET_CONNECTION_MESSAGE, _themeService.getCurrentTheme().getErrorFontColorCss());
                    });
                }

            } finally {
                getStage().getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    private void updateServersList() {
        String inputServerName = URLManager.trimServerURL(serverTextField.getText());
        List<Server> servers = storageService.loadServers().getServers();
        int index = 0;
        if (inputServerName != null && !inputServerName.equals("")) {
            servers.add(index, new Server(inputServerName, api.getValue()));
        }
        storageService.updateServers(new Servers(servers));
    }

    private void showMessage(String msg, Color color) {
        message.setText(msg);
        message.setTextFill(Color.web(color.toString()));
        message.setVisible(true);
    }

    private boolean isInputValid(String inputURL) {
        return URLManager.isURLValid(inputURL);
    }

    private boolean isServerAlreadyExists(String inputURL) {
        String url = URLManager.trimServerURL(inputURL);
        List<Server> servers = storageService.loadServers().getServers();
        return servers.contains(new Server(url, api.getValue()));
    }

    private void setAPIVersion() { // private for now
        // TODO: manage API version from ComboBox
    }

    private Stage getStage() {
        return (Stage) okButton.getScene().getWindow();
    }
}
