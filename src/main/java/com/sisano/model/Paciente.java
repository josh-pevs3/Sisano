package com.sisano.model;

import java.time.LocalDate;

public class Paciente {
    private int id;
    private String dni;
    private String nombre;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento; 
    private String genero;             

    public Paciente() {}

    public Paciente(int id, String dni, String nombre, String email, String telefono, LocalDate fechaNacimiento, String genero) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
    }

    // Getters y Setters
    public int getId() { 
        return id; 

    }
    public void setId(int id) { 
        this.id = id; 
    }

    public String getDni() { 
        return dni; 
    }
    public void setDni(String dni) { 
        this.dni = dni; 
    }

    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getTelefono() { 
        return telefono; 
    }
    public void setTelefono(String telefono) { 
        this.telefono = telefono; 
    }

    public LocalDate getFechaNacimiento() { 
        return fechaNacimiento; 
    }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { 
        return genero; 
    }
    public void setGenero(String genero) { 
        this.genero = genero; 
    }
    
    @Override public String toString() { 
        return nombre; 
    }
}