package Services;

import entities.Category;
import Utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryServices {
    private Connection con = DBConnection.getInstance().getCon();
    private DataPersistenceService dataPersistenceService;

    public CategoryServices() {
        dataPersistenceService = new DataPersistenceService();
    }

    public int insert(Category category) throws SQLException {
        String req = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    category.setId(generatedId);

                    // Record the addition in data history
                    dataPersistenceService.recordCategoryAdded(category);

                    return generatedId;
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
    }

    public int update(Category category) throws SQLException {
        // Get the old category data before updating
        Category oldCategory = getCategoryById(category.getId());

        String req = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());

            int result = ps.executeUpdate();

            // Record the update in data history
            if (result > 0 && oldCategory != null) {
                dataPersistenceService.recordCategoryUpdated(oldCategory, category);
            }

            return result;
        }
    }

    public int delete(Category category) throws SQLException {
        String req = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, category.getId());

            // Record the deletion before actually deleting
            dataPersistenceService.recordCategoryDeleted(category);

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
                        rs.getString("name"),
                        rs.getString("description")
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
                            rs.getString("name"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }
}