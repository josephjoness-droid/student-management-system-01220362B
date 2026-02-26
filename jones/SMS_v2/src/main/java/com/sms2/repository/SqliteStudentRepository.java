package com.sms2.repository;

import com.sms2.domain.Student;
import com.sms2.util.AppLogger;
import com.sms2.util.DatabaseManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class SqliteStudentRepository implements StudentRepository {

    @Override
    public boolean add(Student s) {
        String sql = "INSERT INTO students(student_id,full_name,programme,level,gpa,email,phone_number,date_added,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,s.getStudentId()); p.setString(2,s.getFullName()); p.setString(3,s.getProgramme());
            p.setInt(4,s.getLevel()); p.setDouble(5,s.getGpa()); p.setString(6,s.getEmail());
            p.setString(7,s.getPhoneNumber()); p.setString(8,s.getDateAdded().toString()); p.setString(9,s.getStatus().name());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { AppLogger.error("Add error: "+e.getMessage()); return false; }
    }

    @Override
    public Optional<Student> findById(String id) {
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM students WHERE student_id=?")) {
            p.setString(1,id); ResultSet rs = p.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) { AppLogger.error("FindById error: "+e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM students ORDER BY full_name ASC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { AppLogger.error("FindAll error: "+e.getMessage()); }
        return list;
    }

    @Override
    public boolean update(Student s) {
        String sql = "UPDATE students SET full_name=?,programme=?,level=?,gpa=?,email=?,phone_number=?,status=? WHERE student_id=?";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,s.getFullName()); p.setString(2,s.getProgramme()); p.setInt(3,s.getLevel());
            p.setDouble(4,s.getGpa()); p.setString(5,s.getEmail()); p.setString(6,s.getPhoneNumber());
            p.setString(7,s.getStatus().name()); p.setString(8,s.getStudentId());
            return p.executeUpdate() > 0;
        } catch (SQLException e) { AppLogger.error("Update error: "+e.getMessage()); return false; }
    }

    @Override
    public boolean delete(String id) {
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            p.setString(1,id); return p.executeUpdate() > 0;
        } catch (SQLException e) { AppLogger.error("Delete error: "+e.getMessage()); return false; }
    }

    @Override
    public List<Student> search(String query) {
        List<Student> list = new ArrayList<>();
        String ptn = "%"+query+"%";
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM students WHERE student_id LIKE ? OR full_name LIKE ?")) {
            p.setString(1,ptn); p.setString(2,ptn); ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { AppLogger.error("Search error: "+e.getMessage()); }
        return list;
    }

    @Override
    public List<Student> filter(String programme, Integer level, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (programme!=null && !programme.isBlank()) { sql.append(" AND programme=?"); params.add(programme); }
        if (level!=null) { sql.append(" AND level=?"); params.add(level); }
        if (status!=null && !status.isBlank()) { sql.append(" AND status=?"); params.add(status); }
        sql.append(" ORDER BY full_name ASC");
        List<Student> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) p.setObject(i+1,params.get(i));
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { AppLogger.error("Filter error: "+e.getMessage()); }
        return list;
    }

    @Override
    public boolean existsById(String id) {
        try (Connection c = DatabaseManager.getConnection(); PreparedStatement p = c.prepareStatement("SELECT 1 FROM students WHERE student_id=?")) {
            p.setString(1,id); return p.executeQuery().next();
        } catch (SQLException e) { AppLogger.error("Exists error: "+e.getMessage()); return false; }
    }

    @Override
    public List<String> getAllProgrammes() {
        List<String> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT DISTINCT programme FROM students ORDER BY programme ASC")) {
            while (rs.next()) list.add(rs.getString("programme"));
        } catch (SQLException e) { AppLogger.error("GetProgrammes error: "+e.getMessage()); }
        return list;
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getString("student_id")); s.setFullName(rs.getString("full_name"));
        s.setProgramme(rs.getString("programme")); s.setLevel(rs.getInt("level"));
        s.setGpa(rs.getDouble("gpa")); s.setEmail(rs.getString("email"));
        s.setPhoneNumber(rs.getString("phone_number")); s.setDateAdded(LocalDate.parse(rs.getString("date_added")));
        s.setStatus(Student.StudentStatus.valueOf(rs.getString("status")));
        return s;
    }
}
