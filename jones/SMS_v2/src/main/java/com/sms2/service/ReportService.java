package com.sms2.service;

import com.sms2.domain.Student;
import java.util.*;
import java.util.stream.*;

public class ReportService {

    public List<Student> getTopPerformers(List<Student> students, int topN, String programme, Integer level) {
        return students.stream()
                .filter(s -> programme==null||programme.isBlank()||s.getProgramme().equalsIgnoreCase(programme))
                .filter(s -> level==null||s.getLevel()==level)
                .sorted(Comparator.comparingDouble(Student::getGpa).reversed())
                .limit(topN).collect(Collectors.toList());
    }

    public List<Student> getAtRiskStudents(List<Student> students, double threshold) {
        return students.stream().filter(s -> s.getGpa()<threshold)
                .sorted(Comparator.comparingDouble(Student::getGpa)).collect(Collectors.toList());
    }

    public Map<String, Long> getGpaDistribution(List<Student> students) {
        Map<String,Long> d = new LinkedHashMap<>();
        d.put("0.0 – 1.0",0L); d.put("1.0 – 2.0",0L); d.put("2.0 – 3.0",0L); d.put("3.0 – 4.0",0L);
        for (Student s : students) {
            double g = s.getGpa();
            if (g<1.0) d.merge("0.0 – 1.0",1L,Long::sum);
            else if (g<2.0) d.merge("1.0 – 2.0",1L,Long::sum);
            else if (g<3.0) d.merge("2.0 – 3.0",1L,Long::sum);
            else d.merge("3.0 – 4.0",1L,Long::sum);
        }
        return d;
    }

    public Map<String,double[]> getProgrammeSummary(List<Student> students) {
        Map<String,List<Student>> byProg = students.stream().collect(Collectors.groupingBy(Student::getProgramme));
        Map<String,double[]> summary = new LinkedHashMap<>();
        byProg.forEach((p,list) -> summary.put(p, new double[]{list.size(),
                list.stream().mapToDouble(Student::getGpa).average().orElse(0.0)}));
        return summary;
    }

    public double getAverageGpa(List<Student> students) {
        if (students.isEmpty()) return 0.0;
        return students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
    }

    public long countActive(List<Student> students) {
        return students.stream().filter(s -> s.getStatus()==Student.StudentStatus.ACTIVE).count();
    }

    public long countInactive(List<Student> students) {
        return students.stream().filter(s -> s.getStatus()==Student.StudentStatus.INACTIVE).count();
    }
}
