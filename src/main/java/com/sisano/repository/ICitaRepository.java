package com.sisano.repository;

import com.sisano.model.Cita;
import java.util.List;

public interface ICitaRepository {
    // Guardar una nueva cita
    boolean guardar(Cita c);
    
    // Buscar la cita que corresponde a un cuadrito del calendario
    Cita buscarPorDisponibilidad(int idDisponibilidad);
    
    // Mover de horario (Postergar)
    boolean actualizarDisponibilidadCita(int idCita, int idNuevaDisponibilidad);
    
    // Cancelar cita
    boolean eliminar(int idCita);
}