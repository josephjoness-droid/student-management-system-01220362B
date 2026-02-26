package com.sms2.service;

import com.sms2.domain.Student;
import java.util.*;

public class ValidationService {
    private static final Set<Integer> VALID_LEVELS = Set.of(100,200,300,400,500,600,700);

    public List<String> validate(Student s) {
        List<String> e = new ArrayList<>();
        validateStudentId(s.getStudentId(), e);
        validateFullName(s.getFullName(), e);
        validateProgramme(s.getProgramme(), e);
        validateLevel(s.getLevel(), e);
        validateGpa(s.getGpa(), e);
        validateEmail(s.getEmail(), e);
        validatePhone(s.getPhoneNumber(), e);
        return e;
    }

    public void validateStudentId(String id, List<String> e) {
        if (id==null||id.isBlank()) { e.add("Student ID is required."); return; }
        if (id.length()<4||id.length()>20) e.add("Student ID must be 4–20 characters.");
        if (!id.matches("[A-Za-z0-9]+")) e.add("Student ID must contain only letters and digits.");
    }

    public void validateFullName(String n, List<String> e) {
        if (n==null||n.isBlank()) { e.add("Full name is required."); return; }
        if (n.length()<2||n.length()>60) e.add("Full name must be 2–60 characters.");
        if (n.matches(".*\\d.*")) e.add("Full name must not contain digits.");
    }

    public void validateProgramme(String p, List<String> e) {
        if (p==null||p.isBlank()) e.add("Programme is required.");
    }

    public void validateLevel(int l, List<String> e) {
        if (!VALID_LEVELS.contains(l)) e.add("Level must be one of: 100,200,300,400,500,600,700.");
    }

    public void validateGpa(double g, List<String> e) {
        if (g<0.0||g>4.0) e.add("GPA must be between 0.0 and 4.0.");
    }

    public void validateEmail(String em, List<String> e) {
        if (em==null||em.isBlank()) { e.add("Email is required."); return; }
        if (!em.contains("@")||!em.contains(".")) e.add("Email must contain '@' and '.'.");
    }

    public void validatePhone(String ph, List<String> e) {
        if (ph==null||ph.isBlank()) { e.add("Phone number is required."); return; }
        if (!ph.matches("\\d+")) e.add("Phone number must contain digits only.");
        if (ph.length()<10||ph.length()>15) e.add("Phone must be 10–15 digits.");
    }

    public boolean isValid(Student s) { return validate(s).isEmpty(); }
    public boolean isValidGpaThreshold(double t) { return t>=0.0&&t<=4.0; }
}
