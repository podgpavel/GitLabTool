package com.lgc.gitlabtool.git.ui.javafx;

import java.net.URL;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lgc.gitlabtool.git.ui.UserInterface;
import com.lgc.gitlabtool.git.ui.ViewKey;
import com.lgc.gitlabtool.git.ui.icon.AppIconHolder;
import com.lgc.gitlabtool.git.ui.javafx.controllers.ModularController;
import com.lgc.gitlabtool.git.util.ProjectPropertiesUtil;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class JavaFXUI extends Application implements UserInterface {
    private static final Logger logger = LogManager.getLogger(LoginDialog.class);

    private Image appIcon;
    private Stage mainStage;

    @Override
    public void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        appIcon = AppIconHolder.getInstance().getAppIcoImage();

        URL modularWindow = getClass().getClassLoader().getResource(ViewKey.MODULAR_CONTAINER.getPath());
        if (modularWindow == null) {
            logger.error("Could not load fxml resource");
            return;
        }

        showLoginDialog();
        FXMLLoader fxmlLoader = new FXMLLoader(modularWindow);
        Parent root = fxmlLoader.load();

        ModularController modularController = fxmlLoader.getController();
        modularController.loadWelcomeWindow();

        Scene scene = new Scene(root);

        primaryStage.setTitle("Gitlab Tool v." + ProjectPropertiesUtil.getProjectVersion());
        primaryStage.setScene(scene);
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(primaryScreenBounds.getMaxY() / 1.5);
        primaryStage.setWidth(primaryScreenBounds.getMaxX() / 1.5);
        primaryStage.getIcons().add(appIcon);
        primaryStage.setOnCloseRequest(confirmCloseEventHandler);
        primaryStage.show();

    }

    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
        Alert closeConfirmation = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
        Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(ButtonType.OK);

        exitButton.setText("Exit");
        closeConfirmation.setHeaderText("Confirm exit");
        closeConfirmation.initModality(Modality.APPLICATION_MODAL);
        closeConfirmation.initOwner(mainStage);

        Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
        if (!ButtonType.OK.equals(closeResponse.orElse(ButtonType.CANCEL))) {
            event.consume();
        }
    };

    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog();
        Stage stage = (Stage) loginDialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(appIcon);
        loginDialog.showAndWait();
    }
}
