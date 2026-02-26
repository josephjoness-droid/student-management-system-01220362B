package com.sms2.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {
    private static final String LOG_FILE = "data/app.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {}

    public static void info(String msg)  { log("INFO ", msg); }
    public static void error(String msg) { log("ERROR", msg); }
    public static void warn(String msg)  { log("WARN ", msg); }

    private static void log(String level, String msg) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] [" + level + "] " + msg;
        System.out.println(line);
        try {
            new java.io.File("data").mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                pw.println(line);
            }
        } catch (IOException e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }
}
