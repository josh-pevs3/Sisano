package com.sisano.controller;

import com.sisano.model.Paciente;
import com.sisano.repository.IPacienteRepository;
import com.sisano.repository.impl.PacienteRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class PacientesController {

    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TextField txtDni, txtNombre, txtEmail, txtTelefono;
    @FXML private DatePicker dpFechaNacimiento; // <--- Importante
    @FXML private ComboBox<String> comboGenero; // <--- Importante

    private IPacienteRepository repo = new PacienteRepositoryImpl();
    private Integer idEdicion = null; // Para controlar Crear vs Editar

    @FXML
    public void initialize() {
        configurarTabla();
        cargarDatos();
        
        // Inicializar ComboBox
        comboGenero.setItems(FXCollections.observableArrayList("Masculino", "Femenino"));
        
        // Listener para editar al hacer clic
        tablaPacientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) llenarFormulario(newVal);
        });
    }

    private void configurarTabla() {
        // Asegúrate que las columnas en el FXML coincidan con los nombres del Modelo
        // ejemplo: property="dni", property="nombre", etc.
    }

    private void cargarDatos() {
        tablaPacientes.setItems(FXCollections.observableArrayList(repo.listarTodos()));
    }

    private void llenarFormulario(Paciente p) {
        idEdicion = p.getId();
        txtDni.setText(p.getDni());
        txtNombre.setText(p.getNombre());
        txtEmail.setText(p.getEmail());
        txtTelefono.setText(p.getTelefono());
        
        // Llenar Fecha y Género
        dpFechaNacimiento.setValue(p.getFechaNacimiento());
        
        if ("M".equals(p.getGenero())) comboGenero.setValue("Masculino");
        else if ("F".equals(p.getGenero())) comboGenero.setValue("Femenino");
    }
    @FXML
    private void buscarPorDni() {
        String dni = txtDni.getText().trim();
        
        if (dni.isEmpty()) {
            mostrarAlerta("Aviso", "Por favor ingresa un DNI para buscar.");
            return;
        }

        // Llamamos al repositorio
        Paciente p = repo.buscarPorDni(dni);

        if (p != null) {
            // ¡ENCONTRADO! Cargamos sus datos para editar
            llenarFormulario(p);
            mostrarAlerta("Éxito", "Paciente encontrado. Datos cargados en el formulario.");
            
            // Opcional: Seleccionar en la tabla visualmente
            tablaPacientes.getSelectionModel().select(p); 
            // (Nota: para que el select funcione bien, debes implementar equals/hashCode en Paciente, 
            // pero por ahora llenar el formulario es lo importante)
            
        } else {
            // ¡NO EXISTE! Preparamos para crear uno nuevo
            mostrarAlerta("Información", "No existe paciente con ese DNI. Puedes registrarlo ahora.");
            
            // Limpiamos todo MENOS el DNI que acabamos de escribir
            txtNombre.clear();
            txtEmail.clear();
            txtTelefono.clear();
            dpFechaNacimiento.setValue(null);
            comboGenero.setValue(null);
            
            idEdicion = null; // Nos aseguramos que sea un INSERT
        }
    }
    @FXML
    private void guardar() {
        // Validaciones básicas
        if (txtDni.getText().isEmpty() || txtNombre.getText().isEmpty() || dpFechaNacimiento.getValue() == null) {
            mostrarAlerta("Error", "DNI, Nombre y Fecha de Nacimiento son obligatorios.");
            return;
        }

        Paciente p = new Paciente();
        p.setDni(txtDni.getText());
        p.setNombre(txtNombre.getText());
        p.setEmail(txtEmail.getText());
        p.setTelefono(txtTelefono.getText());
        p.setFechaNacimiento(dpFechaNacimiento.getValue());
        
        // Convertir combo a "M"/"F"
        String generoSel = comboGenero.getValue();
        if (generoSel != null) {
            p.setGenero(generoSel.equals("Masculino") ? "M" : "F");
        } else {
            p.setGenero("M"); // Default
        }

        boolean exito;
        if (idEdicion == null) {
            exito = repo.guardar(p);
        } else {
            p.setId(idEdicion);
            exito = repo.actualizar(p);
        }

        if (exito) {
            mostrarAlerta("Éxito", "Paciente guardado correctamente.");
            limpiar();
            cargarDatos();
        } else {
            mostrarAlerta("Error", "No se pudo guardar.");
        }
    }

    @FXML
    private void eliminar() {
        Paciente seleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Seleccione un paciente.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar a " + seleccionado.getNombre() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (repo.eliminar(seleccionado.getId())) {
                mostrarAlerta("Éxito", "Paciente eliminado.");
                limpiar();
                cargarDatos();
            }
        }
    }

    private void limpiar() {
        idEdicion = null;
        txtDni.clear();
        txtNombre.clear();
        txtEmail.clear();
        txtTelefono.clear();
        dpFechaNacimiento.setValue(null);
        comboGenero.setValue(null);
        tablaPacientes.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String titulo, String msj) {
        new Alert(Alert.AlertType.INFORMATION, msj).show();
    }
}