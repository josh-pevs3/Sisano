package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Usuario;
import com.sisano.repository.IUsuarioRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepositoryImpl implements IUsuarioRepository {

    // --- MÉTODO QUE TE FALTABA PARA EL LOGIN ---
    @Override
    public Usuario validarLogin(String user, String pass) {
        // OJO: Usamos password_hash
        String sql = "SELECT * FROM Usuario WHERE username = ? AND password_hash = ?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass); // En un sistema real, aquí encriptaríamos 'pass' antes de comparar
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Si existe, creamos el objeto con TODOS los datos nuevos
                return new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("rol"),
                    rs.getString("nombre_completo")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login fallido
    }

    // --- MÉTODOS DEL CRUD (Que ya tenías, pero asegúrate que tengan @Override) ---

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM Usuario";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("rol"),
                    rs.getString("nombre_completo")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean guardar(Usuario u) {
        String sql = "INSERT INTO Usuario (username, password_hash, rol, nombre_completo) VALUES (?,?,?,?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRol());
            ps.setString(4, u.getNombreCompleto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE Usuario SET username=?, password_hash=?, rol=?, nombre_completo=? WHERE id_usuario=?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRol());
            ps.setString(4, u.getNombreCompleto());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Usuario WHERE id_usuario=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}