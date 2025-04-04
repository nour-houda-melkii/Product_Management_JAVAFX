package controllers;

import entities.Produit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StatisticsViewController {

    @FXML private LineChart<String, Number> dailySalesChart;
    @FXML private CategoryAxis dailyXAxis;
    @FXML private NumberAxis dailyYAxis;
    @FXML private BarChart<String, Number> monthlySalesChart;
    @FXML private PieChart favoriteProductsChart;

    // Database connection
    private Connection connection;

    @FXML
    public void initialize() {
        // Initialize database connection
        try {
            // You should use your actual database connection here
            // connection = YourDatabaseConnectionClass.getConnection();

            // For now, we'll use mock data for demonstration
            loadMockData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMockData() {
        // Load daily sales chart
        loadDailySalesData();

        // Load monthly sales chart
        loadMonthlySalesData();

        // Load favorite products chart
        loadFavoriteProductsData();
    }

    // In a real implementation, this would fetch data from your database
    private void loadDailySalesData() {
        // Create a series for the line chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Products Sold");

        // Create sample data for the last 7 days
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String formattedDate = date.format(formatter);

            // Mock data - in reality, this would be fetched from your database
            int productsSold = getRandomSalesNumber(5, 25);

            series.getData().add(new XYChart.Data<>(formattedDate, productsSold));
        }

        dailySalesChart.getData().add(series);
    }

    private void loadMonthlySalesData() {
        // Create a series for the bar chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Products Sold");

        // Month names
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Create sample data for months
        for (String month : months) {
            // Mock data - in reality, this would be fetched from your database
            int productsSold = getRandomSalesNumber(30, 100);

            series.getData().add(new XYChart.Data<>(month, productsSold));
        }

        monthlySalesChart.getData().add(series);
    }

    private void loadFavoriteProductsData() {
        // Sample product names and their purchase counts
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        pieChartData.add(new PieChart.Data("Smartphone", 25));
        pieChartData.add(new PieChart.Data("Laptop", 20));
        pieChartData.add(new PieChart.Data("Tablet", 15));
        pieChartData.add(new PieChart.Data("Headphones", 10));
        pieChartData.add(new PieChart.Data("Smartwatch", 8));

        favoriteProductsChart.setData(pieChartData);
    }

    /**
     * In a real implementation, you would have methods like these to fetch actual data:
     */

    // Example method to get daily sales data from the database
    private void loadActualDailySalesData() {
        if (connection == null) return;

        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Products Sold");

            // SQL to get daily sales for the last 7 days
            String sql = "SELECT DATE(order_date) as sale_date, COUNT(*) as total_products " +
                    "FROM orders JOIN order_items ON orders.id = order_items.order_id " +
                    "WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                    "GROUP BY DATE(order_date) " +
                    "ORDER BY sale_date";

            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

            while (rs.next()) {
                String date = LocalDate.parse(rs.getString("sale_date"))
                        .format(formatter);
                int count = rs.getInt("total_products");

                series.getData().add(new XYChart.Data<>(date, count));
            }

            dailySalesChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Example method to get monthly sales data from the database
    private void loadActualMonthlySalesData() {
        if (connection == null) return;

        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Products Sold");

            // SQL to get monthly sales for the current year
            String sql = "SELECT MONTH(order_date) as month, COUNT(*) as total_products " +
                    "FROM orders JOIN order_items ON orders.id = order_items.order_id " +
                    "WHERE YEAR(order_date) = YEAR(CURDATE()) " +
                    "GROUP BY MONTH(order_date) " +
                    "ORDER BY month";

            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

            while (rs.next()) {
                int monthNum = rs.getInt("month");
                int count = rs.getInt("total_products");

                series.getData().add(new XYChart.Data<>(months[monthNum-1], count));
            }

            monthlySalesChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Example method to get favorite products data from the database
    private void loadActualFavoriteProductsData() {
        if (connection == null) return;

        try {
            // SQL to get top 5 products by sales
            String sql = "SELECT p.name, COUNT(*) as purchase_count " +
                    "FROM orders o " +
                    "JOIN order_items oi ON o.id = oi.order_id " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "GROUP BY p.id " +
                    "ORDER BY purchase_count DESC " +
                    "LIMIT 5";

            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            while (rs.next()) {
                String productName = rs.getString("name");
                int count = rs.getInt("purchase_count");

                pieChartData.add(new PieChart.Data(productName, count));
            }

            favoriteProductsChart.setData(pieChartData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to generate random numbers for demo data
    private int getRandomSalesNumber(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    @FXML
    private void handleClose() {
        // Close the window
        ((Stage) dailySalesChart.getScene().getWindow()).close();
    }
}