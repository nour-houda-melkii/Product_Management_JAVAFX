package controllers;

import entities.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class FavoritesViewController {
    @FXML
    private FlowPane favoritesContainer;

    private List<Produit> favoriteProducts;

    public void setFavoriteProducts(List<Produit> favoriteProducts) {
        this.favoriteProducts = favoriteProducts;
        displayFavorites();
    }

    private void displayFavorites() {
        favoritesContainer.getChildren().clear();

        if (favoriteProducts.isEmpty()) {
            Label emptyLabel = new Label("You don't have any favorite products yet.");
            favoritesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Produit product : favoriteProducts) {
            VBox card = createFavoriteCard(product);
            favoritesContainer.getChildren().add(card);
        }
    }

    private VBox createFavoriteCard(Produit product) {
        // Similar to your product card but simplified for favorites
        VBox card = new VBox(10);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f8f8f8; -fx-border-radius: 5;");

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));

        card.getChildren().addAll(nameLabel, priceLabel);
        return card;
    }

    @FXML
    private void handleBackToProducts() {
        // Implement navigation back to products view
    }

    public void handleClose(ActionEvent actionEvent) {
    }
}