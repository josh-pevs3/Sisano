package com.sisano.repository;

import com.sisano.model.Sala;
import java.util.List;

public interface ISalaRepository {
    List<Sala> listarTodas();
    boolean guardar(Sala s);
    boolean actualizar(Sala s);
    boolean eliminar(int id);
}