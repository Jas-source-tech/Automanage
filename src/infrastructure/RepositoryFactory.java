package infrastructure;

import domain.IAppointmentRepository;
import domain.IVehicleRepository;

/**
 * RepositoryFactory — Factory pattern (creational).
 * REQ-2.7: Creates repository instances; caller never calls new directly.
 * Trade-off: swap SQLite for InMemory (tests) by changing factory only.
 */
public class RepositoryFactory {

    public enum RepositoryType {
        SQLITE,
        IN_MEMORY
    }

    public static IVehicleRepository createVehicleRepository(RepositoryType type) {
        switch (type) {
            case SQLITE:
                return new SQLiteVehicleRepository(DatabaseConnection.getInstance());
            case IN_MEMORY:
                return new InMemoryVehicleRepository();
            default:
                throw new IllegalArgumentException("Unknown repository type: " + type);
        }
    }

    public static IAppointmentRepository createAppointmentRepository(RepositoryType type) {
        switch (type) {
            case SQLITE:
                return new SQLiteAppointmentRepository(DatabaseConnection.getInstance());
            case IN_MEMORY:
                return new InMemoryAppointmentRepository();
            default:
                throw new IllegalArgumentException("Unknown repository type: " + type);
        }
    }
}