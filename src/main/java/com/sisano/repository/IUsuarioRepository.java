package com.sisano.repository;

import com.sisano.model.Usuario;
import java.util.List;

public interface IUsuarioRepository {
    // Para el Login
    Usuario validarLogin(String user, String pass);

    // Para la Gestión (CRUD)
    List<Usuario> listarTodos();
    boolean guardar(Usuario u);
    boolean actualizar(Usuario u);
    boolean eliminar(int id);
}