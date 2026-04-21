package domain;

/**
 * Domain entity representing a vehicle in inventory.
 * Owns its own state machine and invariant validation.
 * No direct IO or infrastructure calls here.
 */
public class Vehicle {
    private String vehicleId;
    private String vin;
    private String make;
    private String model;
    private int year;
    private int mileage;
    private double price;
    private String category; // New, Used, CPO
    private VehicleStatus status;

    public Vehicle(String vehicleId, String vin, String make, String model,
                   int year, int mileage, double price, String category) {
        validateVin(vin);
        validatePrice(price);
        validateYear(year);
        this.vehicleId = vehicleId;
        this.vin       = vin.toUpperCase();
        this.make      = make;
        this.model     = model;
        this.year      = year;
        this.mileage   = mileage;
        this.price     = price;
        this.category  = category;
        this.status    = VehicleStatus.AVAILABLE;
    }

    // ── State machine ─────────────────────────────────────────────────────
    public void transitionTo(VehicleStatus newStatus) {
        if (!isTransitionAllowed(this.status, newStatus)) {
            throw new IllegalStateException(
                "[REQ-2.5][CHECK-STATE] REJECTED: transition " +
                this.status + " -> " + newStatus + " is FORBIDDEN.");
        }
        this.status = newStatus;
    }

    private boolean isTransitionAllowed(VehicleStatus from, VehicleStatus to) {
        switch (from) {
            case AVAILABLE:
                return to == VehicleStatus.RESERVED
                    || to == VehicleStatus.UNDER_SERVICE
                    || to == VehicleStatus.ARCHIVED;
            case RESERVED:
                return to == VehicleStatus.AVAILABLE
                    || to == VehicleStatus.SOLD
                    || to == VehicleStatus.ARCHIVED;
            case UNDER_SERVICE:
                return to == VehicleStatus.AVAILABLE
                    || to == VehicleStatus.ARCHIVED;
            case SOLD:
                return false; // terminal — no transitions allowed
            case ARCHIVED:
                return false; // terminal — no transitions allowed
            default:
                return false;
        }
    }

    // ── Invariant validation ──────────────────────────────────────────────
    private void validateVin(String vin) {
        if (vin == null || vin.trim().length() != 17) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID VIN: must be exactly 17 characters. Got: '"
                + vin + "'");
        }
        if (!vin.matches("[A-Za-z0-9]{17}")) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID VIN: must be alphanumeric only. Got: '"
                + vin + "'");
        }
    }

    private void validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID PRICE: must be greater than 0. Got: " + price);
        }
    }

    private void validateYear(int year) {
        int currentYear = java.time.Year.now().getValue();
        if (year < 1900 || year > currentYear + 1) {
            throw new IllegalArgumentException(
                "[REQ-2.8] INVALID YEAR: must be between 1900 and "
                + (currentYear + 1) + ". Got: " + year);
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String getVehicleId() { return vehicleId; }
    public String getVin()       { return vin; }
    public String getMake()      { return make; }
    public String getModel()     { return model; }
    public int    getYear()      { return year; }
    public int    getMileage()   { return mileage; }
    public double getPrice()     { return price; }
    public String getCategory()  { return category; }
    public VehicleStatus getStatus() { return status; }

    public void setStatus(VehicleStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%s] %d %s %s | VIN: %s | $%.2f | %s | %s",
            vehicleId, year, make, model, vin, price, category, status);
    }
}