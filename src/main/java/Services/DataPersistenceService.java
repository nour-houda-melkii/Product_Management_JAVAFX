package Services;

import entities.Produit;
import entities.Category;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for persisting data operations history to a file
 * This keeps track of all product and category operations even after deletion
 */
public class DataPersistenceService {
    private static final String DATA_FILE_PATH = "data_history.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Records a product addition operation
     * @param product The product that was added
     */
    public void recordProductAdded(Produit product) {
        String entry = String.format("[%s] PRODUCT ADDED: ID=%d, Name=%s, Description=%s, Price=%.2f, Quantity=%d, CategoryID=%d, ImagePath=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategoryId(),
                product.getImagePath());
        appendToFile(entry);
    }

    /**
     * Records a product update operation
     * @param oldProduct The product before updating
     * @param newProduct The product after updating
     */
    public void recordProductUpdated(Produit oldProduct, Produit newProduct) {
        String entry = String.format("[%s] PRODUCT UPDATED: ID=%d\n\tOLD: Name=%s, Description=%s, Price=%.2f, Quantity=%d, CategoryID=%d, ImagePath=%s\n\tNEW: Name=%s, Description=%s, Price=%.2f, Quantity=%d, CategoryID=%d, ImagePath=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                newProduct.getId(),
                oldProduct.getName(), oldProduct.getDescription(), oldProduct.getPrice(), oldProduct.getQuantity(), oldProduct.getCategoryId(), oldProduct.getImagePath(),
                newProduct.getName(), newProduct.getDescription(), newProduct.getPrice(), newProduct.getQuantity(), newProduct.getCategoryId(), newProduct.getImagePath());
        appendToFile(entry);
    }

    /**
     * Records a product deletion operation
     * @param product The product that was deleted
     */
    public void recordProductDeleted(Produit product) {
        String entry = String.format("[%s] PRODUCT DELETED: ID=%d, Name=%s, Description=%s, Price=%.2f, Quantity=%d, CategoryID=%d, ImagePath=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategoryId(),
                product.getImagePath());
        appendToFile(entry);
    }

    /**
     * Records a category addition operation
     * @param category The category that was added
     */
    public void recordCategoryAdded(Category category) {
        String entry = String.format("[%s] CATEGORY ADDED: ID=%d, Name=%s, Description=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                category.getId(),
                category.getName(),
                category.getDescription());
        appendToFile(entry);
    }

    /**
     * Records a category update operation
     * @param oldCategory The category before updating
     * @param newCategory The category after updating
     */
    public void recordCategoryUpdated(Category oldCategory, Category newCategory) {
        String entry = String.format("[%s] CATEGORY UPDATED: ID=%d\n\tOLD: Name=%s, Description=%s\n\tNEW: Name=%s, Description=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                newCategory.getId(),
                oldCategory.getName(), oldCategory.getDescription(),
                newCategory.getName(), newCategory.getDescription());
        appendToFile(entry);
    }

    /**
     * Records a category deletion operation
     * @param category The category that was deleted
     */
    public void recordCategoryDeleted(Category category) {
        String entry = String.format("[%s] CATEGORY DELETED: ID=%d, Name=%s, Description=%s",
                LocalDateTime.now().format(DATE_FORMATTER),
                category.getId(),
                category.getName(),
                category.getDescription());
        appendToFile(entry);
    }

    /**
     * Retrieves all data history entries
     * @return List of history entries
     */
    public List<String> getAllHistoryEntries() {
        List<String> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading data history file: " + e.getMessage());
        }
        return entries;
    }

    /**
     * Appends an entry to the data history file
     * @param entry The entry to append
     */
    private void appendToFile(String entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE_PATH, true))) {
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to data history file: " + e.getMessage());
        }
    }
}