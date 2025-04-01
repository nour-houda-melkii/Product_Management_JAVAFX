package controllers;

import Services.CategoryServices;
import Services.AIDescriptionService;
import entities.Category;
import entities.Produit;
import Services.ProduitServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    @FXML
    private TableColumn<Produit, Integer> categoryColumn;

    @FXML
    private Button generateDescriptionButton;

    private ProduitServices produitService = new ProduitServices();
    private final CategoryServices categoryService = new CategoryServices();
    private final AIDescriptionService aiService = new AIDescriptionService();

    private Produit productToEdit = null;

    public void initialize() throws SQLException {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imagePath"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

        loadCategories();

        // Load data
        loadProducts();

        // Set up selection listener
        productsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                populateFields(newValue);
            }
        });

        // Add listener to name field to enable generate button only when name is entered
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            generateDescriptionButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });

        // Initially disable the generate button
        generateDescriptionButton.setDisable(true);
    }

    @FXML
    private void generateAIDescription() {
        String productName = nameField.getText().trim();
        if (!productName.isEmpty()) {
            // Show loading indicator
            descriptionField.setText("Generating description...");

            // In a real application, you would want to do this in a background thread
            // to avoid freezing the UI during API calls
            new Thread(() -> {
                String generatedDescription = aiService.generateProductDescription(productName);

                // Update UI on the JavaFX application thread
                javafx.application.Platform.runLater(() -> {
                    descriptionField.setText(generatedDescription);
                });
            }).start();
        }
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryService.showAll();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));

        // Set up a cell factory to display category names instead of toString()
        categoryCombo.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getName());
                }
            }
        });

        // Same for the displayed value
        categoryCombo.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category.getName());
                }
            }
        });
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

        // Set the category in the combo box
        try {
            for (Category category : categoryCombo.getItems()) {
                if (category.getId() == produit.getCategoryId()) {
                    categoryCombo.setValue(category);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set this as the current product being edited
        this.productToEdit = produit;
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

        showSuccessAlert("Success", "Product added successfully!");
    }

    @FXML
    void modifierProduit(ActionEvent event) {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null && productToEdit == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No product selected!");
            return;
        }

        // Use either the selected product or the product passed from another screen
        Produit productToUpdate = (selectedProduct != null) ? selectedProduct : productToEdit;

        if (!validateFields()) return;

        populateProductFromFields(productToUpdate);

        try {
            produitService.update(productToUpdate);
            loadProducts(); // Refresh the table
            showSuccessAlert("Success", "Product updated successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update: " + e.getMessage());
        }
    }

    @FXML
    void supprimerProduit(ActionEvent event) {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No product selected!");
            return;
        }

        try {
            produitService.delete(selectedProduct);
            productsTable.getItems().remove(selectedProduct);
            clearFields();
            showSuccessAlert("Success", "Product deleted successfully!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
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
        produit.setCategoryId(categoryCombo.getValue().getId());
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        quantityField.clear();
        imageField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        this.productToEdit = null;
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
                // Create the target directory if it doesn't exist
                File targetDir = new File("src/main/resources/images");
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                // Copy the file to the target directory
                File destFile = new File(targetDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Set the relative path in the image field
                imageField.setText("src/main/resources/images/" + selectedFile.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to copy image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setProductToEdit(Produit product) {
        this.productToEdit = product;
        if (product != null) {
            populateFields(product);
        }
    }

    public void handleBackToAdmin(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_dashboard.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}