package controllers;

import Services.CategoryServices;
import entities.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import entities.Produit;
import Services.ProduitServices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class ListeProduitController {

    @FXML
    private TableColumn<Produit, String> descriptionColumn;

    @FXML
    private TextField descriptionField;

    @FXML
    private TableView<Produit> productsTable;

    @FXML
    private TableColumn<Produit, String> imageColumn;

    @FXML
    private TextField imageField;

    @FXML
    private TableColumn<Produit, String> nameColumn;

    @FXML
    private TableColumn<Produit, Double> priceColumn;

    @FXML
    private TextField priceField;

    @FXML
    private TableColumn<Produit, Integer> quantityColumn;

    @FXML
    private TextField quantityField;

    @FXML
    private TableColumn<Produit, Integer> idColumn;

    @FXML
    private TextField nameField;


    @FXML
    private ComboBox<Category> categoryCombo;


    private ProduitServices produitService = new ProduitServices();
    private final CategoryServices categoryService = new CategoryServices();


    public void initialize() throws SQLException {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        loadCategories();

        // Load data
        loadProducts();

        // Set up selection listener
        productsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });
    }

    private void loadCategories() throws SQLException {
        categoryCombo.setItems(FXCollections.observableArrayList(
                categoryService.showAll()
        ));
    }

    private void loadProducts() throws SQLException {
        List<Produit> produitList = produitService.showAll();
        ObservableList<Produit> observableProduitList = FXCollections.observableArrayList(produitList);
        productsTable.setItems(observableProduitList);
    }

    private void populateFields(Produit produit) {
        nameField.setText(produit.getName());
        descriptionField.setText(produit.getDescription());
        priceField.setText(String.valueOf(produit.getPrice()));
        quantityField.setText(String.valueOf(produit.getQuantity()));
        imageField.setText(produit.getImagePath());
    }

    @FXML
    void ajouterProduit(ActionEvent event) throws SQLException {
        if (!validateFields()) return;

        Produit produit = new Produit();
        populateProductFromFields(produit);

        int newId = produitService.insert(produit);
        produit.setId(newId);

        productsTable.getItems().add(produit);
        clearFields();

    }

    @FXML
    void modifierProduit(ActionEvent event) {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun produit sélectionné !");
            return;
        }

        if (!validateFields()) return;

        populateProductFromFields(selectedProduct);

        try {
            produitService.update(selectedProduct);
            productsTable.refresh();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour : " + e.getMessage());
        }
    }

    @FXML
    void supprimerProduit(ActionEvent event) {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun produit sélectionné !");
            return;
        }

        try {
            produitService.delete(selectedProduct);
            productsTable.getItems().remove(selectedProduct);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                priceField.getText().isEmpty() || quantityField.getText().isEmpty() ||
                imageField.getText().isEmpty() || categoryCombo.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Warning", "All fields must be filled!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Price must be positive!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Price must be a valid number!");
            return false;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Quantity must be positive!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Quantity must be a valid integer!");
            return false;
        }

        return true;
    }

    private void populateProductFromFields(Produit produit) {
        produit.setName(nameField.getText());
        produit.setDescription(descriptionField.getText());
        produit.setPrice(Double.parseDouble(priceField.getText()));
        produit.setQuantity(Integer.parseInt(quantityField.getText()));
        produit.setImagePath(imageField.getText());
        produit.setCategoryId(categoryCombo.getValue().getId()); // Get ID from selected category
    }



    @FXML
    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        quantityField.clear();
        imageField.clear();
        categoryCombo.getSelectionModel().clearSelection();

    }

    @FXML
    void choisirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                File destinationDir = new File("src/main/resources/images");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }

                File destinationFile = new File(destinationDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageField.setText(destinationFile.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer l'image : " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
