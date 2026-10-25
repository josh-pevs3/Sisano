package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadRepositoryImpl {

    public List<String> listarNombres() {
        List<String> lista = new ArrayList<>();
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT nombre FROM Especialidad ORDER BY nombre");
            while(rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean guardar(String nombre) {
        // Verifica si ya existe para no duplicar
        try (Connection con = DatabaseConnection.getInstance().getConnection()) {
            // 1. Verificar existencia
            PreparedStatement check = con.prepareStatement("SELECT COUNT(*) FROM Especialidad WHERE nombre = ?");
            check.setString(1, nombre);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false; // Ya existe

            // 2. Insertar
            PreparedStatement ps = con.prepareStatement("INSERT INTO Especialidad (nombre) VALUES (?)");
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }
}