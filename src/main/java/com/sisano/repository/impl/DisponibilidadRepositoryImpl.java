package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Disponibilidad;
import com.sisano.repository.IDisponibilidadRepository;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadRepositoryImpl implements IDisponibilidadRepository {

    @Override
    public List<Disponibilidad> buscarPorSalaYRango(int idSala, LocalDate inicio, LocalDate fin) {
        List<Disponibilidad> lista = new ArrayList<>();
        String sql = "SELECT * FROM Disponibilidad WHERE id_sala = ? AND fecha BETWEEN ? AND ?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idSala);
            ps.setDate(2, Date.valueOf(inicio));
            ps.setDate(3, Date.valueOf(fin));
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Disponibilidad d = new Disponibilidad();
                d.setId(rs.getInt("id_disponibilidad"));
                d.setIdMedico(rs.getInt("id_medico"));
                d.setIdSala(rs.getInt("id_sala"));
                d.setFecha(rs.getDate("fecha").toLocalDate());
                // Convertir Time a LocalTime
                Time tInicio = rs.getTime("hora_inicio");
                Time tFin = rs.getTime("hora_fin");
                d.setHoraInicio(tInicio != null ? tInicio.toLocalTime() : null);
                d.setHoraFin(tFin != null ? tFin.toLocalTime() : null);
                d.setEstado(rs.getString("estado"));
                lista.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean guardar(Disponibilidad d) {
        Connection con = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        
        try {
            con = DatabaseConnection.getInstance().getConnection();
            if (con == null || con.isClosed()) return false;

            con.setAutoCommit(false); 

        
            // Quitamos segundos y nanosegundos para evitar errores de comparación
            LocalTime horaLimpia = d.getHoraInicio().truncatedTo(ChronoUnit.MINUTES);
            LocalTime finLimpio = d.getHoraFin().truncatedTo(ChronoUnit.MINUTES);

            // ELIMINAR (Usamos CAST para evitar el error 'incompatible time/datetime')
            String sqlDelete = "DELETE FROM Disponibilidad WHERE id_sala = ? AND fecha = ? AND CAST(hora_inicio AS TIME(0)) = CAST(? AS TIME(0))";
            psDelete = con.prepareStatement(sqlDelete);
            psDelete.setInt(1, d.getIdSala());
            psDelete.setDate(2, Date.valueOf(d.getFecha()));
            // Enviamos la hora como String para que el CAST de SQL la interprete correctamente
            psDelete.setString(3, horaLimpia.toString() + ":00"); 
            
            psDelete.executeUpdate();

            // INSERTAR
            String sqlInsert = "INSERT INTO Disponibilidad (id_medico, id_sala, fecha, hora_inicio, hora_fin, estado) VALUES (?,?,?,?,?,?)";
            psInsert = con.prepareStatement(sqlInsert);
            psInsert.setInt(1, d.getIdMedico());
            psInsert.setInt(2, d.getIdSala());
            psInsert.setDate(3, Date.valueOf(d.getFecha()));
            psInsert.setTime(4, Time.valueOf(horaLimpia));
            psInsert.setTime(5, Time.valueOf(finLimpio));
            psInsert.setString(6, "ASIGNADO");
            
            int filas = psInsert.executeUpdate();
            
            con.commit(); 
            return filas > 0;

        } catch (SQLException e) { 
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("ERROR AL GUARDAR: " + e.getMessage());
            e.printStackTrace(); 
            return false; 
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
                if (psDelete != null) psDelete.close();
                if (psInsert != null) psInsert.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public boolean eliminar(int idSala, LocalDate fecha, LocalTime horaInicio) {
        
        String sql = "DELETE FROM Disponibilidad WHERE id_sala = ? AND fecha = ? AND CAST(hora_inicio AS TIME(0)) = CAST(? AS TIME(0))";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idSala);
            ps.setDate(2, Date.valueOf(fecha));
            LocalTime horaLimpia = horaInicio.truncatedTo(ChronoUnit.MINUTES);
            ps.setString(3, horaLimpia.toString() + ":00");
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public boolean eliminar(int idDisponibilidad) {
        String sql = "DELETE FROM Disponibilidad WHERE id_disponibilidad = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDisponibilidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) { // FK Conflict
                System.err.println("No se puede borrar: Hay citas asociadas.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean actualizarEstado(int idDisponibilidad, String nuevoEstado) {
        String sql = "UPDATE Disponibilidad SET estado = ? WHERE id_disponibilidad = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idDisponibilidad);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}