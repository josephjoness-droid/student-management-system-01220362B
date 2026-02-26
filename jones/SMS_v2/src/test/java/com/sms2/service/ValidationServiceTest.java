package com.sms2.service;

import com.sms2.domain.Student;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService vs;

    @BeforeEach void setUp() { vs = new ValidationService(); }

    @Test @DisplayName("Valid student ID passes")
    void validId() { List<String> e = new ArrayList<>(); vs.validateStudentId("STU001", e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("Short ID fails")
    void shortId() { List<String> e = new ArrayList<>(); vs.validateStudentId("AB", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("ID over 20 chars fails")
    void longId() { List<String> e = new ArrayList<>(); vs.validateStudentId("ABCDEFGHIJKLMNOPQRSTU", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("ID with special chars fails")
    void specialId() { List<String> e = new ArrayList<>(); vs.validateStudentId("STU-001!", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Blank ID fails")
    void blankId() { List<String> e = new ArrayList<>(); vs.validateStudentId("", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Valid name passes")
    void validName() { List<String> e = new ArrayList<>(); vs.validateFullName("Alice Mensah", e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("Name with digits fails")
    void nameDigits() { List<String> e = new ArrayList<>(); vs.validateFullName("Alice2", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Name too short fails")
    void nameTooShort() { List<String> e = new ArrayList<>(); vs.validateFullName("A", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Valid level 200 passes")
    void validLevel() { List<String> e = new ArrayList<>(); vs.validateLevel(200, e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("Invalid level 250 fails")
    void invalidLevel() { List<String> e = new ArrayList<>(); vs.validateLevel(250, e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("GPA 3.5 passes")
    void validGpa() { List<String> e = new ArrayList<>(); vs.validateGpa(3.5, e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("GPA 4.5 fails")
    void highGpa() { List<String> e = new ArrayList<>(); vs.validateGpa(4.5, e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Negative GPA fails")
    void negGpa() { List<String> e = new ArrayList<>(); vs.validateGpa(-0.1, e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Valid email passes")
    void validEmail() { List<String> e = new ArrayList<>(); vs.validateEmail("a@b.com", e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("Email without @ fails")
    void emailNoAt() { List<String> e = new ArrayList<>(); vs.validateEmail("ab.com", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Valid phone passes")
    void validPhone() { List<String> e = new ArrayList<>(); vs.validatePhone("0241234567", e); assertTrue(e.isEmpty()); }

    @Test @DisplayName("Phone with letters fails")
    void phoneLetters() { List<String> e = new ArrayList<>(); vs.validatePhone("024ABC4567", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Phone too short fails")
    void phoneTooShort() { List<String> e = new ArrayList<>(); vs.validatePhone("024123", e); assertFalse(e.isEmpty()); }

    @Test @DisplayName("Fully valid student passes all checks")
    void fullValid() {
        Student s = new Student("STU001","Alice Smith","CS",300,3.5,"a@b.com","0241234567");
        assertTrue(vs.isValid(s));
    }

    @Test @DisplayName("Invalid student fails")
    void fullInvalid() {
        Student s = new Student(); s.setStudentId(""); s.setFullName("X"); s.setProgramme("");
        s.setLevel(999); s.setGpa(5.0); s.setEmail("bad"); s.setPhoneNumber("abc");
        assertFalse(vs.isValid(s));
    }

    @Test @DisplayName("GPA threshold 2.0 is valid")
    void validThreshold() { assertTrue(vs.isValidGpaThreshold(2.0)); }

    @Test @DisplayName("GPA threshold -1 is invalid")
    void invalidThreshold() { assertFalse(vs.isValidGpaThreshold(-1)); }
}
