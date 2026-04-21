package infrastructure;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection — Singleton pattern (creational).
 * REQ-2.7: Exactly one shared SQLite connection for the app lifetime.
 * Trade-off: easy global access but harder to swap in tests.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:automanage.db";

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initSchema() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS vehicles (
                vehicleId TEXT PRIMARY KEY,
                vin TEXT UNIQUE NOT NULL,
                make TEXT NOT NULL,
                model TEXT NOT NULL,
                year INTEGER NOT NULL,
                mileage INTEGER NOT NULL,
                price REAL NOT NULL,
                category TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS appointments (
                appointmentId TEXT PRIMARY KEY,
                customerId TEXT NOT NULL,
                advisorId TEXT NOT NULL,
                vehicleId TEXT,
                startTime TEXT NOT NULL,
                durationMinutes INTEGER NOT NULL,
                serviceType TEXT NOT NULL,
                status TEXT NOT NULL
            )
        """);

        // Index for performance — REQ-2.9
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_status ON vehicles(status)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_vehicle_make ON vehicles(make)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_appt_advisor ON appointments(advisorId)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_appt_status ON appointments(status)");

        stmt.close();
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}