package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Sala;
import com.sisano.repository.ISalaRepository; // Importamos la interfaz que acabamos de crear
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaRepositoryImpl implements ISalaRepository { // <--- AHORA SÍ IMPLEMENTA
    
    @Override
    public List<Sala> listarTodas() {
        List<Sala> lista = new ArrayList<>();
        // Asumimos que la columna en BD se llama 'estado'
        String sql = "SELECT * FROM Sala";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                lista.add(new Sala(
                    rs.getInt("id_sala"), 
                    rs.getString("nombre"), 
                    rs.getBoolean("estado") // Aquí leías 'estado'
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean guardar(Sala s) {
        // Aquí insertabas en 'estado'
        String sql = "INSERT INTO Sala (nombre, estado) VALUES (?,?)";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNombre());
            ps.setBoolean(2, s.isActiva());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace();
            return false; 
        }
    }

    @Override
    public boolean actualizar(Sala s) {
        // CORRECCIÓN: Antes tenías "activa", lo cambié a "estado" para que coincida con tu BD
        String sql = "UPDATE Sala SET nombre = ?, estado = ? WHERE id_sala = ?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, s.getNombre());
            ps.setBoolean(2, s.isActiva()); 
            ps.setInt(3, s.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean eliminar(int id) {
         try (Connection con = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = con.prepareStatement("DELETE FROM Sala WHERE id_sala=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}