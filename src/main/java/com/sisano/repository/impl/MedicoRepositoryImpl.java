package com.sisano.repository.impl;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.Medico;
import com.sisano.repository.IMedicoRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicoRepositoryImpl implements IMedicoRepository {

    @Override
    public List<Medico> listarTodos() {
        List<Medico> lista = new ArrayList<>();
        String sql = "SELECT * FROM Medico";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                lista.add(new Medico(
                    rs.getInt("id_medico"), 
                    rs.getString("codigo"), 
                    rs.getString("nombre"), 
                    rs.getString("especialidad"), 
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("sexo")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public boolean guardar(Medico m) {
        String sql = "INSERT INTO Medico (codigo, nombre, especialidad, email, telefono, sexo) VALUES (?,?,?,?,?,?)";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Generar código automático solo si viene vacío
            String codigoAuto = (m.getCodigo() == null || m.getCodigo().isEmpty()) 
                                ? generarSiguienteCodigo(con) 
                                : m.getCodigo();
            
            ps.setString(1, codigoAuto);
            ps.setString(2, m.getNombre());
            ps.setString(3, m.getEspecialidad());
            ps.setString(4, m.getEmail());
            ps.setString(5, m.getTelefono());
            ps.setString(6, m.getSexo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    // --- NUEVO MÉTODO ACTUALIZAR (UPDATE) ---
    @Override
    public boolean actualizar(Medico m) {
        // No actualizamos el CÓDIGO (es identificador único visual), solo los datos.
        // Usamos el ID (clave primaria oculta) para el WHERE.
        String sql = "UPDATE Medico SET nombre=?, especialidad=?, email=?, telefono=?, sexo=? WHERE id_medico=?";
        
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getEspecialidad());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getTelefono());
            ps.setString(5, m.getSexo());
            ps.setInt(6, m.getId()); // Importante: WHERE id_medico = ?
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Medico WHERE id_medico=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // --- GENERADOR DE CÓDIGO MEJORADO ---
    // Usamos MAX(id) en lugar de COUNT(*) para evitar duplicados si borras registros.
    private String generarSiguienteCodigo(Connection con) throws SQLException {
        String sql = "SELECT MAX(id_medico) FROM Medico"; 
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                // Obtenemos el último ID insertado y le sumamos 1
                int maxId = rs.getInt(1); 
                int siguiente = maxId + 1;
                return "DOC-" + String.format("%03d", siguiente); // Ej: DOC-005
            }
        }
        return "DOC-001";
    }
}