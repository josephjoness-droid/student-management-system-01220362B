# AcaTrack – Student Management System Plus

A clean, offline desktop application for managing student records.
Built with JavaFX, SQLite, and JDBC. Light academic theme with top navigation bar.

**Version:** 1.0.0 | **Platform:** Windows Offline

---

## How to Run on Windows

### Prerequisites
- JDK 21 (Eclipse Temurin recommended: https://adoptium.net/)
- JavaFX SDK 21 (https://gluonhq.com/products/javafx/)

### Open in IntelliJ IDEA (Recommended)

1. `File → Open` → select the **`pom.xml`** file inside this folder
2. Click **"Open as Project"** when prompted
3. Wait for Maven to download dependencies (needs internet once)
4. Go to `Run → Edit Configurations → + → Application`
5. Set **Main class**: `com.sms2.MainApp`
6. Set **VM options**:
   ```
   --module-path "C:\javafx\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
   ```
7. Set **Working directory** to the project root folder
8. Click **Run**

### Run via Maven

```bash
mvn clean javafx:run
```

### Run Tests

```bash
mvn test
```

Capture test output:
```bash
mvn test > evidence\mvn_test_output.txt 2>&1
```

---

## Architecture

```
com.sms2/
├── MainApp.java                        ← Entry point (main class)
├── domain/     Student, ImportResult   ← Pure models
├── repository/ Interface + SQLite impl ← Data access (prepared statements only)
├── service/    StudentService, ValidationService, ReportService
├── ui/controllers/ MainController      ← No SQL, no business logic
└── util/       DatabaseManager, AppLogger, CsvHelper
```

## IntelliJ Run Config Summary

| Field | Value |
|---|---|
| Main class | `com.sms2.MainApp` |
| VM options | `--module-path "C:\javafx\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml` |
| Working directory | Project root (where pom.xml is) |
