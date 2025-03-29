package Services;

import entities.Category;
import Utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryServices {
    private Connection con = DBConnection.getInstance().getCon();

    public int insert(Category category) throws SQLException {
        String req = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }
    public int update(Category category) throws SQLException {
        String req = "UPDATE categories SET name = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Category category) throws SQLException {
        String req = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, category.getId());
            return ps.executeUpdate();
        }
    }

    public List<Category> showAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String req = "SELECT * FROM categories";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        }
        return categories;
    }
    public Category getCategoryById(int id) throws SQLException {
        String req = "SELECT * FROM categories WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                }
            }
        }
        return null;
    }
}