package infrastructure;

import domain.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteAppointmentRepository — implements IAppointmentRepository.
 * REQ-2.3: Infrastructure layer only.
 */
public class SQLiteAppointmentRepository implements IAppointmentRepository {
    private final Connection conn;

    public SQLiteAppointmentRepository(DatabaseConnection db) {
        this.conn = db.getConnection();
    }

    @Override
    public void save(Appointment a) {
        String sql = """
            INSERT INTO appointments
            (appointmentId, customerId, advisorId, vehicleId, startTime, durationMinutes, serviceType, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(appointmentId) DO UPDATE SET
                status=excluded.status,
                startTime=excluded.startTime,
                durationMinutes=excluded.durationMinutes
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAppointmentId());
            ps.setString(2, a.getCustomerId());
            ps.setString(3, a.getAdvisorId());
            ps.setString(4, a.getVehicleId());
            ps.setString(5, a.getStartTime().toString());
            ps.setInt(6, a.getDurationMinutes());
            ps.setString(7, a.getServiceType());
            ps.setString(8, a.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving appointment: " + e.getMessage());
        }
    }

    @Override
    public Appointment findById(String appointmentId) {
        String sql = "SELECT * FROM appointments WHERE appointmentId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding appointment: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        return query("SELECT * FROM appointments", null);
    }

    @Override
    public List<Appointment> findByAdvisor(String advisorId) {
        return query("SELECT * FROM appointments WHERE advisorId = ?", advisorId);
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return query("SELECT * FROM appointments WHERE status = ?", status.name());
    }

    @Override
    public void delete(String appointmentId) {
        String sql = "DELETE FROM appointments WHERE appointmentId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting appointment: " + e.getMessage());
        }
    }

    private List<Appointment> query(String sql, String param) {
        List<Appointment> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error querying appointments: " + e.getMessage());
        }
        return results;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment(
            rs.getString("appointmentId"),
            rs.getString("customerId"),
            rs.getString("advisorId"),
            rs.getString("vehicleId"),
            LocalDateTime.parse(rs.getString("startTime")),
            rs.getInt("durationMinutes"),
            rs.getString("serviceType")
        );
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        return a;
    }
}