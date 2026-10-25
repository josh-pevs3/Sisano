package com.sisano.service.strategy;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Cita;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SalidaStrategy implements IProcesamientoStrategy {

    @Override
    public void procesar(Cita cita) {
        String sql = "UPDATE Cita SET estado = 'FINALIZADA' WHERE id_cita = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cita.getId());
            ps.executeUpdate();
            System.out.println("Cita finalizada. Generando orden de cobro...");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public String getNombreMovimiento() { return "FINALIZACIÓN Y COBRO"; }
}