package com.womenshop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // On charge la vue principale définie dans le FXML
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/womenshop/views/MainView.fxml"));
        Parent root = fxmlLoader.load();

        scene = new Scene(root, 1000, 700); // Taille de la fenêtre (1000x700)
        stage.setScene(scene);
        stage.setTitle("WomenShop - Stock Management");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}