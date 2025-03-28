package Tests;

import Utils.DBConnection;
import entities.Produit;
import Services.ProduitServices;
import java.sql.SQLException;

public class MainTest {
    public static void main(String[] args) {
        DBConnection db = DBConnection.getInstance();

        // Create a product that matches your database structure
        Produit p1 = new Produit(
                "Product Name",         // name
                "Product Description",  // description
                29.99,                 // price
                "images/product.jpg",   // imagePath
                100,                   // quantity
                1                      // categoryId
        );

        ProduitServices ps = new ProduitServices();

        try {
            // Test insertion
            int insertedId = ps.insert(p1);
            System.out.println("Inserted product with ID: " + insertedId);

            // Test retrieving all products
            System.out.println("All products:");
            ps.showAll().forEach(System.out::println);

            // Test deletion
            ps.delete(p1);
            System.out.println("Product deleted");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}