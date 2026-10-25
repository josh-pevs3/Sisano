package com.sisano.service.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Cita;
import com.sisano.service.ICitaService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CitaService implements ICitaService {

    @Override
    public boolean agendarCita(Cita cita) {
        String sqlCita = "INSERT INTO Cita (id_disponibilidad, id_paciente, estado, observaciones) VALUES (?, ?, 'PROGRAMADA', ?)";
        String sqlUpdateDisp = "UPDATE Disponibilidad SET estado = 'OCUPADO' WHERE id_disponibilidad = ?";

        Connection con = DatabaseConnection.getInstance().getConnection();
        try {
            con.setAutoCommit(false); // Transacción para asegurar consistencia

            // 1. Guardar la Cita
            try (PreparedStatement ps = con.prepareStatement(sqlCita)) {
                ps.setInt(1, cita.getIdAsignacion());
                ps.setInt(2, cita.getIdPaciente());
                ps.setString(3, cita.getObservaciones());
                ps.executeUpdate();
            }

            // 2. Marcar el horario como OCUPADO
            try (PreparedStatement ps2 = con.prepareStatement(sqlUpdateDisp)) {
                ps2.setInt(1, cita.getIdAsignacion());
                ps2.executeUpdate();
            }

            con.commit(); // Confirmar cambios
            return true;

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}