package controllers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Services.FaceRecognitionService;
import Services.FaceRecognitionService.FaceDetectionCallback;

public class FaceLoginController {

    @FXML
    private ImageView cameraView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button enrollButton;

    @FXML
    private Button manualLoginButton;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private VBox loginContainer;

    private FaceRecognitionService faceService;
    private AtomicBoolean isRecognized = new AtomicBoolean(false);

    @FXML
    public void initialize() {
        statusLabel.setText("Initializing face recognition...");
        progressIndicator.setVisible(true);

        // Initialize face recognition service
        faceService = new FaceRecognitionService();

        // Bind the recognized property
        faceService.faceRecognizedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                isRecognized.set(true);
                Platform.runLater(() -> {
                    statusLabel.setText("Face recognized! Logging in...");
                    statusLabel.setStyle("-fx-text-fill: green;");

                    // Short pause before loading dashboard
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            Platform.runLater(this::loadAdminDashboard);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                });
            }
        });

        // Start face recognition with a short delay to allow UI to initialize
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    startFaceRecognition();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Setup enroll button
        enrollButton.setOnAction(event -> {
            // Stop current detection
            faceService.stopFaceDetection();

            // Enroll face
            statusLabel.setText("Enrolling face...");
            faceService.enrollFace();

            // Restart face detection
            statusLabel.setText("Face enrolled. Please try login now.");
            startFaceRecognition();
        });

        // Setup manual login button
        manualLoginButton.setOnAction(event -> {
            faceService.stopFaceDetection();
            loadAdminDashboard();
        });
    }

    private void startFaceRecognition() {
        if (faceService == null) {
            statusLabel.setText("Face recognition service not initialized");
            return;
        }

        statusLabel.setText("Looking for face...");
        progressIndicator.setVisible(true);

        faceService.startFaceDetection(new FaceDetectionCallback() {
            @Override
            public void onFrame(Image frame) {
                cameraView.setImage(frame);
                if (progressIndicator.isVisible() && !isRecognized.get()) {
                    progressIndicator.setVisible(false);
                }
            }

            @Override
            public void onFaceRecognized() {
                // Handled by the property listener
            }

            @Override
            public void onError(String message) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + message);
                    statusLabel.setStyle("-fx-text-fill: red;");
                    progressIndicator.setVisible(false);
                });
            }
        });
    }

    private void loadAdminDashboard() {
        try {
            // Stop face detection
            if (faceService != null) {
                faceService.stopFaceDetection();
            }

            // Load admin dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) loginContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public void onWindowClosed() {
        // Ensure resources are released
        if (faceService != null) {
            faceService.stopFaceDetection();
        }
    }
}