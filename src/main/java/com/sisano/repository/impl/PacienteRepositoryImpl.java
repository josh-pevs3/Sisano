package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Paciente;
import com.sisano.repository.IPacienteRepository; // Asumo que crearás esta interfaz igual que las anteriores
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteRepositoryImpl implements IPacienteRepository {
    @Override
    public Paciente buscarPorDni(String dni) {
        String sql = "SELECT * FROM Paciente WHERE dni = ?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Recuperamos y convertimos la fecha
                Date dateSql = rs.getDate("fecha_nacimiento");
                java.time.LocalDate fechaNac = (dateSql != null) ? dateSql.toLocalDate() : null;

                return new Paciente(
                    rs.getInt("id_paciente"),
                    rs.getString("dni"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    fechaNac,
                    rs.getString("genero")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; // Retorna null si no existe
    }

    @Override
    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Paciente";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                // Conversión segura de fecha SQL a LocalDate
                Date dateSql = rs.getDate("fecha_nacimiento");
                java.time.LocalDate fechaNac = (dateSql != null) ? dateSql.toLocalDate() : null;

                lista.add(new Paciente(
                    rs.getInt("id_paciente"), // Asegúrate que tu columna PK se llame así
                    rs.getString("dni"),
                    rs.getString("nombre"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    fechaNac,
                    rs.getString("genero")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean guardar(Paciente p) {
        String sql = "INSERT INTO Paciente (dni, nombre, email, telefono, fecha_nacimiento, genero) VALUES (?,?,?,?,?,?)";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getDni());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelefono());
            
            // Convertir LocalDate a SQL Date
            ps.setDate(5, p.getFechaNacimiento() != null ? Date.valueOf(p.getFechaNacimiento()) : null);
            ps.setString(6, p.getGenero());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean actualizar(Paciente p) {
        String sql = "UPDATE Paciente SET dni=?, nombre=?, email=?, telefono=?, fecha_nacimiento=?, genero=? WHERE id_paciente=?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getDni());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getTelefono());
            ps.setDate(5, p.getFechaNacimiento() != null ? Date.valueOf(p.getFechaNacimiento()) : null);
            ps.setString(6, p.getGenero());
            ps.setInt(7, p.getId()); // WHERE id = ?
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean eliminar(int id) {
         try (Connection con = DatabaseConnection.getInstance().getConnection();
              PreparedStatement ps = con.prepareStatement("DELETE FROM Paciente WHERE id_paciente=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}