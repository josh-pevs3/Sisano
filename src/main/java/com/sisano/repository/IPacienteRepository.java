package com.sisano.repository;
import com.sisano.model.Paciente;
import java.util.List;

public interface IPacienteRepository {
    List<Paciente> listarTodos();
    boolean guardar(Paciente p);
    boolean eliminar(int id);
    boolean actualizar(Paciente p);
    Paciente buscarPorDni(String dni);
}