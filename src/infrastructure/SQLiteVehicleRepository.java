package infrastructure;

import domain.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteVehicleRepository — implements IVehicleRepository.
 * REQ-2.3: Infrastructure layer; domain never touches this directly.
 * REQ-2.7: Repository is the structural pattern (abstraction boundary).
 */
public class SQLiteVehicleRepository implements IVehicleRepository {
    private final Connection conn;

    public SQLiteVehicleRepository(DatabaseConnection db) {
        this.conn = db.getConnection();
    }

    @Override
    public void save(Vehicle v) {
        String sql = """
            INSERT INTO vehicles (vehicleId, vin, make, model, year, mileage, price, category, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(vehicleId) DO UPDATE SET
                vin=excluded.vin, make=excluded.make, model=excluded.model,
                year=excluded.year, mileage=excluded.mileage, price=excluded.price,
                category=excluded.category, status=excluded.status
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getVehicleId());
            ps.setString(2, v.getVin());
            ps.setString(3, v.getMake());
            ps.setString(4, v.getModel());
            ps.setInt(5, v.getYear());
            ps.setInt(6, v.getMileage());
            ps.setDouble(7, v.getPrice());
            ps.setString(8, v.getCategory());
            ps.setString(9, v.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving vehicle: " + e.getMessage());
        }
    }

    @Override
    public Vehicle findById(String vehicleId) {
        String sql = "SELECT * FROM vehicles WHERE vehicleId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicle: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Vehicle findByVin(String vin) {
        String sql = "SELECT * FROM vehicles WHERE vin = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vin.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding vehicle by VIN: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Vehicle> findAll() {
        return query("SELECT * FROM vehicles", null);
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return query("SELECT * FROM vehicles WHERE status = ?", status.name());
    }

    @Override
    public List<Vehicle> findByMake(String make) {
        String sql = "SELECT * FROM vehicles WHERE LOWER(make) LIKE LOWER(?)";
        List<Vehicle> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + make + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error searching vehicles: " + e.getMessage());
        }
        return results;
    }

    @Override
    public void delete(String vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicleId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting vehicle: " + e.getMessage());
        }
    }

    private List<Vehicle> query(String sql, String param) {
        List<Vehicle> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error querying vehicles: " + e.getMessage());
        }
        return results;
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle(
            rs.getString("vehicleId"),
            rs.getString("vin"),
            rs.getString("make"),
            rs.getString("model"),
            rs.getInt("year"),
            rs.getInt("mileage"),
            rs.getDouble("price"),
            rs.getString("category")
        );
        v.setStatus(VehicleStatus.valueOf(rs.getString("status")));
        return v;
    }
}