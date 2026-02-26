package com.sms2.repository;

import com.sms2.domain.Student;
import com.sms2.util.DatabaseManager;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SqliteStudentRepositoryTest {

    private SqliteStudentRepository repo;

    @BeforeEach void setUp() throws SQLException {
        repo = new SqliteStudentRepository();
        try (Connection c = DatabaseManager.getConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM students");
        }
    }

    @AfterAll static void tearDown() { DatabaseManager.close(); }

    private Student make(String id) {
        return new Student(id,"Test Student","CS",300,3.0,id+"@uni.edu","0241234567");
    }

    @Test @Order(1) @DisplayName("Add returns true, student retrievable")
    void addAndFind() {
        assertTrue(repo.add(make("T001")));
        assertTrue(repo.findById("T001").isPresent());
    }

    @Test @Order(2) @DisplayName("Duplicate ID returns false")
    void duplicateId() {
        repo.add(make("T002"));
        assertFalse(repo.add(make("T002")));
    }

    @Test @Order(3) @DisplayName("FindById returns empty for unknown")
    void findUnknown() { assertFalse(repo.findById("GHOST").isPresent()); }

    @Test @Order(4) @DisplayName("FindAll returns all added")
    void findAll() {
        repo.add(make("T010")); repo.add(make("T011")); repo.add(make("T012"));
        assertEquals(3, repo.findAll().size());
    }

    @Test @Order(5) @DisplayName("Update changes values")
    void update() {
        Student s = make("T020"); repo.add(s);
        s.setFullName("Updated"); s.setGpa(3.9); assertTrue(repo.update(s));
        Student found = repo.findById("T020").orElseThrow();
        assertEquals("Updated", found.getFullName());
        assertEquals(3.9, found.getGpa(), 0.001);
    }

    @Test @Order(6) @DisplayName("Delete removes student")
    void delete() {
        repo.add(make("T030")); assertTrue(repo.delete("T030"));
        assertFalse(repo.findById("T030").isPresent());
    }

    @Test @Order(7) @DisplayName("Delete non-existent returns false")
    void deleteGhost() { assertFalse(repo.delete("GHOST999")); }

    @Test @Order(8) @DisplayName("Search by name finds student")
    void searchName() {
        Student s = make("T040"); s.setFullName("Kwame Mensah"); repo.add(s);
        assertFalse(repo.search("Mensah").isEmpty());
    }

    @Test @Order(9) @DisplayName("Search by ID finds student")
    void searchId() {
        repo.add(make("T050"));
        assertFalse(repo.search("T050").isEmpty());
    }

    @Test @Order(10) @DisplayName("existsById true for existing")
    void existsTrue() { repo.add(make("T060")); assertTrue(repo.existsById("T060")); }

    @Test @Order(11) @DisplayName("existsById false for missing")
    void existsFalse() { assertFalse(repo.existsById("NOTHERE")); }

    @Test @Order(12) @DisplayName("Filter by programme returns correct students")
    void filterProgramme() {
        Student s1 = make("T070"); s1.setProgramme("Math"); repo.add(s1);
        Student s2 = make("T071"); s2.setProgramme("Phys"); repo.add(s2);
        repo.filter("Math",null,null).forEach(s -> assertEquals("Math",s.getProgramme()));
    }
}
