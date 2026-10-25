package com.sisano.controller;

import com.sisano.App;
import com.sisano.config.SessionManager;
import com.sisano.model.Usuario;
import com.sisano.repository.IUsuarioRepository;
import com.sisano.repository.impl.UsuarioRepositoryImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;
    @FXML private Label lblError;

    private IUsuarioRepository usuarioRepo = new UsuarioRepositoryImpl();

    @FXML
    private void handleLogin() {
        try {
            // Intentar login
            Usuario u = usuarioRepo.validarLogin(txtUser.getText(), txtPass.getText());
            
            if (u != null) {
                SessionManager.getInstance().login(u);
                System.out.println("Login exitoso: " + u.getRol());
                App.setRoot("dashboard"); 
            } else {
                lblError.setText("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            // Si la base de datos no conecta, mostramos alerta
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Conexión");
            alert.setHeaderText("No se pudo conectar a la Base de Datos");
            alert.setContentText("Probleemas de coneccion.\n\nDetalle: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}