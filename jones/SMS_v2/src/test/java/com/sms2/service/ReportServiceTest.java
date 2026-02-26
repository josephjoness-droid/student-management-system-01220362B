package com.sms2.service;

import com.sms2.domain.Student;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    private ReportService rs;
    private List<Student> students;

    @BeforeEach void setUp() {
        rs = new ReportService();
        students = List.of(
            mk("S1","Alice",  "CS",  300, 3.9, Student.StudentStatus.ACTIVE),
            mk("S2","Bob",    "CS",  200, 1.5, Student.StudentStatus.ACTIVE),
            mk("S3","Carol",  "Math",400, 2.8, Student.StudentStatus.ACTIVE),
            mk("S4","David",  "Math",300, 0.9, Student.StudentStatus.INACTIVE),
            mk("S5","Eve",    "Phys",100, 3.2, Student.StudentStatus.ACTIVE),
            mk("S6","Frank",  "Phys",200, 2.1, Student.StudentStatus.ACTIVE),
            mk("S7","Grace",  "CS",  400, 3.5, Student.StudentStatus.ACTIVE)
        );
    }

    private Student mk(String id, String name, String prog, int lvl, double gpa, Student.StudentStatus st) {
        Student s = new Student(id,name,prog,lvl,gpa,id+"@u.edu","0241234567");
        s.setStatus(st); return s;
    }

    @Test @DisplayName("Top performers limited correctly")
    void topPerformersCount() { assertEquals(3, rs.getTopPerformers(students,3,null,null).size()); }

    @Test @DisplayName("Top performers sorted desc by GPA")
    void topPerformersSorted() {
        List<Student> top = rs.getTopPerformers(students,7,null,null);
        for (int i=0;i<top.size()-1;i++) assertTrue(top.get(i).getGpa()>=top.get(i+1).getGpa());
    }

    @Test @DisplayName("Top performers filtered by programme")
    void topPerformersProgramme() {
        rs.getTopPerformers(students,10,"Phys",null).forEach(s -> assertEquals("Phys",s.getProgramme()));
    }

    @Test @DisplayName("Top performers filtered by level")
    void topPerformersLevel() {
        rs.getTopPerformers(students,10,null,300).forEach(s -> assertEquals(300,s.getLevel()));
    }

    @Test @DisplayName("At-risk all below threshold")
    void atRiskBelowThreshold() {
        rs.getAtRiskStudents(students,2.0).forEach(s -> assertTrue(s.getGpa()<2.0));
    }

    @Test @DisplayName("At-risk count correct for 2.0")
    void atRiskCount() { assertEquals(2, rs.getAtRiskStudents(students,2.0).size()); }

    @Test @DisplayName("At-risk empty when threshold very low")
    void atRiskEmpty() { assertTrue(rs.getAtRiskStudents(students,0.5).isEmpty()); }

    @Test @DisplayName("GPA distribution has 4 bands")
    void distBands() { assertEquals(4, rs.getGpaDistribution(students).size()); }

    @Test @DisplayName("GPA distribution sums to total")
    void distSum() {
        long total = rs.getGpaDistribution(students).values().stream().mapToLong(Long::longValue).sum();
        assertEquals(students.size(), total);
    }

    @Test @DisplayName("GPA distribution correct low band")
    void distLowBand() { assertTrue(rs.getGpaDistribution(students).get("0.0 – 1.0") >= 1); }

    @Test @DisplayName("Programme summary correct count")
    void progSummaryCount() { assertEquals(3, rs.getProgrammeSummary(students).size()); }

    @Test @DisplayName("Programme average GPA in valid range")
    void progAvgValid() {
        rs.getProgrammeSummary(students).forEach((p,v) -> { assertTrue(v[1]>=0); assertTrue(v[1]<=4.0); });
    }

    @Test @DisplayName("Average GPA correct range")
    void avgGpa() { double a = rs.getAverageGpa(students); assertTrue(a>0&&a<=4.0); }

    @Test @DisplayName("Count active correct")
    void countActive() { assertEquals(6, rs.countActive(students)); }

    @Test @DisplayName("Count inactive correct")
    void countInactive() { assertEquals(1, rs.countInactive(students)); }

    @Test @DisplayName("Average GPA empty list returns 0")
    void avgEmpty() { assertEquals(0.0, rs.getAverageGpa(List.of())); }
}
