package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import entities.Produit;

import java.io.File;
import java.sql.SQLException;

public class ProductDetailsController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label quantityLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private ImageView productImage;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    private Produit currentProduct;

    public void setProductData(Produit product) {
        this.currentProduct = product;
        nameLabel.setText(product.getName());
        priceLabel.setText(String.format("%.2f TND", product.getPrice()));
        descriptionLabel.setText(product.getDescription());
        quantityLabel.setText("Quantity: " + product.getQuantity());
        categoryLabel.setText("Category ID: " + product.getCategoryId());

        // Load product image
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            File file = new File(product.getImagePath());
            if (file.exists()) {
                productImage.setImage(new Image(file.toURI().toString()));
            } else {
                // Set a default image if the specified image doesn't exist
                productImage.setImage(new Image(getClass().getResourceAsStream("/images/default-product.png")));
            }
        }
    }

    @FXML
    private void handleReservation() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Fields",
                    "Please fill all fields before reserving!");
            return;
        }

        // Here you would typically:
        // 1. Validate the input (email format, phone number format)
        // 2. Create a reservation record in the database
        // 3. Show success message

        showAlert(Alert.AlertType.INFORMATION, "Reservation Successful",
                "Thank you for your reservation!\nWe'll contact you soon.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}