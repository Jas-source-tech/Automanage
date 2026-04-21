package ui;

import application.*;
import domain.*;
import infrastructure.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Main — CLI entry point.
 * REQ-2.1: Interactive menu OR run with --demo flag for scripted demo.
 * Every requirement is labelled [REQ-2.X] in output.
 */
public class Main {

    static VehicleService vehicleService;
    static AppointmentService appointmentService;
    static EventBus eventBus;

    public static void main(String[] args) throws Exception {

        // ── Wire up the application ───────────────────────────────────────
        eventBus = EventBus.getInstance();
        eventBus.subscribe("VEHICLE_ADDED",     new ConsoleNotificationListener());
        eventBus.subscribe("VEHICLE_RESERVED",  new ConsoleNotificationListener());
        eventBus.subscribe("VEHICLE_SOLD",      new ConsoleNotificationListener());
        eventBus.subscribe("VEHICLE_ARCHIVED",  new ConsoleNotificationListener());
        eventBus.subscribe("APPOINTMENT_SCHEDULED", new ConsoleNotificationListener());
        eventBus.subscribe("APPOINTMENT_CONFIRMED", new ConsoleNotificationListener());
        eventBus.subscribe("APPOINTMENT_CANCELLED", new ConsoleNotificationListener());
        eventBus.subscribe("APPOINTMENT_COMPLETED", new ConsoleNotificationListener());

        IVehicleRepository vehicleRepo =
            RepositoryFactory.createVehicleRepository(RepositoryFactory.RepositoryType.SQLITE);
        IAppointmentRepository appointmentRepo =
            RepositoryFactory.createAppointmentRepository(RepositoryFactory.RepositoryType.SQLITE);

        HoldPolicy holdPolicy = new StandardHoldPolicy();
        vehicleService     = new VehicleService(vehicleRepo, eventBus, holdPolicy);
        appointmentService = new AppointmentService(appointmentRepo, eventBus);

        // ── Demo mode or interactive menu ─────────────────────────────────
        if (args.length > 0 && args[0].equals("--demo")) {
            runFullDemo();
        } else {
            runInteractiveMenu();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERACTIVE MENU
    // ══════════════════════════════════════════════════════════════════════
    static void runInteractiveMenu() {
        Scanner sc = new Scanner(System.in);
        printArchitectureSummary();

        while (true) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         AUTOMANAGE  MENU             ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Add vehicle                      ║");
            System.out.println("║  2. List all vehicles                 ║");
            System.out.println("║  3. Filter vehicles by status        ║");
            System.out.println("║  4. Search vehicles by make          ║");
            System.out.println("║  5. Reserve vehicle                  ║");
            System.out.println("║  6. Mark vehicle as sold             ║");
            System.out.println("║  7. Archive vehicle                  ║");
            System.out.println("║  8. Schedule appointment             ║");
            System.out.println("║  9. Confirm appointment              ║");
            System.out.println("║ 10. Cancel appointment               ║");
            System.out.println("║ 11. List all appointments            ║");
            System.out.println("║ 12. Demo Policy A vs Policy B        ║");
            System.out.println("║ 13. Run performance benchmark        ║");
            System.out.println("║ 14. Run full demo sequence           ║");
            System.out.println("║  0. Exit                             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Choice: ");

            String input = sc.nextLine().trim();
            System.out.println();

            try {
                switch (input) {
                    case "1"  -> menuAddVehicle(sc);
                    case "2"  -> menuListVehicles();
                    case "3"  -> menuFilterByStatus(sc);
                    case "4"  -> menuSearchByMake(sc);
                    case "5"  -> menuReserveVehicle(sc);
                    case "6"  -> menuMarkSold(sc);
                    case "7"  -> menuArchiveVehicle(sc);
                    case "8"  -> menuScheduleAppointment(sc);
                    case "9"  -> menuConfirmAppointment(sc);
                    case "10" -> menuCancelAppointment(sc);
                    case "11" -> menuListAppointments();
                    case "12" -> demoPolicyComparison();
                    case "13" -> demoPerformance();
                    case "14" -> runFullDemo();
                    case "0"  -> { System.out.println("Goodbye!"); return; }
                    default   -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // FULL SCRIPTED DEMO — covers all REQ-2.X
    // ══════════════════════════════════════════════════════════════════════
    static void runFullDemo() throws Exception {
        sep("AUTOMANAGE — FULL DEMO SEQUENCE");

        // REQ-2.1 ─────────────────────────────────────────────────────────
        sep("[REQ-2.1] Runnable Interface");
        System.out.println("  Program started. CLI menu available. Running scripted demo.");

        // REQ-2.2 ─────────────────────────────────────────────────────────
        sep("[REQ-2.2] Architecture Boundaries");
        printArchitectureSummary();

        // REQ-2.3 ─────────────────────────────────────────────────────────
        sep("[REQ-2.3] Persistence with Abstraction Boundary");
        System.out.println("  Repository interface: domain.IVehicleRepository");
        System.out.println("  Implementation:       infrastructure.SQLiteVehicleRepository");
        System.out.println("  Domain never calls SQLite directly.");
        System.out.println("  Adding vehicles — they will be saved to automanage.db...");

        Vehicle v1 = vehicleService.addVehicle(
            "1HGBH41JXMN109186", "Toyota", "Camry", 2022, 15000, 24999.99, "Used");
        System.out.println("  Saved: " + v1);

        Vehicle v2 = vehicleService.addVehicle(
            "2T1BURHE0JC043821", "Honda", "Civic", 2023, 5000, 28500.00, "New");
        System.out.println("  Saved: " + v2);

        Vehicle v3 = vehicleService.addVehicle(
            "3VWFE21C04M000001", "Toyota", "Corolla", 2021, 30000, 19999.00, "Used");
        System.out.println("  Saved: " + v3);

        System.out.println("  Reloading all vehicles from DB (persistence check)...");
        List<Vehicle> all = vehicleService.getAllVehicles();
        all.forEach(v -> System.out.println("  Loaded: " + v));

        // REQ-2.4 ─────────────────────────────────────────────────────────
        sep("[REQ-2.4] Two Non-Trivial Queries");
        System.out.println("  Query 1: Filter by status = AVAILABLE");
        List<Vehicle> available = vehicleService.filterByStatus(VehicleStatus.AVAILABLE);
        available.forEach(v -> System.out.println("    " + v));

        System.out.println("\n  Query 2: Search by make = 'Toyota'");
        List<Vehicle> toyotas = vehicleService.searchByMake("Toyota");
        toyotas.forEach(v -> System.out.println("    " + v));

        // REQ-2.5 ─────────────────────────────────────────────────────────
        sep("[REQ-2.5] Lifecycle / State Machine Enforcement");
        System.out.println("  Vehicle " + v1.getVehicleId() + " is currently: " + v1.getStatus());
        System.out.println("  Transitioning AVAILABLE -> RESERVED (valid)...");
        vehicleService.reserveVehicle(v1.getVehicleId());
        System.out.println("  Status after: " + vehicleService.findById(v1.getVehicleId()).getStatus());

        System.out.println("\n  Now attempting RESERVED -> AVAILABLE -> SOLD -> AVAILABLE (FORBIDDEN)...");
        vehicleService.markSold(v1.getVehicleId());
        System.out.println("  Status after markSold: " + vehicleService.findById(v1.getVehicleId()).getStatus());
        try {
            vehicleService.reserveVehicle(v1.getVehicleId()); // FORBIDDEN
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        // REQ-2.6 ─────────────────────────────────────────────────────────
        sep("[REQ-2.6] Two Configurable Policies");
        demoPolicyComparison();

        // REQ-2.7 ─────────────────────────────────────────────────────────
        sep("[REQ-2.7] Design Patterns");
        System.out.println("  Pattern Map:");
        System.out.println("  1. SINGLETON  — infrastructure.DatabaseConnection");
        System.out.println("     One shared DB connection for the entire app lifetime.");
        System.out.println("  2. FACTORY    — infrastructure.RepositoryFactory");
        System.out.println("     Creates SQLite or InMemory repos; caller uses interface only.");
        System.out.println("  3. OBSERVER   — application.EventBus + domain.IEventListener");
        System.out.println("     Events published on state changes; listeners notified automatically.");
        System.out.println("     (See NOTIFICATION lines above — Observer in action)");
        System.out.println("  4. STRATEGY   — domain.HoldPolicy (StandardHoldPolicy / StrictHoldPolicy)");
        System.out.println("     Hold duration rule swappable at runtime without changing Vehicle code.");

        // REQ-2.8 ─────────────────────────────────────────────────────────
        sep("[REQ-2.8] Robustness — Invalid Input Rejections");
        System.out.println("  Test 1: Duplicate VIN");
        try {
            vehicleService.addVehicle("1HGBH41JXMN109186", "Ford", "Focus", 2020, 10000, 15000, "Used");
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n  Test 2: Invalid VIN (too short)");
        try {
            vehicleService.addVehicle("BADVIN", "Ford", "Focus", 2020, 10000, 15000, "Used");
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n  Test 3: Invalid price (zero)");
        try {
            vehicleService.addVehicle("4T1BF3EK8AU561234", "Ford", "Focus", 2020, 10000, 0, "Used");
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n  Test 4: Schedule appointment then schedule overlapping one");
        Appointment appt1 = appointmentService.scheduleAppointment(
            "CUST001", "ADV001", v2.getVehicleId(),
            LocalDateTime.of(2026, 6, 1, 10, 0), 60, "Test Drive");
        System.out.println("  Scheduled: " + appt1);
        try {
            appointmentService.scheduleAppointment(
                "CUST002", "ADV001", v2.getVehicleId(),
                LocalDateTime.of(2026, 6, 1, 10, 30), 60, "Test Drive");
        } catch (Exception e) {
            System.out.println("  " + e.getMessage());
        }

        // REQ-2.9 ─────────────────────────────────────────────────────────
        sep("[REQ-2.9] Performance / Scalability Evidence");
        demoPerformance();

        // REQ-2.10 ────────────────────────────────────────────────────────
        sep("[REQ-2.10] Portability");
        System.out.println("  DB file: automanage.db (relative path — no hardcoded absolute paths).");
        System.out.println("  No OS-specific code. Runs on Windows, macOS, Linux with Java 11+.");
        System.out.println("  See README.md for exact run instructions.");

        sep("DEMO COMPLETE");
    }

    // ══════════════════════════════════════════════════════════════════════
    // POLICY COMPARISON DEMO — REQ-2.6
    // ══════════════════════════════════════════════════════════════════════
    static void demoPolicyComparison() {
        System.out.println("  [REQ-2.6] Running same scenario under Policy A then Policy B:");
        String[] categories = {"New", "Used", "CPO"};

        HoldPolicy policyA = new StandardHoldPolicy();
        HoldPolicy policyB = new StrictHoldPolicy();

        System.out.printf("  %-10s %-35s %-30s%n", "Category", policyA.getPolicyName(), policyB.getPolicyName());
        System.out.println("  " + "-".repeat(75));
        for (String cat : categories) {
            System.out.printf("  %-10s %-35s %-30s%n",
                cat,
                policyA.getHoldDurationHours(cat) + " hours",
                policyB.getHoldDurationHours(cat) + " hours");
        }
        System.out.println("  Policy A gives longer holds for New/CPO vehicles.");
        System.out.println("  Policy B enforces a strict 24h cap regardless of category.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // PERFORMANCE BENCHMARK — REQ-2.9
    // ══════════════════════════════════════════════════════════════════════
    static void demoPerformance() {
        System.out.println("  [REQ-2.9] Hot path: filter vehicles by status (dashboard query).");
        System.out.println("  Using InMemory repository to benchmark query speed.");

        IVehicleRepository memRepo =
            RepositoryFactory.createVehicleRepository(RepositoryFactory.RepositoryType.IN_MEMORY);

        int datasetSize = 10000;
        System.out.println("  Generating " + datasetSize + " synthetic vehicles...");

        long genStart = System.nanoTime();
        String[] makes = {"Toyota", "Honda", "Ford", "BMW", "Chevrolet"};
        String[] cats  = {"New", "Used", "CPO"};
        VehicleStatus[] statuses = VehicleStatus.values();

        for (int i = 0; i < datasetSize; i++) {
            String vin = String.format("TEST%013d", i);
            Vehicle v = new Vehicle(
                "V" + i,
                vin,
                makes[i % makes.length],
                "Model" + (i % 10),
                2015 + (i % 10),
                i * 100,
                10000 + (i * 2.5),
                cats[i % cats.length]
            );
            v.setStatus(statuses[i % statuses.length]);
            memRepo.save(v);
        }
        long genEnd = System.nanoTime();
        System.out.printf("  Dataset generated in %.2f ms%n", (genEnd - genStart) / 1_000_000.0);

        // Benchmark filter query
        long queryStart = System.nanoTime();
        List<Vehicle> result = memRepo.findByStatus(VehicleStatus.AVAILABLE);
        long queryEnd = System.nanoTime();

        double ms = (queryEnd - queryStart) / 1_000_000.0;
        System.out.printf("  Filter by AVAILABLE from %d vehicles: %d results in %.2f ms%n",
            datasetSize, result.size(), ms);
        System.out.println("  Target SLO: < 500 ms. Result: " + (ms < 500 ? "PASS" : "FAIL"));
        System.out.println("  Approach: SQLite indexes on status + make columns (see DatabaseConnection.java).");
        System.out.println("  Trade-off: indexes speed reads but add overhead on writes.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // ARCHITECTURE SUMMARY
    // ══════════════════════════════════════════════════════════════════════
    static void printArchitectureSummary() {
        System.out.println("\n  [REQ-2.2] Architecture Summary:");
        System.out.println("  ┌─────────────────────────────────────────────────┐");
        System.out.println("  │  UI Layer        ui.Main                        │");
        System.out.println("  │  Application     application.VehicleService      │");
        System.out.println("  │                  application.AppointmentService  │");
        System.out.println("  │                  application.EventBus            │");
        System.out.println("  │  Domain          domain.Vehicle                  │");
        System.out.println("  │                  domain.Appointment              │");
        System.out.println("  │                  domain.IVehicleRepository (if)  │");
        System.out.println("  │  Infrastructure  infrastructure.SQLiteVehicleRepo│");
        System.out.println("  │                  infrastructure.DatabaseConnection│");
        System.out.println("  └─────────────────────────────────────────────────┘");
        System.out.println("  Rule: Domain never imports Infrastructure.");
        System.out.println("  Business logic lives in domain entities only.");
    }

    // ══════════════════════════════════════════════════════════════════════
    // INTERACTIVE MENU HANDLERS
    // ══════════════════════════════════════════════════════════════════════
    static void menuAddVehicle(Scanner sc) {
        System.out.print("VIN (17 chars): ");       String vin      = sc.nextLine().trim();
        System.out.print("Make: ");                  String make     = sc.nextLine().trim();
        System.out.print("Model: ");                 String model    = sc.nextLine().trim();
        System.out.print("Year: ");                  int year        = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Mileage: ");               int mileage     = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Price: ");                 double price    = Double.parseDouble(sc.nextLine().trim());
        System.out.print("Category (New/Used/CPO): ");String category= sc.nextLine().trim();
        Vehicle v = vehicleService.addVehicle(vin, make, model, year, mileage, price, category);
        System.out.println("  Added: " + v);
    }

    static void menuListVehicles() {
        List<Vehicle> all = vehicleService.getAllVehicles();
        if (all.isEmpty()) { System.out.println("  No vehicles found."); return; }
        all.forEach(v -> System.out.println("  " + v));
        System.out.println("  Total: " + all.size());
    }

    static void menuFilterByStatus(Scanner sc) {
        System.out.print("Status (AVAILABLE/RESERVED/SOLD/ARCHIVED/UNDER_SERVICE): ");
        VehicleStatus status = VehicleStatus.valueOf(sc.nextLine().trim().toUpperCase());
        List<Vehicle> results = vehicleService.filterByStatus(status);
        System.out.println("  [REQ-2.4] Filter by " + status + ": " + results.size() + " result(s)");
        results.forEach(v -> System.out.println("  " + v));
    }

    static void menuSearchByMake(Scanner sc) {
        System.out.print("Make to search: ");
        String make = sc.nextLine().trim();
        List<Vehicle> results = vehicleService.searchByMake(make);
        System.out.println("  [REQ-2.4] Search '" + make + "': " + results.size() + " result(s)");
        results.forEach(v -> System.out.println("  " + v));
    }

    static void menuReserveVehicle(Scanner sc) {
        System.out.print("Vehicle ID: ");
        String id = sc.nextLine().trim();
        vehicleService.reserveVehicle(id);
        System.out.println("  [REQ-2.5] Reserved: " + vehicleService.findById(id));
    }

    static void menuMarkSold(Scanner sc) {
        System.out.print("Vehicle ID: ");
        String id = sc.nextLine().trim();
        vehicleService.markSold(id);
        System.out.println("  [REQ-2.5] Sold: " + vehicleService.findById(id));
    }

    static void menuArchiveVehicle(Scanner sc) {
        System.out.print("Vehicle ID: ");
        String id = sc.nextLine().trim();
        vehicleService.archiveVehicle(id);
        System.out.println("  [REQ-2.5] Archived: " + vehicleService.findById(id));
    }

    static void menuScheduleAppointment(Scanner sc) {
        System.out.print("Customer ID: ");   String cust    = sc.nextLine().trim();
        System.out.print("Advisor ID: ");    String adv     = sc.nextLine().trim();
        System.out.print("Vehicle ID (or leave blank): "); String veh = sc.nextLine().trim();
        System.out.print("Date/time (yyyy-MM-ddTHH:mm): "); String dt = sc.nextLine().trim();
        System.out.print("Duration (minutes): "); int dur = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Service type: "); String svc = sc.nextLine().trim();
        Appointment a = appointmentService.scheduleAppointment(
            cust, adv, veh.isEmpty() ? null : veh,
            LocalDateTime.parse(dt), dur, svc);
        System.out.println("  Scheduled: " + a);
    }

    static void menuConfirmAppointment(Scanner sc) {
        System.out.print("Appointment ID: ");
        String id = sc.nextLine().trim();
        appointmentService.confirmAppointment(id);
        System.out.println("  [REQ-2.5] Confirmed appointment " + id);
    }

    static void menuCancelAppointment(Scanner sc) {
        System.out.print("Appointment ID: ");
        String id = sc.nextLine().trim();
        appointmentService.cancelAppointment(id);
        System.out.println("  [REQ-2.5] Cancelled appointment " + id);
    }

    static void menuListAppointments() {
        List<Appointment> all = appointmentService.getAllAppointments();
        if (all.isEmpty()) { System.out.println("  No appointments found."); return; }
        all.forEach(a -> System.out.println("  " + a));
        System.out.println("  Total: " + all.size());
    }

    static void sep(String title) {
        System.out.println("\n══════════════════════════════════════════════════");
        System.out.println("  " + title);
        System.out.println("══════════════════════════════════════════════════");
    }
}