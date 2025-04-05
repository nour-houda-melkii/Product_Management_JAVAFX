package Services;

import Utils.DBConnection;
import entities.Produit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitServices implements CRUD<Produit> {
    private Connection con = DBConnection.getInstance().getCon();
    private Statement st;
    private PreparedStatement ps;
    private DataPersistenceService dataPersistenceService;

    public ProduitServices() {
        dataPersistenceService = new DataPersistenceService();
    }

    @Override
    public int insert(Produit produit) throws SQLException {
        String req = "INSERT INTO products (name, description, price, image_path, quantity, category_id) VALUES (?, ?, ?, ?, ?, ?)";
        ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, produit.getName());
        ps.setString(2, produit.getDescription());
        ps.setDouble(3, produit.getPrice());
        ps.setString(4, produit.getImagePath());
        ps.setInt(5, produit.getQuantity());
        ps.setInt(6, produit.getCategoryId());

        int affectedRows = ps.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Creating product failed, no rows affected.");
        }

        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int generatedId = generatedKeys.getInt(1);
                produit.setId(generatedId);

                // Record the addition in data history
                dataPersistenceService.recordProductAdded(produit);

                return generatedId;
            } else {
                throw new SQLException("Creating product failed, no ID obtained.");
            }
        }
    }

    @Override
    public int update(Produit produit) throws SQLException {
        // First, get the original product data before updating
        Produit oldProduct = getProductById(produit.getId());

        String req = "UPDATE products SET name = ?, description = ?, price = ?, image_path = ?, quantity = ?, category_id = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, produit.getName());
            ps.setString(2, produit.getDescription());
            ps.setDouble(3, produit.getPrice());
            ps.setString(4, produit.getImagePath());
            ps.setInt(5, produit.getQuantity());
            ps.setInt(6, produit.getCategoryId());
            ps.setInt(7, produit.getId());

            int result = ps.executeUpdate();

            // Record the update in data history
            if (result > 0 && oldProduct != null) {
                dataPersistenceService.recordProductUpdated(oldProduct, produit);
            }

            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(Produit produit) throws SQLException {
        String req = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, produit.getId());

            // Record the deletion before actually deleting
            dataPersistenceService.recordProductDeleted(produit);

            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Produit> showAll() throws SQLException {
        List<Produit> products = new ArrayList<>();
        String req = "SELECT * FROM products";
        st = con.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Produit produit = new Produit();
            produit.setId(rs.getInt("id"));
            produit.setName(rs.getString("name"));
            produit.setDescription(rs.getString("description"));
            produit.setPrice(rs.getDouble("price"));
            produit.setImagePath(rs.getString("image_path"));
            produit.setQuantity(rs.getInt("quantity"));
            produit.setCategoryId(rs.getInt("category_id"));

            products.add(produit);
        }

        return products;
    }

    public Produit getProductById(int id) throws SQLException {
        String req = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produit produit = new Produit();
                    produit.setId(rs.getInt("id"));
                    produit.setName(rs.getString("name"));
                    produit.setDescription(rs.getString("description"));
                    produit.setPrice(rs.getDouble("price"));
                    produit.setImagePath(rs.getString("image_path"));
                    produit.setQuantity(rs.getInt("quantity"));
                    produit.setCategoryId(rs.getInt("category_id"));
                    return produit;
                }
            }
        }
        return null;
    }

    public PreparedStatement getConnection() {
        return null;
    }
}