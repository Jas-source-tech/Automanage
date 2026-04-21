package domain;

import java.util.List;

/**
 * Repository interface — persistence abstraction boundary.
 * REQ-2.3: Domain never talks to storage directly.
 * REQ-2.7: Structural pattern (Repository/Facade).
 */
public interface IVehicleRepository {
    void save(Vehicle vehicle);
    Vehicle findById(String vehicleId);
    Vehicle findByVin(String vin);
    List<Vehicle> findAll();
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByMake(String make);
    void delete(String vehicleId);
}