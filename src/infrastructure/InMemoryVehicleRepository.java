package infrastructure;

import domain.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * InMemoryVehicleRepository — used in tests / demo without SQLite.
 * REQ-2.3: Shows repository/interface boundary clearly.
 */
public class InMemoryVehicleRepository implements IVehicleRepository {
    private final Map<String, Vehicle> store = new HashMap<>();

    @Override
    public void save(Vehicle v) {
        store.put(v.getVehicleId(), v);
    }

    @Override
    public Vehicle findById(String vehicleId) {
        return store.get(vehicleId);
    }

    @Override
    public Vehicle findByVin(String vin) {
        return store.values().stream()
            .filter(v -> v.getVin().equalsIgnoreCase(vin))
            .findFirst().orElse(null);
    }

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Vehicle> findByStatus(VehicleStatus status) {
        return store.values().stream()
            .filter(v -> v.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public List<Vehicle> findByMake(String make) {
        return store.values().stream()
            .filter(v -> v.getMake().toLowerCase().contains(make.toLowerCase()))
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String vehicleId) {
        store.remove(vehicleId);
    }
}