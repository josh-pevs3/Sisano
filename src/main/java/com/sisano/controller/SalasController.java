package com.sisano.controller;

import com.sisano.model.Sala;
import com.sisano.repository.ISalaRepository;
import com.sisano.repository.impl.SalaRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class SalasController {

    @FXML private ListView<Sala> listaSalas;
    @FXML private TextField txtNombre;
    @FXML private CheckBox chkActiva;

    private ISalaRepository repo = new SalaRepositoryImpl();
    
    // Variable para saber si estamos editando (no null) o creando (null)
    private Integer idEdicion = null;

    @FXML
    public void initialize() {
        cargarSalas();

        // --- LISTENER: DETECTAR CLIC EN LA LISTA ---
        listaSalas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarFormulario(newVal); // Pasamos los datos a los campos
            }
        });
    }

    private void cargarSalas() {
        listaSalas.setItems(FXCollections.observableArrayList(repo.listarTodas()));
    }

    // Poner los datos de la sala seleccionada en los inputs
    private void cargarFormulario(Sala s) {
        idEdicion = s.getId(); // Guardamos el ID que estamos editando
        txtNombre.setText(s.getNombre());
        chkActiva.setSelected(s.isActiva());
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText();
        
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarAlerta("Error", "El nombre de la sala es obligatorio.");
            return;
        }

        Sala sala = new Sala();
        sala.setNombre(nombre);
        sala.setActiva(chkActiva.isSelected());

        boolean exito;

        if (idEdicion == null) {
            // --- CASO 1: CREAR NUEVA ---
            exito = repo.guardar(sala);
        } else {
            // --- CASO 2: ACTUALIZAR EXISTENTE ---
            sala.setId(idEdicion); // Es importante asignarle el ID
            exito = repo.actualizar(sala);
        }

        if (exito) {
            mostrarAlerta("Éxito", "Sala guardada correctamente.");
            limpiar();      // Borrar campos
            cargarSalas();  // Refrescar lista
        } else {
            mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
        }
    }

    @FXML
    private void eliminar() {
        Sala seleccionada = listaSalas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            // Confirmación de seguridad
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar eliminación");
            confirm.setHeaderText("¿Estás seguro de eliminar: " + seleccionada.getNombre() + "?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (repo.eliminar(seleccionada.getId())) {
                    mostrarAlerta("Eliminado", "Sala eliminada.");
                    limpiar();
                    cargarSalas();
                }
            }
        } else {
            mostrarAlerta("Aviso", "Selecciona una sala de la lista para eliminar.");
        }
    }

    // Botón o función para cancelar edición y limpiar
    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true); // Por defecto habilitada
        idEdicion = null; // Volvemos a modo CREAR
        listaSalas.getSelectionModel().clearSelection(); // Soltamos la selección visual
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}