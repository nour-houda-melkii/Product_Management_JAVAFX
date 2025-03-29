package controllers;

import entities.Produit;
import Services.ProduitServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProductFrontController {
    @FXML
    private FlowPane productContainer;

    @FXML
    public void initialize() {
        loadProductsInCardView();
    }

    private void loadProductsInCardView() {
        try {
            ProduitServices produitService = new ProduitServices();
            List<Produit> products = produitService.showAll();
            productContainer.getChildren().clear();

            for (Produit product : products) {
                VBox card = createProductCard(product);
                productContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private VBox createProductCard(Produit product) {
        // Create a styled card container
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setPrefHeight(320);
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Product Image with better styling
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        loadProductImage(product, imageView);

        // Create container for image to center it
        HBox imageContainer = new HBox(imageView);
        imageContainer.setStyle("-fx-alignment: center; -fx-padding: 10 0 5 0;");

        // Product Details with styled labels
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 10 0 10;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a085; -fx-padding: 0 10 5 10;");

        // Description label
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 0 10 0 10;");
        descLabel.setWrapText(true);

        // Create container for buy button
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 5 10 10 10;");

        // Buy Button
        Button buyButton = new Button("Add to Cart");
        buyButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4;");
        buyButton.setOnAction(e -> showAlert("Shopping Cart", "Product '" + product.getName() + "' added to cart!"));

        // Add button to button container
        buttonBox.getChildren().add(buyButton);

        // Add all components to card
        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, descLabel, buttonBox);
        return card;
    }

    private void loadProductImage(Produit product, ImageView imageView) {
        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                File file = new File(product.getImagePath());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    // Set a default placeholder image
                    imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
                }
            } else {
                // Set a default placeholder image
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set a default placeholder image in case of error
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
            } catch (Exception ex) {
                // If placeholder can't be loaded, leave it empty
            }
        }
    }

    @FXML
    private void handleBackToAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Stage stage = (Stage) productContainer.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to admin dashboard");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}