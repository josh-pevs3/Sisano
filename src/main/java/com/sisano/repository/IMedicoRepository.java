package com.sisano.repository;

import com.sisano.model.Medico;
import java.util.List;

public interface IMedicoRepository {
    List<Medico> listarTodos();
    boolean guardar(Medico m);
    boolean actualizar(Medico m);
    boolean eliminar(int id);
}