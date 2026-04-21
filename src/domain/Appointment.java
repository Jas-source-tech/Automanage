package domain;

import java.time.LocalDateTime;

/**
 * Domain entity representing a scheduled appointment.
 * Owns its own state machine. No IO here.
 */
public class Appointment {
    private String appointmentId;
    private String customerId;
    private String advisorId;
    private String vehicleId; // optional
    private LocalDateTime startTime;
    private int durationMinutes;
    private String serviceType;
    private AppointmentStatus status;

    public Appointment(String appointmentId, String customerId, String advisorId,
                       String vehicleId, LocalDateTime startTime,
                       int durationMinutes, String serviceType) {
        validateStartTime(startTime);
        validateDuration(durationMinutes);
        this.appointmentId   = appointmentId;
        this.customerId      = customerId;
        this.advisorId       = advisorId;
        this.vehicleId       = vehicleId;
        this.startTime       = startTime;
        this.durationMinutes = durationMinutes;
        this.serviceType     = serviceType;
        this.status          = AppointmentStatus.SCHEDULED;
    }

    // ── State machine ─────────────────────────────────────────────────────
    public void transitionTo(AppointmentStatus newStatus) {
        if (!isTransitionAllowed(this.status, newStatus)) {
            throw new IllegalStateException(
                "[REQ-2.5][CHECK-STATE] REJECTED: transition " +
                this.status + " -> " + newStatus + " is FORBIDDEN.");
        }
        this.status = newStatus;
    }

    private boolean isTransitionAllowed(AppointmentStatus from, AppointmentStatus to) {
        switch (from) {
            case SCHEDULED:
                return to == AppointmentStatus.CONFIRMED
                    || to == AppointmentStatus.CANCELLED;
            case CONFIRMED:
                return to == AppointmentStatus.COMPLETED
                    || to == AppointmentStatus.CANCELLED
                    || to == AppointmentStatus.NO_SHOW;
            case COMPLETED:
            case CANCELLED:
            case NO_SHOW:
                return false; // terminal states
            default:
                return false;
        }
    }

    // ── Overlap detection ─────────────────────────────────────────────────
    public boolean overlapsWith(Appointment other) {
        if (!this.advisorId.equals(other.advisorId)) return false;
        if (this.appointmentId.equals(other.appointmentId)) return false;
        LocalDateTime thisEnd  = this.startTime.plusMinutes(this.durationMinutes);
        LocalDateTime otherEnd = other.startTime.plusMinutes(other.durationMinutes);
        return this.startTime.isBefore(otherEnd)
            && other.startTime.isBefore(thisEnd);
    }

    // ── Validation ────────────────────────────────────────────────────────
    private void validateStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID: start time cannot be null.");
        }
    }

    private void validateDuration(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID DURATION: must be greater than 0. Got: "
                + durationMinutes);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String getAppointmentId()   { return appointmentId; }
    public String getCustomerId()      { return customerId; }
    public String getAdvisorId()       { return advisorId; }
    public String getVehicleId()       { return vehicleId; }
    public LocalDateTime getStartTime(){ return startTime; }
    public int getDurationMinutes()    { return durationMinutes; }
    public String getServiceType()     { return serviceType; }
    public AppointmentStatus getStatus(){ return status; }

    public void setStatus(AppointmentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%s] Customer:%s Advisor:%s | %s | %d min | %s | %s",
            appointmentId, customerId, advisorId,
            startTime, durationMinutes, serviceType, status);
    }
}