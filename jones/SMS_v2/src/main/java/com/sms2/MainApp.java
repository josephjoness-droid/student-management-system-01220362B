package com.sms2;

import com.sms2.util.AppLogger;
import com.sms2.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for AcaTrack – Student Management System Plus (v2).
 * Run configuration main class: com.sms2.MainApp
 */
public class MainApp extends Application {*/-


    @Override
    public void start(Stage stage) throws Exception {
        AppLogger.info("AcaTrack starting.");
        try { DatabaseManager.getConnection(); }
        catch (Exception e) { AppLogger.error("DB init failed: " + e.getMessage()); }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/sms2/fxml/Dashboard.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().add(
                getClass().getResource("/com/sms2/css/theme.css").toExternalForm());

        stage.setTitle("AcaTrack – Student Management System Plus");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        AppLogger.info("AcaTrack closing.");
        DatabaseManager.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
