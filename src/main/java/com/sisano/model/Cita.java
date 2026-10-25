package com.sisano.model;

public class Cita {
    private int id;
    private int idPaciente;
    private int idAsignacion; // id_disponibilidad
    private String observaciones;
    
    // CAMPO AUXILIAR (No existe en la tabla Cita, pero lo llenamos con JOIN)
    private String nombrePacienteAux; 

    public Cita() {}

    public Cita(int id, int idPaciente, int idAsignacion, String observaciones) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.idAsignacion = idAsignacion;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Getters y Setters para el campo auxiliar
    public String getNombrePacienteAux() { return nombrePacienteAux; }
    public void setNombrePacienteAux(String nombrePacienteAux) { this.nombrePacienteAux = nombrePacienteAux; }
}