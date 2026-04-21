package domain;

import java.util.List;

/**
 * Appointment repository interface — persistence abstraction.
 * REQ-2.3: Infrastructure implements this; domain only sees the interface.
 */
public interface IAppointmentRepository {
    void save(Appointment appointment);
    Appointment findById(String appointmentId);
    List<Appointment> findAll();
    List<Appointment> findByAdvisor(String advisorId);
    List<Appointment> findByStatus(AppointmentStatus status);
    void delete(String appointmentId);
}