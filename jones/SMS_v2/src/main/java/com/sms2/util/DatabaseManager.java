package com.sms2.util;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_FILE = "data/students.db";
    private static Connection connection;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            new File("data").mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            initSchema(connection);
        }
        return connection;
    }

    private static void initSchema(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS students (
                student_id   TEXT PRIMARY KEY,
                full_name    TEXT NOT NULL,
                programme    TEXT NOT NULL,
                level        INTEGER NOT NULL CHECK(level IN (100,200,300,400,500,600,700)),
                gpa          REAL NOT NULL CHECK(gpa >= 0.0 AND gpa <= 4.0),
                email        TEXT NOT NULL,
                phone_number TEXT NOT NULL,
                date_added   TEXT NOT NULL,
                status       TEXT NOT NULL DEFAULT 'ACTIVE'
            );
            """;
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL;");
            s.execute(sql);
        }
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            AppLogger.error("DB close error: " + e.getMessage());
        }
    }
}
