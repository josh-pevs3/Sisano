package com.sisano.repository;
import com.sisano.model.Disponibilidad;
import java.time.LocalDate;
import java.util.List;

public interface IDisponibilidadRepository {
    // Busca horarios de una sala en un rango de fechas 
    List<Disponibilidad> buscarPorSalaYRango(int idSala, LocalDate inicio, LocalDate fin);
    boolean guardar(Disponibilidad d);
    boolean eliminar(int idSala, LocalDate fecha, java.time.LocalTime horaInicio);
    boolean eliminar(int id);
    boolean actualizarEstado(int idDisponibilidad, String nuevoEstado);
}