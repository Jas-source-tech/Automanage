Automanage — Dealership Inventory & Appointment Hub

Prerequisites
- Java 11 or higher (`java --version` to verify)
- SQLite JDBC driver (`sqlite-jdbc-3.x.x.jar`) — download from https://github.com/xerial/sqlite-jdbc/releases
  - Place the JAR file in the `lib/` folder

Architecture Notes [REQ-2.2]
| Layer          | Package          | Rule |
|----------------|------------------|------|
| UI             | ui               | Calls Application only |
| Application    | application      | Orchestrates Domain; calls Repository interfaces |
| Domain         | domain           | Zero imports from Infrastructure or UI |
| Infrastructure | infrastructure   | Implements Domain interfaces; owns SQLite |

**Dependency direction:** UI → Application → Domain ← Infrastructure

**Business logic lives in:** domain entities (Vehicle.transitionTo, Appointment.overlapsWith)


## Design Patterns [REQ-2.7]
| Pattern   | Category    | Classes                                        | Trade-off |
|-----------|-------------|------------------------------------------------|-----------|
| Singleton | Creational  | infrastructure.DatabaseConnection              | Easy global access; harder to mock in tests |
| Factory   | Structural  | infrastructure.RepositoryFactory               | Swap SQLite/InMemory without changing callers |
| Observer  | Behavioral  | application.EventBus + domain.IEventListener   | Extensible notifications; async chains harder to debug |
| Strategy  | Behavioral  | domain.HoldPolicy (Standard vs Strict)         | Configurable rules at runtime; requires policy loader |


How to Compile and Run

Windows
```
# Step 1 — Create output folder
mkdir out

# Step 2 — Compile all Java files
javac -cp "lib\sqlite-jdbc-3.47.1.0.jar" -d out src\domain\*.java src\application\*.java src\infrastructure\*.java src\ui\*.java

# Step 3 — Run interactive menu
java -cp "out;lib\sqlite-jdbc-3.47.1.0.jar" ui.Main

# Step 4 — Run scripted demo (all REQ-2.X labels shown automatically)
java -cp "out;lib\sqlite-jdbc-3.47.1.0.jar" ui.Main --demo
```

macOS / Linux
```
mkdir out
javac -cp "lib/sqlite-jdbc-3.47.1.0.jar" -d out src/domain/*.java src/application/*.java src/infrastructure/*.java src/ui/*.java
java -cp "out:lib/sqlite-jdbc-3.47.1.0.jar" ui.Main
java -cp "out:lib/sqlite-jdbc-3.47.1.0.jar" ui.Main --demo
```

---

Demo Mode [REQ-2.1]
Run with `--demo` flag. The program will automatically:
- Print architecture summary [REQ-2.2]
- Save and reload vehicles from SQLite [REQ-2.3]
- Run two queries: filter by status + search by make [REQ-2.4]
- Show valid state transition then attempt forbidden transition [REQ-2.5]
- Compare Policy A vs Policy B outcomes [REQ-2.6]
- Print pattern map with class names [REQ-2.7]
- Demonstrate 4 invalid input rejections [REQ-2.8]
- Benchmark 10,000 vehicle query with timing [REQ-2.9]
- Confirm no hardcoded paths [REQ-2.10]

---

Notes
- Database file `automanage.db` is created automatically in the project root on first run
- No absolute paths are hardcoded anywhere
- To reset the database, delete `automanage.db` and rerun