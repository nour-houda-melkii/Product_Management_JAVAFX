package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database connection parameters (using default values if .env not available)
    private final String URL;
    private final String USER;
    private final String PWD;

    private static DBConnection instance;
    private Connection con;

    private DBConnection() {
        // Try to load from environment variables or use defaults
        this.URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/produitjavafx";
        this.USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
        this.PWD = System.getenv("DB_PWD") != null ? System.getenv("DB_PWD") : "";

        try {
            con = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connection successful");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getCon() {
        return con;
    }

    public Connection getCnx() {
        return null;
    }
}