package edu.pucmm.eict.services;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthLogger {

    private final String jdbcUrl;

    public AuthLogger() {
        // Lee la URL de conexión desde la variable de ambiente
        this.jdbcUrl = System.getenv("JDBC_DATABASE_URL");
        if (this.jdbcUrl == null || this.jdbcUrl.isEmpty()) {
            throw new RuntimeException("La variable de ambiente JDBC_DATABASE_URL no esta definida.");
        }
    }

    public void logAuthEvent(String username) {
        // Consulta parametrizada para evitar inyección SQL
        String sql = "INSERT INTO auth_log (username, login_time) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            conn.setAutoCommit(true);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                System.out.println("Registro de autenticación insertado para " + username);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
