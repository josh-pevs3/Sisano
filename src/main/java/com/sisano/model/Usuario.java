package com.sisano.model;

public class Usuario {
    private int id;
    private String username;
    private String passwordHash; // <--- CAMBIO: Coincide con tu BD
    private String rol;
    private String nombreCompleto;

    public Usuario() {}

    public Usuario(int id, String username, String passwordHash, String rol, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombreCompleto = nombreCompleto;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    // OJO AQUÍ: El getter/setter usa la propiedad interna, aunque visualmente sea "Password"
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    
    @Override public String toString() { return username; }
}