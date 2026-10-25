package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.repository.IDashboardRepository;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardRepositoryImpl implements IDashboardRepository {

    public Map<String, Integer> obtenerEstadisticas() {
        Map<String, Integer> stats = new HashMap<>();
        Connection con = null;
        try {
            con = DatabaseConnection.getInstance().getConnection();
            
            stats.put("pacientes", contar(con, "SELECT COUNT(*) FROM Paciente"));
            stats.put("medicos", contar(con, "SELECT COUNT(*) FROM Medico"));
            stats.put("salas", contar(con, "SELECT COUNT(*) FROM Sala WHERE estado = 1"));
            
            // Salas asignadas desde hoy en adelante (Disponibilidad futura)
            // SQL Server: GETDATE()
            stats.put("asignaciones", contar(con, "SELECT COUNT(*) FROM Disponibilidad WHERE fecha >= CAST(GETDATE() AS DATE)"));
            
            // Total Citas Reservadas
            stats.put("citas", contar(con, "SELECT COUNT(*) FROM Cita"));
            
            // Citas para HOY (Indicador extra muy útil)
            stats.put("citasHoy", contar(con, 
                "SELECT COUNT(*) FROM Cita c " +
                "JOIN Disponibilidad d ON c.id_disponibilidad = d.id_disponibilidad " +
                "WHERE d.fecha = CAST(GETDATE() AS DATE)"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private int contar(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}