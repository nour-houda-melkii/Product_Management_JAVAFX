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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductFrontController {
    @FXML
    private FlowPane productContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label cartCountLabel;

    @FXML
    private Label favoritesCountLabel;

    // Lists to keep track of cart and favorites
    private List<Produit> cartProducts = new ArrayList<>();
    private List<Produit> favoriteProducts = new ArrayList<>();
    private List<Produit> allProducts = new ArrayList<>(); // Store all products for filtering

    @FXML
    public void initialize() {
        setupSearchListener();
        loadProductsInCardView();
        updateCounters();
    }

    private void setupSearchListener() {
        // Add a delay to prevent searching on every keystroke
        PauseTransition pause = new PauseTransition(Duration.millis(300));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> {
                filterProducts(newValue.toLowerCase());
            });
            pause.playFromStart();
        });
    }

    private void filterProducts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If search is empty, show all products
            displayProducts(allProducts);
            return;
        }

        // Filter products based on search text (case insensitive)
        List<Produit> filteredProducts = allProducts.stream()
                .filter(product ->
                        product.getName().toLowerCase().contains(searchText) ||
                                product.getDescription().toLowerCase().contains(searchText)
                )
                .collect(Collectors.toList());

        displayProducts(filteredProducts);
    }

    private void displayProducts(List<Produit> products) {
        productContainer.getChildren().clear();
        for (Produit product : products) {
            productContainer.getChildren().add(createProductCard(product));
        }
    }

    private void loadProductsInCardView() {
        try {
            ProduitServices produitService = new ProduitServices();
            allProducts = produitService.showAll();
            displayProducts(allProducts);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load products: " + e.getMessage());
        }
    }

    private VBox createProductCard(Produit product) {
        // Create a styled card container
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setPrefHeight(350);
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        loadProductImage(product, imageView);

        // Image container
        HBox imageContainer = new HBox(imageView);
        imageContainer.setStyle("-fx-alignment: center; -fx-padding: 10 0 5 0;");

        // Product Details
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 10 0 10;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a085; -fx-padding: 0 10 5 10;");

        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 0 10 0 10;");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center; -fx-padding: 5 10 5 10;");

        // Add to Cart Button
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4;");
        addToCartButton.setOnAction(e -> {
            addToCart(product);
            showAlert("Shopping Cart", "Product '" + product.getName() + "' added to cart!");
        });

        // Favorite Button container
        HBox favoriteBox = new HBox(10);
        favoriteBox.setStyle("-fx-alignment: center; -fx-padding: 0 10 10 10;");

        // Favorite Button
        Button favoriteButton = new Button("♥");
        favoriteButton.setStyle("-fx-background-color: " + (isFavorite(product) ? "#e74c3c" : "#ecf0f1") +
                "; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 40px; -fx-min-height: 40px; -fx-font-size: 18px;");
        favoriteButton.setOnAction(e -> {
            toggleFavorite(product);
            favoriteButton.setStyle("-fx-background-color: " + (isFavorite(product) ? "#e74c3c" : "#ecf0f1") +
                    "; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 40px; -fx-min-height: 40px; -fx-font-size: 18px;");
        });

        // Add components to card
        buttonBox.getChildren().add(addToCartButton);
        favoriteBox.getChildren().add(favoriteButton);
        card.getChildren().addAll(imageContainer, nameLabel, priceLabel, descLabel, buttonBox, favoriteBox);

        return card;
    }

    private void loadProductImage(Produit product, ImageView imageView) {
        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                File file = new File(product.getImagePath());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    loadPlaceholderImage(imageView);
                }
            } else {
                loadPlaceholderImage(imageView);
            }
        } catch (Exception e) {
            loadPlaceholderImage(imageView);
        }
    }

    private void loadPlaceholderImage(ImageView imageView) {
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        } catch (Exception ex) {
            // If placeholder can't be loaded, leave it empty
        }
    }

    private void addToCart(Produit product) {
        cartProducts.add(product);
        updateCounters();
    }

    private void toggleFavorite(Produit product) {
        if (isFavorite(product)) {
            favoriteProducts.removeIf(p -> p.getId() == product.getId());
        } else {
            favoriteProducts.add(product);
        }
        updateCounters();
    }

    private boolean isFavorite(Produit product) {
        return favoriteProducts.stream().anyMatch(p -> p.getId() == product.getId());
    }

    private void updateCounters() {
        cartCountLabel.setText(String.valueOf(cartProducts.size()));
        favoritesCountLabel.setText(String.valueOf(favoriteProducts.size()));
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cart_view.fxml"));
            Parent root = loader.load();

            CartViewController controller = loader.getController();
            controller.setCartProducts(cartProducts);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Shopping Cart");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open cart view");
        }
    }

    @FXML
    private void handleViewFavorites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/favorites_view.fxml"));
            Parent root = loader.load();

            FavoritesViewController controller = loader.getController();
            controller.setFavoriteProducts(favoriteProducts);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Favorites");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open favorites view");
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