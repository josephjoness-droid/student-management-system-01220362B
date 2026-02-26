package com.sms2.ui.controllers;

import com.sms2.domain.ImportResult;
import com.sms2.domain.Student;
import com.sms2.repository.SqliteStudentRepository;
import com.sms2.service.ReportService;
import com.sms2.service.StudentService;
import com.sms2.service.ValidationService;
import com.sms2.util.AppLogger;
import javafx.applicationproperty.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // ── Services ──
    private final StudentService studentService;
    private final ReportService reportService;
    private final ValidationService validationService;

    // ── Top Nav ──
    @FXML private Button navHome, navStudents, navReports, navImport, navSettings;
    @FXML private Label lblClock;

    // ── Pages ──
    @FXML private ScrollPane homePage;
    @FXML private VBox studentsPage, reportsPage;
    @FXML private ScrollPane importExportPage, settingsPage;

    // ── Home KPIs ──
    @FXML private Label kpiTotal, kpiActive, kpiInactive, kpiAvgGpa;

    // ── Students ──
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cboProgramme, cboStatus;
    @FXML private ComboBox<String> cboLevel;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student,String>  colId, colName, colProg, colEmail, colPhone, colDate, colStatus;
    @FXML private TableColumn<Student,Integer> colLevel;
    @FXML private TableColumn<Student,Double>  colGpa;
    @FXML private Label lblStudentCount;

    // ── Reports ──
    @FXML private TabPane reportTabs;
    @FXML private Tab tabTop, tabRisk, tabDist, tabProg;

    // ── Import/Export ──
    @FXML private TextField txtImportPath;
    @FXML private Label lblImportSuccess, lblImportErrors, lblExportStatus;

    // ── Settings ──
    @FXML private TextField txtThreshold;
    @FXML private Label lblThresholdMsg;

    // ── Status Bar ──
    @FXML private Label lblStatusMsg;

    private double atRiskThreshold = 2.0;
    private ObservableList<Student> studentData;
    private File selectedImportFile;

    public MainController() {
        this.studentService   = new StudentService(new SqliteStudentRepository());
        this.reportService    = new ReportService();
        this.validationService= new ValidationService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initTable();
        initFilters();
        loadStudents();
        refreshKpis();
        startClock();
        AppLogger.info("Application started.");
    }

    // ═══════════════════════════════
    //  NAVIGATION
    // ═══════════════════════════════

    @FXML public void showHome()         { switchPage(homePage,         navHome);     refreshKpis(); }
    @FXML public void showStudents()     { switchPage(studentsPage,     navStudents); loadStudents(); }
    @FXML public void showReports()      { switchPage(reportsPage,      navReports);  buildReports(); }
    @FXML public void showImportExport() { switchPage(importExportPage, navImport); }
    @FXML public void showSettings()     { switchPage(settingsPage,     navSettings); txtThreshold.setText(String.valueOf(atRiskThreshold)); }

    private void switchPage(Node activePage, Button activeBtn) {
        Node[] pages = {homePage, studentsPage, reportsPage, importExportPage, settingsPage};
        Button[] btns = {navHome, navStudents, navReports, navImport, navSettings};
        for (Node p : pages) { p.setVisible(false); p.setManaged(false); }
        for (Button b : btns) { b.getStyleClass().removeAll("nav-tab-active"); }
        activePage.setVisible(true);
        activePage.setManaged(true);
        activeBtn.getStyleClass().add("nav-tab-active");
        setStatus("Ready");
    }

    // ═══════════════════════════════
    //  HOME KPIs
    // ═══════════════════════════════

    private void refreshKpis() {
        List<Student> all = studentService.getAllStudents();
        kpiTotal.setText(String.valueOf(all.size()));
        kpiActive.setText(String.valueOf(reportService.countActive(all)));
        kpiInactive.setText(String.valueOf(reportService.countInactive(all)));
        kpiAvgGpa.setText(String.format("%.2f", reportService.getAverageGpa(all)));
    }

    private void startClock() {
        Thread t = new Thread(() -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE dd MMM  HH:mm");
            while (true) {
                String now = LocalDateTime.now().format(fmt);
                Platform.runLater(() -> lblClock.setText(now));
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ═══════════════════════════════
    //  STUDENTS TABLE
    // ═══════════════════════════════

    private void initTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentId()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colProg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramme()));
        colLevel.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getLevel()).asObject());
        colGpa.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getGpa()).asObject());
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateAdded().toString()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        studentData = FXCollections.observableArrayList();
        studentTable.setItems(studentData);

        // Colour-code GPA column
        colGpa.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double gpa, boolean empty) {
                super.updateItem(gpa, empty);
                if (empty || gpa == null) { setText(null); setStyle(""); }
                else {
                    setText(String.format("%.2f", gpa));
                    if (gpa < atRiskThreshold) setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
                    else if (gpa >= 3.5) setStyle("-fx-text-fill:#059669; -fx-font-weight:bold;");
                    else setStyle("");
                }
            }
        });

        // Colour-code Status column
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); }
                else {
                    setText(status);
                    setStyle(status.equals("ACTIVE")
                            ? "-fx-text-fill:#059669; -fx-font-weight:bold;"
                            : "-fx-text-fill:#94a3b8;");
                }
            }
        });
    }

    private void initFilters() {
        cboLevel.setItems(FXCollections.observableArrayList("All","100","200","300","400","500","600","700"));
        cboStatus.setItems(FXCollections.observableArrayList("All","ACTIVE","INACTIVE"));
        cboProgramme.getItems().add("All");
    }

    private void loadStudents() {
        List<Student> students = studentService.getAllStudents();
        studentData.setAll(students);
        lblStudentCount.setText(students.size() + " records");
        refreshProgrammeFilter();
    }

    private void refreshProgrammeFilter() {
        String sel = cboProgramme.getValue();
        cboProgramme.getItems().clear();
        cboProgramme.getItems().add("All");
        cboProgramme.getItems().addAll(studentService.getAllProgrammes());
        cboProgramme.setValue(sel != null && cboProgramme.getItems().contains(sel) ? sel : "All");
    }

    @FXML private void onSearch() {
        String q = txtSearch.getText().trim();
        List<Student> r = q.isEmpty() ? studentService.getAllStudents() : studentService.searchStudents(q);
        studentData.setAll(r);
        lblStudentCount.setText(r.size() + " records");
        setStatus("Found " + r.size() + " student(s).");
    }

    @FXML private void onClearFilter() {
        cboProgramme.setValue("All"); cboLevel.setValue("All"); cboStatus.setValue("All");
        txtSearch.clear(); loadStudents(); setStatus("Filters cleared.");
    }

    @FXML private void onRefresh()      { loadStudents(); setStatus("Refreshed."); }
    @FXML private void onSortByName()   { studentData.setAll(studentService.getSortedByName(studentData)); }
    @FXML private void onSortGpaAsc()   { studentData.setAll(studentService.getSortedByGpa(studentData, true)); }
    @FXML private void onSortGpaDesc()  { studentData.setAll(studentService.getSortedByGpa(studentData, false)); }

    // ═══════════════════════════════
    //  ADD / EDIT / DELETE
    // ═══════════════════════════════

    @FXML private void onAddStudent()   { showStudentDialog(null); }

    @FXML private void onEditStudent() {
        Student s = studentTable.getSelectionModel().getSelectedItem();
        if (s == null) { alert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit."); return; }
        showStudentDialog(s);
    }

    @FXML private void onDeleteStudent() {
        Student s = studentTable.getSelectionModel().getSelectedItem();
        if (s == null) { alert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirm Delete");
        c.setHeaderText("Delete student: " + s.getFullName() + "?");
        c.setContentText("This action cannot be undone.");
        c.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (studentService.deleteStudent(s.getStudentId())) {
                loadStudents(); setStatus("Student deleted.");
            } else alert(Alert.AlertType.ERROR, "Error", "Failed to delete student.");
        });
    }

    private void showStudentDialog(Student existing) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Student" : "Edit Student");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(24, 28, 10, 28));
        grid.setMinWidth(440);

        TextField fId    = new TextField();
        TextField fName  = new TextField();
        TextField fProg  = new TextField();
        ComboBox<Integer> fLevel = new ComboBox<>(FXCollections.observableArrayList(100,200,300,400,500,600,700));
        TextField fGpa   = new TextField();
        TextField fEmail = new TextField();
        TextField fPhone = new TextField();
        ComboBox<String> fStatus = new ComboBox<>(FXCollections.observableArrayList("ACTIVE","INACTIVE"));

        // Apply styling
        for (TextField tf : new TextField[]{fId,fName,fProg,fGpa,fEmail,fPhone}) {
            tf.setPrefWidth(260);
            tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; -fx-border-radius:5; -fx-background-radius:5; -fx-padding:7 10;");
        }

        if (existing != null) {
            fId.setText(existing.getStudentId()); fId.setEditable(false);
            fId.setStyle(fId.getStyle() + "-fx-text-fill:#94a3b8;");
            fName.setText(existing.getFullName()); fProg.setText(existing.getProgramme());
            fLevel.setValue(existing.getLevel()); fGpa.setText(String.valueOf(existing.getGpa()));
            fEmail.setText(existing.getEmail()); fPhone.setText(existing.getPhoneNumber());
            fStatus.setValue(existing.getStatus().name());
        } else { fLevel.setValue(100); fStatus.setValue("ACTIVE"); }

        String[] labels = {"Student ID *","Full Name *","Programme *","Level *","GPA *","Email *","Phone *","Status"};
        Node[] fields   = {fId, fName, fProg, fLevel, fGpa, fEmail, fPhone, fStatus};
        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.setStyle("-fx-font-weight:bold; -fx-text-fill:#374151; -fx-font-size:12px;");
            grid.add(lbl, 0, i); grid.add(fields[i], 1, i);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color:#ffffff;");

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            Student s = existing != null ? existing : new Student();
            s.setStudentId(fId.getText().trim());
            s.setFullName(fName.getText().trim());
            s.setProgramme(fProg.getText().trim());
            s.setLevel(fLevel.getValue() != null ? fLevel.getValue() : 100);
            try { s.setGpa(Double.parseDouble(fGpa.getText().trim())); } catch (NumberFormatException e) { s.setGpa(-1); }
            s.setEmail(fEmail.getText().trim());
            s.setPhoneNumber(fPhone.getText().trim());
            s.setStatus(Student.StudentStatus.valueOf(fStatus.getValue()));
            return s;
        });

        dialog.showAndWait().ifPresent(s -> {
            List<String> errors = existing == null ? studentService.addStudent(s) : studentService.updateStudent(s);
            if (errors.isEmpty()) {
                loadStudents();
                setStatus(existing == null ? "Student added successfully." : "Student updated successfully.");
            } else {
                alert(Alert.AlertType.ERROR, "Validation Error", String.join("\n", errors));
            }
        });
    }

    // ═══════════════════════════════
    //  REPORTS
    // ═══════════════════════════════

    private void buildReports() {
        List<Student> all = studentService.getAllStudents();

        // Tab 1 – Top Performers
        List<Student> top = reportService.getTopPerformers(all, 10, null, null);
        tabTop.setContent(buildReportTable(top, "Top 10 students by GPA across all programmes"));

        // Tab 2 – At Risk
        List<Student> risk = reportService.getAtRiskStudents(all, atRiskThreshold);
        tabRisk.setContent(buildReportTable(risk, "Students with GPA below threshold of " + atRiskThreshold));

        // Tab 3 – GPA Distribution
        java.util.Map<String,Long> dist = reportService.getGpaDistribution(all);
        TableView<String[]> tDist = new TableView<>();
        TableColumn<String[],String> cBand = new TableColumn<>("GPA Band");
        cBand.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        cBand.setPrefWidth(200);
        TableColumn<String[],String> cCount = new TableColumn<>("Number of Students");
        cCount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        cCount.setPrefWidth(180);
        tDist.getColumns().addAll(cBand, cCount);
        ObservableList<String[]> dRows = FXCollections.observableArrayList();
        dist.forEach((k,v) -> dRows.add(new String[]{k, String.valueOf(v)}));
        tDist.setItems(dRows); tDist.setMaxHeight(280);
        tDist.getStyleClass().add("data-table");
        VBox distBox = new VBox(10, new Label("GPA distribution across all students"), tDist);
        distBox.setPadding(new Insets(16)); distBox.setStyle("-fx-background-color:#f5f7fa;");
        tabDist.setContent(distBox);

        // Tab 4 – Programme Summary
        java.util.Map<String,double[]> summary = reportService.getProgrammeSummary(all);
        TableView<String[]> tProg = new TableView<>();
        TableColumn<String[],String> cPName  = new TableColumn<>("Programme");    cPName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));  cPName.setPrefWidth(220);
        TableColumn<String[],String> cPCount = new TableColumn<>("Total Students"); cPCount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1])); cPCount.setPrefWidth(140);
        TableColumn<String[],String> cPAvg   = new TableColumn<>("Average GPA");   cPAvg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));   cPAvg.setPrefWidth(140);
        tProg.getColumns().addAll(cPName, cPCount, cPAvg);
        ObservableList<String[]> pRows = FXCollections.observableArrayList();
        summary.forEach((k,v) -> pRows.add(new String[]{k, String.valueOf((int)v[0]), String.format("%.2f", v[1])}));
        tProg.setItems(pRows); tProg.setMaxHeight(280);
        tProg.getStyleClass().add("data-table");
        VBox progBox = new VBox(10, new Label("Student count and average GPA per programme"), tProg);
        progBox.setPadding(new Insets(16)); progBox.setStyle("-fx-background-color:#f5f7fa;");
        tabProg.setContent(progBox);
    }

    private VBox buildReportTable(List<Student> students, String caption) {
        Label cap = new Label(caption);
        cap.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        TableView<Student> t = new TableView<>();
        t.getStyleClass().add("data-table");
        TableColumn<Student,String>  c1 = new TableColumn<>("Student ID");  c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentId())); c1.setPrefWidth(110);
        TableColumn<Student,String>  c2 = new TableColumn<>("Full Name");   c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));  c2.setPrefWidth(180);
        TableColumn<Student,String>  c3 = new TableColumn<>("Programme");   c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramme())); c3.setPrefWidth(170);
        TableColumn<Student,Integer> c4 = new TableColumn<>("Level");       c4.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getLevel()).asObject()); c4.setPrefWidth(65);
        TableColumn<Student,Double>  c5 = new TableColumn<>("GPA");         c5.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getGpa()).asObject()); c5.setPrefWidth(65);
        t.getColumns().addAll(c1,c2,c3,c4,c5);
        t.setItems(FXCollections.observableArrayList(students));

        VBox box = new VBox(10, cap, t);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#f5f7fa;");
        return box;
    }

    // ═══════════════════════════════
    //  IMPORT / EXPORT
    // ═══════════════════════════════

    @FXML private void onBrowseImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select CSV to Import");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files","*.csv"));
        File f = fc.showOpenDialog(null);
        if (f != null) { selectedImportFile = f; txtImportPath.setText(f.getAbsolutePath()); }
    }

    @FXML private void onImport() {
        if (selectedImportFile == null) { alert(Alert.AlertType.WARNING,"No File","Please browse for a CSV file first."); return; }
        Task<ImportResult> task = new Task<>() { @Override protected ImportResult call() { return studentService.importFromCsv(selectedImportFile); } };
        task.setOnSucceeded(e -> {
            ImportResult r = task.getValue();
            lblImportSuccess.setText("✓ Imported: " + r.getSuccessCount() + " rows");
            lblImportErrors.setText(r.getErrorCount() > 0 ? "✗ Errors: " + r.getErrorCount() + "  (see data/import_errors.csv)" : "");
            loadStudents();
            setStatus("Import complete. " + r.getSuccessCount() + " added.");
        });
        task.setOnFailed(e -> alert(Alert.AlertType.ERROR,"Import Failed", task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML private void onExportAll()           { doExport("students_export.csv",    studentService.getAllStudents(), "All students"); }
    @FXML private void onExportTopPerformers() { doExport("top_performers_export.csv", reportService.getTopPerformers(studentService.getAllStudents(),10,null,null), "Top performers"); }
    @FXML private void onExportAtRisk()        { doExport("at_risk_export.csv",     reportService.getAtRiskStudents(studentService.getAllStudents(), atRiskThreshold), "At-risk students"); }

    private void doExport(String fn, List<Student> list, String label) {
        Task<Void> task = new Task<>() { @Override protected Void call() throws IOException { studentService.exportListToCsv(fn, list); return null; } };
        task.setOnSucceeded(e -> { lblExportStatus.setText("✓ " + label + " exported → data/" + fn); setStatus("Export saved: " + fn); });
        task.setOnFailed(e -> { lblExportStatus.setText("✗ Export failed: " + task.getException().getMessage()); });
        new Thread(task).start();
    }

    // ═══════════════════════════════
    //  SETTINGS
    // ═══════════════════════════════

    @FXML private void onSaveThreshold() {
        try {
            double v = Double.parseDouble(txtThreshold.getText().trim());
            if (!validationService.isValidGpaThreshold(v)) { lblThresholdMsg.setText("✗ Must be 0.0 – 4.0"); return; }
            atRiskThreshold = v;
            lblThresholdMsg.setText("✓ Saved");
            lblThresholdMsg.setStyle("-fx-text-fill:#059669;");
            AppLogger.info("At-risk threshold set to " + v);
        } catch (NumberFormatException ex) {
            lblThresholdMsg.setText("✗ Invalid number");
            lblThresholdMsg.setStyle("-fx-text-fill:#dc2626;");
        }
    }

    // ═══════════════════════════════
    //  HELPERS
    // ═══════════════════════════════

    private void setStatus(String msg) {
        if (lblStatusMsg != null) lblStatusMsg.setText(msg);
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
