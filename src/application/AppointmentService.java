package application;

import domain.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AppointmentService — application layer orchestration.
 * REQ-2.2: No direct IO or DB calls here.
 */
public class AppointmentService {
    private final IAppointmentRepository repo;
    private final EventBus eventBus;

    public AppointmentService(IAppointmentRepository repo, EventBus eventBus) {
        this.repo     = repo;
        this.eventBus = eventBus;
    }

    // ── Schedule appointment ──────────────────────────────────────────────
    public Appointment scheduleAppointment(String customerId, String advisorId,
                                           String vehicleId, LocalDateTime startTime,
                                           int durationMinutes, String serviceType) {
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Appointment newAppt = new Appointment(id, customerId, advisorId,
            vehicleId, startTime, durationMinutes, serviceType);

        // Check for time conflicts — REQ-2.8
        List<Appointment> existing = repo.findByAdvisor(advisorId);
        for (Appointment a : existing) {
            if (a.getStatus() == AppointmentStatus.SCHEDULED
             || a.getStatus() == AppointmentStatus.CONFIRMED) {
                if (newAppt.overlapsWith(a)) {
                    throw new IllegalArgumentException(
                        "[REQ-2.8] TIME CONFLICT: Advisor '" + advisorId
                        + "' already has an appointment overlapping "
                        + startTime + ". E-TIME-CONFLICT.");
                }
            }
        }

        repo.save(newAppt);
        eventBus.publish("APPOINTMENT_SCHEDULED",
            "Customer:" + customerId + " with Advisor:" + advisorId
            + " at " + startTime);
        return newAppt;
    }

    // ── Confirm ───────────────────────────────────────────────────────────
    public void confirmAppointment(String appointmentId) {
        Appointment a = getOrThrow(appointmentId);
        a.transitionTo(AppointmentStatus.CONFIRMED);
        repo.save(a);
        eventBus.publish("APPOINTMENT_CONFIRMED", "Appointment " + appointmentId);
    }

    // ── Cancel ────────────────────────────────────────────────────────────
    public void cancelAppointment(String appointmentId) {
        Appointment a = getOrThrow(appointmentId);
        a.transitionTo(AppointmentStatus.CANCELLED);
        repo.save(a);
        eventBus.publish("APPOINTMENT_CANCELLED", "Appointment " + appointmentId);
    }

    // ── Complete ──────────────────────────────────────────────────────────
    public void completeAppointment(String appointmentId) {
        Appointment a = getOrThrow(appointmentId);
        a.transitionTo(AppointmentStatus.COMPLETED);
        repo.save(a);
        eventBus.publish("APPOINTMENT_COMPLETED", "Appointment " + appointmentId);
    }

    // ── Queries ───────────────────────────────────────────────────────────
    public List<Appointment> getAllAppointments() {
        return repo.findAll();
    }

    public List<Appointment> filterByStatus(AppointmentStatus status) {
        return repo.findByStatus(status);
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private Appointment getOrThrow(String id) {
        Appointment a = repo.findById(id);
        if (a == null) {
            throw new IllegalArgumentException(
                "[REQ-2.8] NOT FOUND: No appointment with ID '" + id + "'.");
        }
        return a;
    }
}