package com.sisano.model;

public class Medico {
    private int id;
    private String codigo;
    private String nombre;
    private String especialidad;
    private String email;
    private String telefono;
    private String sexo;   

    public Medico() {}

    public Medico(int id, String codigo, String nombre, String especialidad, String email, String telefono, String sexo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.telefono = telefono;
        this.sexo = sexo;
    }

    // --- LOGICA DE NEGOCIO PARA VISTA ---
    // Este método devuelve el nombre con el prefijo correcto
    public String getNombreFormal() {
        String prefijo = "Dr.";
        if ("F".equals(sexo)) {
            prefijo = "Dra.";
        }
        return prefijo + " " + nombre;
    }

    // Getters y Setters normales
    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getCodigo() { 
        return codigo; 
    }
    public void setCodigo(String codigo) { 
        this.codigo = codigo; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; }
    
    public String getEspecialidad() { 
        return especialidad; 
    }
    public void setEspecialidad(String especialidad) { 
        this.especialidad = especialidad; 
    }
    
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

    public String getSexo() { 
        return sexo; 
    }
    public void setSexo(String sexo) { 
        this.sexo = sexo; 
    }

    @Override 
    public String toString() { 
        return getNombreFormal(); 
    }
}