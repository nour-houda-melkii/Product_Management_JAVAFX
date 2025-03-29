package controllers;

import entities.Category;
import Services.CategoryServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class CategoryController {
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TextField nameField;

    private final CategoryServices categoryService = new CategoryServices();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();


    @FXML
    public void initialize() throws SQLException {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        loadCategories();
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
    private void addCategory() throws SQLException {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Category name cannot be empty");
            return;
        }

        if (name.length() > 100) {  // Assuming your DB column is VARCHAR(100)
            showAlert("Error", "Category name is too long (max 100 characters)");
            return;
        }

        Category category = new Category(nameField.getText());
        categoryService.insert(category);
        loadCategories();
        nameField.clear();
    }

    @FXML
    private void updateCategory() throws SQLException {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null || nameField.getText().isEmpty()) {
            showAlert("Error", "Please select a category and enter a name");
            return;
        }

        selected.setName(nameField.getText());
        categoryService.update(selected);
        loadCategories();
    }

    @FXML
    private void deleteCategory() throws SQLException {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a category to delete");
            return;
        }

        categoryService.delete(selected);
        loadCategories();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}