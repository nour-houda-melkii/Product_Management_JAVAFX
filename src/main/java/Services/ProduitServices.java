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
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating product failed, no ID obtained.");
            }
        }
    }

    @Override
    public int update(Produit produit) throws SQLException {
        String req = "UPDATE products SET name = ?, description = ?, price = ?, image_path = ?, quantity = ?, category_id = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, produit.getName());
            ps.setString(2, produit.getDescription());
            ps.setDouble(3, produit.getPrice());
            ps.setString(4, produit.getImagePath());
            ps.setInt(5, produit.getQuantity());
            ps.setInt(6, produit.getCategoryId());
            ps.setInt(7, produit.getId());

            return ps.executeUpdate();
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
}