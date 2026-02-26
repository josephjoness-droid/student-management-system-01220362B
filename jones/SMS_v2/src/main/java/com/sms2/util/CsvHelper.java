package com.sms2.util;

import com.sms2.domain.Student;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvHelper {
    private static final String DATA = "data";
    private CsvHelper() {}

    public static void exportStudents(String filename, List<Student> students) throws IOException {
        Path out = Paths.get(DATA, filename);
        Files.createDirectories(out.getParent());
        try (PrintWriter w = new PrintWriter(new FileWriter(out.toFile()))) {
            w.println("StudentID,FullName,Programme,Level,GPA,Email,PhoneNumber,DateAdded,Status");
            for (Student s : students) {
                w.printf("%s,%s,%s,%d,%.2f,%s,%s,%s,%s%n",
                        esc(s.getStudentId()), esc(s.getFullName()), esc(s.getProgramme()),
                        s.getLevel(), s.getGpa(), esc(s.getEmail()),
                        esc(s.getPhoneNumber()), s.getDateAdded(), s.getStatus().name());
            }
        }
        AppLogger.info("Exported " + students.size() + " records to " + out);
    }

    public static void exportErrors(String filename, List<String> errors) throws IOException {
        Path out = Paths.get(DATA, filename);
        Files.createDirectories(out.getParent());
        try (PrintWriter w = new PrintWriter(new FileWriter(out.toFile()))) {
            w.println("Row,Error");
            for (String e : errors) w.println(esc(e));
        }
    }

    public static List<String[]> readCsv(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line; boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; }
                if (!line.isBlank()) rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (char c : line.toCharArray()) {
            if (c == '"') { inQ = !inQ; }
            else if (c == ',' && !inQ) { fields.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(c);
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }

    private static String esc(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"")) return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
