package Services;

import Utils.DBConnection;
import entities.Produit;
import Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitServices {
    private Connection connection;

    public ProduitServices() {
        try {
            connection = DBConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    public int insert(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (category_id, name, description, price, image, quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, produit.getCategoryId());
            ps.setString(2, produit.getName());
            ps.setString(3, produit.getDescription());
            ps.setDouble(4, produit.getPrice());
            ps.setString(5, produit.getImagePath());
            ps.setInt(6, produit.getQuantity());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get inserted ID");
                }
            }
        }
    }

    public void update(Produit produit) throws SQLException {
        String query = "UPDATE produit SET category_id = ?, name = ?, description = ?, price = ?, image = ?, quantity = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, produit.getCategoryId());
            ps.setString(2, produit.getName());
            ps.setString(3, produit.getDescription());
            ps.setDouble(4, produit.getPrice());
            ps.setString(5, produit.getImagePath());
            ps.setInt(6, produit.getQuantity());
            ps.setInt(7, produit.getId());

            ps.executeUpdate();
        }
    }

    public void delete(Produit produit) throws SQLException {
        String query = "DELETE FROM produit WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, produit.getId());
            ps.executeUpdate();
        }
    }

    public List<Produit> showAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Produit produit = new Produit();
                produit.setId(resultSet.getInt("id"));
                produit.setCategoryId(resultSet.getInt("category_id"));
                produit.setName(resultSet.getString("name"));
                produit.setDescription(resultSet.getString("description"));
                produit.setPrice(resultSet.getDouble("price"));
                produit.setImagePath(resultSet.getString("image"));
                produit.setQuantity(resultSet.getInt("quantity"));

                produits.add(produit);
            }
        }
        return produits;
    }

    public Produit getOne(int id) throws SQLException {
        String query = "SELECT * FROM produit WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    Produit produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setCategoryId(resultSet.getInt("category_id"));
                    produit.setName(resultSet.getString("name"));
                    produit.setDescription(resultSet.getString("description"));
                    produit.setPrice(resultSet.getDouble("price"));
                    produit.setImagePath(resultSet.getString("image"));
                    produit.setQuantity(resultSet.getInt("quantity"));
                    return produit;
                }
            }
        }
        return null;
    }

    public List<Produit> getByCategory(int categoryId) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit WHERE category_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    Produit produit = new Produit();
                    produit.setId(resultSet.getInt("id"));
                    produit.setCategoryId(resultSet.getInt("category_id"));
                    produit.setName(resultSet.getString("name"));
                    produit.setDescription(resultSet.getString("description"));
                    produit.setPrice(resultSet.getDouble("price"));
                    produit.setImagePath(resultSet.getString("image"));
                    produit.setQuantity(resultSet.getInt("quantity"));

                    produits.add(produit);
                }
            }
        }
        return produits;
    }
}