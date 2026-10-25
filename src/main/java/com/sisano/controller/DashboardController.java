package com.sisano.controller;

import com.sisano.App;
import com.sisano.config.SessionManager;
import com.sisano.model.Usuario;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class DashboardController {

    @FXML private Label lblUsuario;
    @FXML private StackPane contentArea;
    
    // Botones del menú 
    @FXML private Button btnMedicos;
    @FXML private Button btnSalas;
    @FXML private Button btnGestion; 

    @FXML
    public void initialize() {
        // 1. Obtener usuario logueado
        Usuario u = SessionManager.getInstance().getUsuarioActual();
        
        if (u != null) {
            lblUsuario.setText("Usuario: " + u.getUsername() + " (" + u.getRol() + ")");
            aplicarPermisos(u.getRol());
        }

        // 2. Cargar la pantalla de inicio 
        mostrarInicio();
    }

    private void aplicarPermisos(String rol) {
        // Regla: El SECRETARIO no puede gestionar la infraestructura (Salas/Médicos) ni Usuarios (Gestión)
        if ("SECRETARIO".equals(rol)) {
            ocultarBoton(btnMedicos);
            ocultarBoton(btnSalas);
            ocultarBoton(btnGestion);
        }
    }

    // Método auxiliar para desaparecer un botón totalmente (no solo invisible, sino que no ocupe espacio)
    private void ocultarBoton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    // Método genérico para cargar vistas en el centro
    private void cargarVista(String fxml) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sisano/" + fxml + ".fxml"));
            Parent vista = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando la vista: " + fxml);
        }
    }

    // --- MÉTODOS DE NAVEGACIÓN ---

    @FXML 
    private void mostrarInicio() {
        cargarVista("inicio"); // Carga las tarjetas de estadísticas
    }

    @FXML 
    private void mostrarMedicos() {
        cargarVista("medicos");
    }

    @FXML 
    private void mostrarSalas() {
        cargarVista("salas");
    }

    @FXML 
    private void mostrarAsignacion() {
        cargarVista("asignacion"); // El calendario de turnos médicos
    }

    @FXML 
    private void mostrarCitas() {
        cargarVista("citas"); // El calendario de reserva de pacientes
    }
    
    @FXML 
    private void mostrarGestion() {
        cargarVista("gestion"); // El CRUD de usuarios
    }

    @FXML 
    private void mostrarPacientes() {
        cargarVista("pacientes");
    }

    @FXML 
    private void cerrarSesion() throws IOException {
        SessionManager.getInstance().logout(); // Limpia la sesión actual
        App.setRoot("login"); // Vuelve al login
    }
}