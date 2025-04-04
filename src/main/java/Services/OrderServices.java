package Services;

import entities.Order;
import entities.OrderItem;
import entities.Produit;
import Utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderServices {
    private Connection connection;

    public OrderServices() {
        connection = DBConnection.getInstance().getCnx();
    }
    // Save an order with its items
    public void save(Order order) throws SQLException {
        String orderSQL = "INSERT INTO orders (reference, customer_email, order_date, total_amount, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        String orderItemSQL = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) " +
                "VALUES (?, ?, ?, ?, ?)";

        // Use transaction to ensure all or nothing is saved
        connection.setAutoCommit(false);

        try {
            // Insert the order first
            PreparedStatement orderStmt = connection.prepareStatement(orderSQL, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, order.getReference());
            orderStmt.setString(2, order.getCustomerEmail());
            orderStmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
            orderStmt.setDouble(4, order.getTotalAmount());
            orderStmt.setString(5, order.getStatus());

            int affectedRows = orderStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            // Get the generated order ID
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int orderId = generatedKeys.getInt(1);
                order.setId(orderId);

                // Now insert each order item
                PreparedStatement itemStmt = connection.prepareStatement(orderItemSQL);

                for (OrderItem item : order.getItems()) {
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setString(3, item.getProductName());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setDouble(5, item.getUnitPrice());
                    itemStmt.addBatch();

                    // Update the order ID in the item
                    item.setOrderId(orderId);
                }

                itemStmt.executeBatch();
                itemStmt.close();
            }

            orderStmt.close();
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // Get daily sales statistics
    public Map<LocalDate, Integer> getDailySalesStats(int days) throws SQLException {
        Map<LocalDate, Integer> stats = new LinkedHashMap<>();

        String sql = "SELECT DATE(order_date) as sale_date, COUNT(oi.id) as total_products " +
                "FROM orders o JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE o.order_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY DATE(order_date) " +
                "ORDER BY sale_date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                int count = rs.getInt("total_products");
                stats.put(date, count);
            }
        }

        return stats;
    }

    // Get monthly sales statistics
    public Map<String, Integer> getMonthlySalesStats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String sql = "SELECT MONTH(order_date) as month, COUNT(oi.id) as total_products " +
                "FROM orders o JOIN order_items oi ON o.id = oi.order_id " +
                "WHERE YEAR(order_date) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(order_date) " +
                "ORDER BY month";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            // Initialize all months with 0 counts
            for (int i = 0; i < months.length; i++) {
                stats.put(months[i], 0);
            }

            // Update with actual data from database
            while (rs.next()) {
                int monthNum = rs.getInt("month");
                int count = rs.getInt("total_products");
                stats.put(months[monthNum-1], count);
            }
        }

        return stats;
    }

    // Get top selling products
    public Map<String, Integer> getTopSellingProducts(int limit) throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();

        String sql = "SELECT oi.product_name, SUM(oi.quantity) as total_sold " +
                "FROM order_items oi JOIN orders o ON oi.order_id = o.id " +
                "GROUP BY oi.product_id " +
                "ORDER BY total_sold DESC " +
                "LIMIT ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String productName = rs.getString("product_name");
                int count = rs.getInt("total_sold");
                stats.put(productName, count);
            }
        }

        return stats;
    }

    // Get all orders for a customer
    public List<Order> getOrdersByCustomer(String email) throws SQLException {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM orders WHERE customer_email = ? ORDER BY order_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Load order items
                loadOrderItems(order);
                orders.add(order);
            }
        }

        return orders;
    }

    // Get an order by its reference
    public Order getOrderByReference(String reference) throws SQLException {
        String sql = "SELECT * FROM orders WHERE reference = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reference);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Load order items
                loadOrderItems(order);
                return order;
            }
        }

        return null;
    }

    // Helper method to map ResultSet to Order
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String reference = rs.getString("reference");
        String customerEmail = rs.getString("customer_email");
        LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();
        double totalAmount = rs.getDouble("total_amount");
        String status = rs.getString("status");

        return new Order(id, reference, customerEmail, orderDate, totalAmount, status);
    }

    // Helper method to load order items
    private void loadOrderItems(Order order) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();

            List<OrderItem> items = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                int orderId = rs.getInt("order_id");
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");

                OrderItem item = new OrderItem(id, orderId, productId, productName, quantity, unitPrice);
                items.add(item);
            }

            order.setItems(items);
        }
    }
}