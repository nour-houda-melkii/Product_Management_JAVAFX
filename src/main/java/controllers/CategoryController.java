package controllers;

import entities.Category;
import Services.CategoryServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class CategoryController {
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Number> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TextField nameField;

    private final CategoryServices categoryService = new CategoryServices();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Load data
        try {
            loadCategories();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }

        // Set up selection listener
        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        nameField.setText(newSelection.getName());
                    }
                });
    }

    private void loadCategories() throws SQLException {
        categories.setAll(categoryService.showAll());
        categoryTable.setItems(categories);
    }

    @FXML
    private void addCategory() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Input Error", "Category name cannot be empty");
            return;
        }

        if (name.length() > 100) {
            showAlert("Input Error", "Category name is too long (max 100 characters)");
            return;
        }

        try {
            Category category = new Category(name);
            categoryService.insert(category);
            loadCategories();
            nameField.clear();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add category: " + e.getMessage());
        }
    }

    @FXML
    private void updateCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a category to update");
            return;
        }

        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            showAlert("Input Error", "Category name cannot be empty");
            return;
        }

        try {
            selected.setName(newName);
            categoryService.update(selected);
            loadCategories();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update category: " + e.getMessage());
        }
    }

    @FXML
    private void deleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a category to delete");
            return;
        }

        try {
            categoryService.delete(selected);
            loadCategories();
            nameField.clear();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to delete category: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}