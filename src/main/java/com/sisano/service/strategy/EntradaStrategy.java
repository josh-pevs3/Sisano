package com.sisano.service.strategy;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Cita;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntradaStrategy implements IProcesamientoStrategy {

    @Override
    public void procesar(Cita cita) {
        String sql = "UPDATE Cita SET estado = 'EN_SALA' WHERE id_cita = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cita.getId());
            ps.executeUpdate();
            System.out.println("LOG: Paciente ingresó a sala de espera.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public String getNombreMovimiento() { return "REGISTRO DE LLEGADA"; }
}