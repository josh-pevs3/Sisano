package com.sisano.model;

public class Sala {
    private int id;
    private String nombre;
    private boolean activa; 

    public Sala() {}

    public Sala(int id, String nombre, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.activa = activa;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    // IMPORTANTE: Esto define qué se ve en la lista
    @Override
    public String toString() {
        String estado = activa ? "(Habilitada)" : "(Mantenimiento)";
        return nombre + " " + estado;
    }
}