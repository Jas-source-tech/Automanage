package infrastructure;

import domain.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryAppointmentRepository implements IAppointmentRepository {
    private final Map<String, Appointment> store = new HashMap<>();

    @Override
    public void save(Appointment a) {
        store.put(a.getAppointmentId(), a);
    }

    @Override
    public Appointment findById(String appointmentId) {
        return store.get(appointmentId);
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Appointment> findByAdvisor(String advisorId) {
        return store.values().stream()
            .filter(a -> a.getAdvisorId().equals(advisorId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return store.values().stream()
            .filter(a -> a.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String appointmentId) {
        store.remove(appointmentId);
    }
}