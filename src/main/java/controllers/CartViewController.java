package controllers;

import entities.Produit;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class CartViewController {

    @FXML private TableView<Produit> cartTableView;
    @FXML private TableColumn<Produit, String> nameColumn;
    @FXML private TableColumn<Produit, Double> priceColumn;
    @FXML private TableColumn<Produit, Integer> quantityColumn;
    @FXML private TableColumn<Produit, Double> totalColumn;
    @FXML private TableColumn<Produit, String> actionColumn;
    @FXML private Label totalAmountLabel;
    @FXML private TextField emailField;

    private List<Produit> cartProducts;
    private Map<Integer, Integer> productQuantities = new HashMap<>();
    private String orderReference;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        configureTableColumns();
    }

    private void configureTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<Produit, Double>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : currencyFormat.format(price));
            }
        });

        quantityColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getId();
            int quantity = productQuantities.getOrDefault(productId, 1);
            return new SimpleIntegerProperty(quantity).asObject();
        });

        totalColumn.setCellValueFactory(cellData -> {
            Produit product = cellData.getValue();
            int quantity = productQuantities.getOrDefault(product.getId(), 1);
            return new SimpleDoubleProperty(product.getPrice() * quantity).asObject();
        });
        totalColumn.setCellFactory(column -> new TableCell<Produit, Double>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? "" : currencyFormat.format(total));
            }
        });

        actionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        actionColumn.setCellFactory(col -> new TableButtonCell());
    }


    private void updateTotal() {
        double total = 0;
        for (Produit product : cartProducts) {
            int quantity = productQuantities.getOrDefault(product.getId(), 1);
            total += product.getPrice() * quantity;
        }
        totalAmountLabel.setText(currencyFormat.format(total));
    }


    @FXML
    private void handleCheckout() {
        if (cartProducts.isEmpty()) {
            showAlert("Cart Empty", "Your cart is empty. Add some products first!");
            return;
        }

        if (emailField == null || emailField.getText().isEmpty() || !isValidEmail(emailField.getText())) {
            showAlert("Invalid Email", "Please enter a valid email address to continue.");
            return;
        }

        // Send confirmation email
        boolean emailSent = sendConfirmationEmail(emailField.getText());

        if (emailSent) {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Order Confirmed");
            alert.setHeaderText("Thank you for your purchase!");
            alert.setContentText("Order Reference: " + orderReference + "\nTotal amount: " + totalAmountLabel.getText() +
                    "\nA confirmation email has been sent to " + emailField.getText());
            alert.showAndWait();

            // Show QR code
            showQRCode();

            // Clear the cart
            cartProducts.clear();
            cartTableView.getItems().clear();
            updateTotal();

            // Close this window
            ((Stage) cartTableView.getScene().getWindow()).close();
        } else {
            showAlert("Email Error", "Could not send confirmation email. Please try again.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean sendConfirmationEmail(String email) {
        // This is a placeholder for actual email sending
        // In a real application, you would implement SMTP email sending here
        try {
            // For demonstration purposes, we'll just simulate success
            // In production code, you would use JavaMail API to send actual emails

            System.out.println("Sending confirmation email to: " + email);
            System.out.println("Order reference: " + orderReference);
            System.out.println("Total amount: " + totalAmountLabel.getText());

            // List products in the order
            for (Produit product : cartTableView.getItems()) {
                int quantity = productQuantities.getOrDefault(product.getId(), 1);
                System.out.println(" - " + product.getName() + " x " + quantity +
                        " = $" + (product.getPrice() * quantity));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void showQRCode() {
        try {
            StringBuilder qrContent = new StringBuilder();
            qrContent.append("Order: ").append(orderReference).append("\n");
            qrContent.append("Total: ").append(totalAmountLabel.getText()).append("\n");
            qrContent.append("Products:\n");

            for (Produit product : cartTableView.getItems()) {
                int quantity = productQuantities.getOrDefault(product.getId(), 1);
                qrContent.append(" - ")
                        .append(product.getName())
                        .append(" x ").append(quantity)
                        .append(" = ").append(currencyFormat.format(product.getPrice() * quantity))
                        .append("\n");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/qr_code_view.fxml"));
            Parent root = loader.load();

            QRCodeViewController controller = loader.getController();
            controller.generateQRCode(qrContent.toString());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Purchase QR Code");
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate QR code: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void setCartProducts(List<Produit> cartProducts) {
        this.cartProducts = cartProducts;
        cartTableView.setItems(FXCollections.observableArrayList(cartProducts));
        updateTotal();
    }
    private class TableButtonCell extends TableCell<Produit, String> {
        private final Button removeButton = new Button("Remove");

        private void updateTotal() {
            double total = 0;
            for (Produit product : cartProducts) {
                int quantity = productQuantities.getOrDefault(product.getId(), 1);
                total += product.getPrice() * quantity;
            }
            totalAmountLabel.setText(currencyFormat.format(total));
        }

        TableButtonCell() {
            removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            removeButton.setOnAction(event -> {
                Produit product = getTableView().getItems().get(getIndex());
                cartProducts.removeIf(p -> p.getId() == product.getId());
                getTableView().getItems().remove(product);
                updateTotal();
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : removeButton);
        }
    }

    @FXML
    private void handleClearCart() {
        cartProducts.clear();
        productQuantities.clear();
        cartTableView.getItems().clear();
        updateTotal();
    }

    @FXML
    private void handleClose() {
        ((Stage) totalAmountLabel.getScene().getWindow()).close();
    }


    @FXML
    private void handleViewCart() {
        try {
            // Get the resource URL to verify it exists
            java.net.URL resourceUrl = getClass().getResource("/cart_view.fxml");

            if (resourceUrl == null) {
                showAlert("Error", "Could not find resource: /cart_view.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            CartViewController controller = loader.getController();
            controller.setCartProducts(cartProducts);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Shopping Cart");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open cart view: " + e.getMessage() +
                    "\nResource URL: " + getClass().getResource("/cart_view.fxml"));
        }
    }
}