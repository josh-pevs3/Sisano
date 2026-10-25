package com.sisano.controller;

import com.sisano.model.Usuario;
import com.sisano.repository.impl.UsuarioRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class GestionController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TextField txtNombre, txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> comboRol;

    private UsuarioRepositoryImpl repo = new UsuarioRepositoryImpl();
    private Integer idEdicion = null;

    @FXML
    public void initialize() {
        cargarRoles();
        cargarTabla();
        
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) llenarFormulario(newVal);
        });
    }

    private void cargarRoles() {
        comboRol.setItems(FXCollections.observableArrayList("ADMIN", "SECRETARIO"));
    }

    private void cargarTabla() {
        tablaUsuarios.setItems(FXCollections.observableArrayList(repo.listarTodos()));
    }

    private void llenarFormulario(Usuario u) {
        idEdicion = u.getId();
        txtNombre.setText(u.getNombreCompleto());
        txtUsername.setText(u.getUsername());
        txtPassword.setText(u.getPasswordHash()); 
        comboRol.setValue(u.getRol());
    }

    @FXML
    private void guardar() {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() || comboRol.getValue() == null) {
            mostrarAlerta("Error", "Usuario, Contraseña y Rol son obligatorios.");
            return;
        }

        Usuario u = new Usuario();
        u.setNombreCompleto(txtNombre.getText());
        u.setUsername(txtUsername.getText());
        u.setPasswordHash(txtPassword.getText());
        u.setRol(comboRol.getValue());

        boolean exito;
        if (idEdicion == null) {
            exito = repo.guardar(u);
        } else {
            u.setId(idEdicion);
            exito = repo.actualizar(u);
        }

        if (exito) {
            mostrarAlerta("Éxito", "Usuario guardado correctamente.");
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo guardar (Revisa si el usuario ya existe).");
        }
    }

    @FXML
    private void eliminar() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        
        if (repo.eliminar(seleccionado.getId())) {
            mostrarAlerta("Éxito", "Eliminado.");
            limpiar();
            cargarTabla();
        }
    }
    
    @FXML
    private void limpiar() {
        idEdicion = null;
        txtNombre.clear();
        txtUsername.clear();
        txtPassword.clear();
        comboRol.setValue(null);
        tablaUsuarios.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String titulo, String contenido) {
        new Alert(Alert.AlertType.INFORMATION, contenido).show();
    }
}