package controllers;

import entities.Produit;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartViewController {

    @FXML
    private TableView<Produit> cartTableView;

    @FXML
    private TableColumn<Produit, String> nameColumn;

    @FXML
    private TableColumn<Produit, Double> priceColumn;

    @FXML
    private TableColumn<Produit, Integer> quantityColumn;

    @FXML
    private TableColumn<Produit, Double> totalColumn;

    @FXML
    private TableColumn<Produit, String> actionColumn;

    @FXML
    private Label totalAmountLabel;

    private List<Produit> cartProducts;
    private Map<Integer, Integer> productQuantities = new HashMap<>();

    @FXML
    public void initialize() {
        configureTableColumns();
    }

    private void configureTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // For quantity, we need to use a custom cell value factory
        quantityColumn.setCellValueFactory(cellData -> {
            int productId = cellData.getValue().getId();
            int quantity = productQuantities.getOrDefault(productId, 1);
            return new SimpleIntegerProperty(quantity).asObject();
        });

        // Calculate total for each product
        totalColumn.setCellValueFactory(cellData -> {
            Produit product = cellData.getValue();
            int quantity = productQuantities.getOrDefault(product.getId(), 1);
            double total = product.getPrice() * quantity;
            return new SimpleDoubleProperty(total).asObject();
        });

        // Add action buttons to each row
        actionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        actionColumn.setCellFactory(col -> new TableButtonCell());
    }

    public void setCartProducts(List<Produit> cartProducts) {
        this.cartProducts = cartProducts;

        // Initialize quantities for each product (count duplicates)
        productQuantities.clear();
        for (Produit product : cartProducts) {
            int id = product.getId();
            productQuantities.put(id, productQuantities.getOrDefault(id, 0) + 1);
        }

        // Convert to a set to remove duplicates when displaying
        cartTableView.setItems(FXCollections.observableArrayList(
                cartProducts.stream().distinct().toList()
        ));

        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (Produit product : cartProducts) {
            total += product.getPrice();
        }
        totalAmountLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleCheckout() {
        if (cartProducts.isEmpty()) {
            showAlert("Cart Empty", "Your cart is empty. Add some products first!");
            return;
        }

        showAlert("Checkout", "Thank you for your purchase!\nTotal amount: " + totalAmountLabel.getText());

        // Clear the cart
        cartProducts.clear();
        cartTableView.getItems().clear();
        updateTotal();
    }

    @FXML
    private void handleClearCart() {
        cartProducts.clear();
        cartTableView.getItems().clear();
        updateTotal();
    }

    @FXML
    private void handleClose() {
        ((Stage) cartTableView.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Custom cell factory for the action column
    private class TableButtonCell extends javafx.scene.control.TableCell<Produit, String> {
        private final Button removeButton = new Button("Remove");

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
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(removeButton);
            }
        }
    }
}