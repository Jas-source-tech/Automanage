package application;

import domain.*;
import java.util.List;
import java.util.UUID;

/**
 * VehicleService — application layer.
 * Orchestrates domain objects; owns transaction logic.
 * No direct IO. Calls repository interfaces only.
 * REQ-2.2: Architecture boundary — no direct DB calls here.
 */
public class VehicleService {
    private final IVehicleRepository repo;
    private final EventBus eventBus;
    private HoldPolicy holdPolicy;

    public VehicleService(IVehicleRepository repo, EventBus eventBus, HoldPolicy holdPolicy) {
        this.repo       = repo;
        this.eventBus   = eventBus;
        this.holdPolicy = holdPolicy;
    }

    public void setHoldPolicy(HoldPolicy holdPolicy) {
        this.holdPolicy = holdPolicy;
    }

    public HoldPolicy getHoldPolicy() {
        return holdPolicy;
    }

    // ── Add vehicle ───────────────────────────────────────────────────────
    public Vehicle addVehicle(String vin, String make, String model,
                              int year, int mileage, double price, String category) {
        // Check duplicate VIN — REQ-2.8
        if (repo.findByVin(vin) != null) {
            throw new IllegalArgumentException(
                "[REQ-2.8] DUPLICATE VIN: a vehicle with VIN '" + vin
                + "' already exists. E-VIN-DUPLICATE.");
        }
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Vehicle vehicle = new Vehicle(id, vin, make, model, year, mileage, price, category);
        repo.save(vehicle);
        eventBus.publish("VEHICLE_ADDED", make + " " + model + " (VIN: " + vin + ")");
        return vehicle;
    }

    // ── Reserve vehicle ───────────────────────────────────────────────────
    public void reserveVehicle(String vehicleId) {
        Vehicle v = getOrThrow(vehicleId);
        v.transitionTo(VehicleStatus.RESERVED); // state machine enforced
        repo.save(v);
        int holdHours = holdPolicy.getHoldDurationHours(v.getCategory());
        eventBus.publish("VEHICLE_RESERVED",
            v.getMake() + " " + v.getModel()
            + " reserved for " + holdHours + "h ("
            + holdPolicy.getPolicyName() + ")");
    }

    // ── Mark sold ─────────────────────────────────────────────────────────
    public void markSold(String vehicleId) {
        Vehicle v = getOrThrow(vehicleId);
        v.transitionTo(VehicleStatus.SOLD);
        repo.save(v);
        eventBus.publish("VEHICLE_SOLD", v.getMake() + " " + v.getModel());
    }

    // ── Archive ───────────────────────────────────────────────────────────
    public void archiveVehicle(String vehicleId) {
        Vehicle v = getOrThrow(vehicleId);
        v.transitionTo(VehicleStatus.ARCHIVED);
        repo.save(v);
        eventBus.publish("VEHICLE_ARCHIVED", v.getMake() + " " + v.getModel());
    }

    // ── Queries ───────────────────────────────────────────────────────────
    public List<Vehicle> getAllVehicles() {
        return repo.findAll();
    }

    public List<Vehicle> filterByStatus(VehicleStatus status) {
        return repo.findByStatus(status);
    }

    public List<Vehicle> searchByMake(String make) {
        return repo.findByMake(make);
    }

    public Vehicle findById(String vehicleId) {
        return getOrThrow(vehicleId);
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private Vehicle getOrThrow(String vehicleId) {
        Vehicle v = repo.findById(vehicleId);
        if (v == null) {
            throw new IllegalArgumentException(
                "[REQ-2.8] NOT FOUND: No vehicle with ID '" + vehicleId + "'.");
        }
        return v;
    }
}