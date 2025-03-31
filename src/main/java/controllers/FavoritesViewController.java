package controllers;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesViewController {
    @FXML private TableView<Produit> favoritesTableView;
    @FXML private TableColumn<Produit, String> nameColumn;
    @FXML private TableColumn<Produit, Double> priceColumn;
    @FXML private TableColumn<Produit, String> descriptionColumn;
    @FXML private TableColumn<Produit, String> actionColumn;

    private List<Produit> favoriteProducts;
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

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        actionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        actionColumn.setCellFactory(col -> new TableButtonCell());
    }

    public void setFavoriteProducts(List<Produit> favoriteProducts) {
        this.favoriteProducts = favoriteProducts;
        favoritesTableView.setItems(FXCollections.observableArrayList(favoriteProducts));
    }

    private class TableButtonCell extends TableCell<Produit, String> {
        private final Button removeButton = new Button("Remove");

        TableButtonCell() {
            removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            removeButton.setOnAction(event -> {
                Produit product = getTableView().getItems().get(getIndex());
                favoriteProducts.removeIf(p -> p.getId() == product.getId());
                getTableView().getItems().remove(product);
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : removeButton);
        }
    }

    @FXML
    public void handleClose() {
        ((Stage) favoritesTableView.getScene().getWindow()).close();
    }
}