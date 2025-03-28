package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entities.Produit;
import Services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ProductFrontController {
    @FXML
    private FlowPane productContainer;

    private final ProduitServices produitService = new ProduitServices();

    @FXML
    public void initialize() throws SQLException {
        loadProducts();
    }

    private void loadProducts() throws SQLException {
        List<Produit> products = produitService.showAll();

        for (Produit product : products) {
            VBox card = createProductCard(product);
            productContainer.getChildren().add(card);
        }
    }

    private VBox createProductCard(Produit product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 10px; -fx-border-color: #ccc; -fx-border-radius: 10px; " +
                "-fx-background-color: white; -fx-alignment: center;");

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        loadProductImage(product, imageView);

        // Product Name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Product Price
        Label priceLabel = new Label(String.format("%.2f TND", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2e8b57;");

        // Product Quantity
        Label quantityLabel = new Label("In Stock: " + product.getQuantity());
        quantityLabel.setStyle("-fx-font-size: 14px;");

        // Details Button
        Button detailsButton = new Button("View Details");
        detailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        detailsButton.setOnAction(e -> openProductDetails(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, quantityLabel, detailsButton);
        return card;
    }

    private void loadProductImage(Produit product, ImageView imageView) {
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            File file = new File(product.getImagePath());
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
            } else {
                // Set default image if specified image doesn't exist
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default-product.png")));
            }
        }
    }

    private void openProductDetails(Produit product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_details.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(product.getName() + " Details");

            ProductDetailsController controller = loader.getController();
            controller.setProductData(product);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to open product details");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleMyReservation(ActionEvent actionEvent) {
        // Implementation for reservation handling
        // You might want to open a new window showing user's reservations
    }
}