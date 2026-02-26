package com.sms2.repository;

import com.sms2.domain.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    boolean add(Student student);
    Optional<Student> findById(String studentId);
    List<Student> findAll();
    boolean update(Student student);
    boolean delete(String studentId);
    List<Student> search(String query);
    List<Student> filter(String programme, Integer level, String status);
    boolean existsById(String studentId);
    List<String> getAllProgrammes();
}
