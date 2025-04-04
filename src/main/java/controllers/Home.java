package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Load the face login screen instead of going directly to dashboard
            String fxmlPath = "/face_login.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get controller to handle window close events
            FaceLoginController controller = loader.getController();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Login - Face Recognition");

            // Set up close handler to ensure webcam resources are released
            stage.setOnCloseRequest(event -> {
                controller.onWindowClosed();
            });

            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();

            // Fall back to admin dashboard if face login fails to load
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/admin_dashboard.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Admin Dashboard");
                stage.show();
            } catch (IOException e2) {
                System.err.println("Critical error: Unable to load any FXML file");
                e2.printStackTrace();
            }
        }
    }
}