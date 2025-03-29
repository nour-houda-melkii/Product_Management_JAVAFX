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
            // Vérification du chemin du fichier FXML
            String fxmlPath = "/ListPorduit.fxml"; // Assurez-vous que le fichier est au bon endroit
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Liste des produits"); // Titre de la fenêtre
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            e.printStackTrace(); // Affiche la trace de l'exception pour faciliter le débogage
        }

    }
}

