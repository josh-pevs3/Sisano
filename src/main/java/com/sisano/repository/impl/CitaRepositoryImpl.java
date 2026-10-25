package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Cita;
import com.sisano.repository.ICitaRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaRepositoryImpl implements ICitaRepository {

    @Override
    public boolean guardar(Cita c) {
        String sql = "INSERT INTO Cita (id_paciente, id_disponibilidad, observaciones) VALUES (?,?,?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, c.getIdPaciente());
            ps.setInt(2, c.getIdAsignacion());
            ps.setString(3, c.getObservaciones());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Cita buscarPorDisponibilidad(int idDisponibilidad) {
        // Hacemos JOIN con Paciente para traer el nombre y mostrarlo en el calendario
        String sql = "SELECT c.id_cita, c.id_paciente, c.id_disponibilidad, c.observaciones, p.nombre AS nombre_paciente " +
                     "FROM Cita c " +
                     "JOIN Paciente p ON c.id_paciente = p.id_paciente " +
                     "WHERE c.id_disponibilidad = ?";
                     
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idDisponibilidad);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Cita c = new Cita();
                c.setId(rs.getInt("id_cita"));
                c.setIdPaciente(rs.getInt("id_paciente"));
                c.setIdAsignacion(rs.getInt("id_disponibilidad"));
                c.setObservaciones(rs.getString("observaciones"));
                
                // Aquí guardamos el nombre que trajimos con el JOIN
                c.setNombrePacienteAux(rs.getString("nombre_paciente"));
                
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean actualizarDisponibilidadCita(int idCita, int idNuevaDisponibilidad) {
        String sql = "UPDATE Cita SET id_disponibilidad = ? WHERE id_cita = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idNuevaDisponibilidad);
            ps.setInt(2, idCita);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idCita) {
        String sql = "DELETE FROM Cita WHERE id_cita = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}