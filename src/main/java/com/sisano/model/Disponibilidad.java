package com.sisano.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Disponibilidad {
    private int id;
    private int idMedico;
    private int idSala;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado; // "LIBRE", "OCUPADO" (Cita agendada)

    public Disponibilidad() {}

    public Disponibilidad(int idMedico, int idSala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.idMedico = idMedico;
        this.idSala = idSala;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = "LIBRE";
    }

    // Getters y Setters manuales
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
    public int getIdSala() { return idSala; }
    public void setIdSala(int idSala) { this.idSala = idSala; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}