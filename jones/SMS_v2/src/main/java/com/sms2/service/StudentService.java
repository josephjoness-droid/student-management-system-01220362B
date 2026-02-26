package com.sms2.service;

import com.sms2.domain.ImportResult;
import com.sms2.domain.Student;
import com.sms2.repository.StudentRepository;
import com.sms2.util.AppLogger;
import com.sms2.util.CsvHelper;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class StudentService {
    private final StudentRepository repo;
    private final ValidationService validator;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
        this.validator = new ValidationService();
    }

    public List<String> addStudent(Student s) {
        List<String> errors = validator.validate(s);
        if (!errors.isEmpty()) return errors;
        if (repo.existsById(s.getStudentId())) { errors.add("Student ID '"+s.getStudentId()+"' already exists."); return errors; }
        s.setDateAdded(LocalDate.now());
        if (!repo.add(s)) errors.add("Failed to save. Please try again.");
        else AppLogger.info("Student added: ID="+s.getStudentId());
        return errors;
    }

    public List<String> updateStudent(Student s) {
        List<String> errors = validator.validate(s);
        if (!errors.isEmpty()) return errors;
        if (!repo.update(s)) errors.add("Failed to update. Record may not exist.");
        else AppLogger.info("Student updated: ID="+s.getStudentId());
        return errors;
    }

    public boolean deleteStudent(String id) {
        boolean ok = repo.delete(id);
        if (ok) AppLogger.info("Student deleted: ID="+id);
        else AppLogger.error("Delete failed: ID="+id);
        return ok;
    }

    public Optional<Student> findById(String id)                      { return repo.findById(id); }
    public List<Student> getAllStudents()                              { return repo.findAll(); }
    public List<Student> searchStudents(String q)                     { return repo.search(q); }
    public List<Student> filterStudents(String p, Integer l, String s){ return repo.filter(p,l,s); }
    public List<String> getAllProgrammes()                             { return repo.getAllProgrammes(); }

    public List<Student> getSortedByGpa(List<Student> list, boolean asc) {
        List<Student> s = new ArrayList<>(list);
        s.sort(asc ? Comparator.comparingDouble(Student::getGpa)
                   : Comparator.comparingDouble(Student::getGpa).reversed());
        return s;
    }

    public List<Student> getSortedByName(List<Student> list) {
        List<Student> s = new ArrayList<>(list);
        s.sort(Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER));
        return s;
    }

    public ImportResult importFromCsv(File file) {
        ImportResult result = new ImportResult();
        List<String[]> rows;
        try { rows = CsvHelper.readCsv(file); }
        catch (IOException e) { result.addError("Cannot read file: "+e.getMessage()); return result; }
        int row = 1;
        for (String[] r : rows) {
            row++;
            try {
                Student s = parseRow(r);
                List<String> errs = validator.validate(s);
                if (!errs.isEmpty()) { result.addError("Row "+row+": "+String.join("; ",errs)); continue; }
                if (repo.existsById(s.getStudentId())) { result.addError("Row "+row+": Duplicate ID '"+s.getStudentId()+"'"); continue; }
                repo.add(s); result.incrementSuccess();
            } catch (Exception e) { result.addError("Row "+row+": "+e.getMessage()); }
        }
        AppLogger.info("Import done. Success="+result.getSuccessCount()+" Errors="+result.getErrorCount());
        if (result.getErrorCount()>0) {
            try { CsvHelper.exportErrors("import_errors.csv",result.getErrors()); }
            catch (IOException e) { AppLogger.error("Cannot write error report: "+e.getMessage()); }
        }
        return result;
    }

    private Student parseRow(String[] r) {
        if (r.length<8) throw new IllegalArgumentException("Not enough columns");
        Student s = new Student();
        s.setStudentId(r[0].trim()); s.setFullName(r[1].trim()); s.setProgramme(r[2].trim());
        s.setLevel(Integer.parseInt(r[3].trim())); s.setGpa(Double.parseDouble(r[4].trim()));
        s.setEmail(r[5].trim()); s.setPhoneNumber(r[6].trim());
        s.setDateAdded(r[7].trim().isEmpty() ? LocalDate.now() : LocalDate.parse(r[7].trim()));
        if (r.length>8&&!r[8].isBlank()) s.setStatus(Student.StudentStatus.valueOf(r[8].trim().toUpperCase()));
        return s;
    }

    public void exportAllToCsv(String fn) throws IOException {
        List<Student> all = repo.findAll();
        CsvHelper.exportStudents(fn, all);
        AppLogger.info("Export: "+fn+" ("+all.size()+" records)");
    }

    public void exportListToCsv(String fn, List<Student> list) throws IOException {
        CsvHelper.exportStudents(fn, list);
        AppLogger.info("Export: "+fn+" ("+list.size()+" records)");
    }
}
