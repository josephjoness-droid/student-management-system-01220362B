package com.sms2.domain;

import java.time.LocalDate;

public class Student {

    private String studentId;
    private String fullName;
    private String programme;
    private int level;
    private double gpa;
    private String email;
    private String phoneNumber;
    private LocalDate dateAdded;
    private StudentStatus status;

    public enum StudentStatus { ACTIVE, INACTIVE }

    public Student() {
        this.dateAdded = LocalDate.now();
        this.status = StudentStatus.ACTIVE;
    }

    public Student(String studentId, String fullName, String programme, int level,
                   double gpa, String email, String phoneNumber) {
        this();
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.level = level;
        this.gpa = gpa;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getStudentId()      { return studentId; }
    public String getFullName()       { return fullName; }
    public String getProgramme()      { return programme; }
    public int getLevel()             { return level; }
    public double getGpa()            { return gpa; }
    public String getEmail()          { return email; }
    public String getPhoneNumber()    { return phoneNumber; }
    public LocalDate getDateAdded()   { return dateAdded; }
    public StudentStatus getStatus()  { return status; }

    public void setStudentId(String v)      { this.studentId = v; }
    public void setFullName(String v)       { this.fullName = v; }
    public void setProgramme(String v)      { this.programme = v; }
    public void setLevel(int v)             { this.level = v; }
    public void setGpa(double v)            { this.gpa = v; }
    public void setEmail(String v)          { this.email = v; }
    public void setPhoneNumber(String v)    { this.phoneNumber = v; }
    public void setDateAdded(LocalDate v)   { this.dateAdded = v; }
    public void setStatus(StudentStatus v)  { this.status = v; }
}
