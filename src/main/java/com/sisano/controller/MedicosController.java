package com.sisano.controller;

import com.sisano.model.Medico;
import com.sisano.repository.IMedicoRepository;
import com.sisano.repository.impl.EspecialidadRepositoryImpl;
import com.sisano.repository.impl.MedicoRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class MedicosController {

    @FXML private TableView<Medico> tablaMedicos;
    @FXML private TextField txtCodigo, txtNombre, txtEmail, txtTelefono;
    @FXML private ComboBox<String> comboEspecialidad;
    @FXML private ComboBox<String> comboSexo;

    private IMedicoRepository repo = new MedicoRepositoryImpl();
    private EspecialidadRepositoryImpl especialidadRepo = new EspecialidadRepositoryImpl();
    
    // VARIABLE CLAVE: Para saber si estamos editando (tendrá valor) o creando (será null)
    private Integer idActual = null; 

    @FXML
    public void initialize() {
        configurarCombos();
        cargarTabla();
        
        // --- NUEVO: LISTENER DE SELECCIÓN ---
        // Al hacer clic en una fila, llenamos el formulario
        tablaMedicos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                llenarFormulario(newSelection);
            }
        });
    }

    private void configurarCombos() {
        cargarEspecialidades();
        comboSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino"));
    }

    private void cargarEspecialidades() {
        comboEspecialidad.setItems(FXCollections.observableArrayList(especialidadRepo.listarNombres()));
    }

    @FXML
    private void nuevaEspecialidad() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Especialidad");
        dialog.setHeaderText("Agregar al catálogo");
        dialog.setContentText("Nombre de la especialidad:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (nombre.trim().isEmpty()) return;
            if (especialidadRepo.guardar(nombre.trim())) {
                cargarEspecialidades();
                comboEspecialidad.setValue(nombre.trim());
                mostrarAlerta("Éxito", "Especialidad añadida correctamente.");
            } else {
                mostrarAlerta("Aviso", "Esa especialidad ya existe o hubo un error.");
            }
        });
    }

    private void cargarTabla() {
        tablaMedicos.setItems(FXCollections.observableArrayList(repo.listarTodos()));
    }
    
    // --- NUEVO: MÉTODO PARA LLENAR CAMPOS AL EDITAR ---
    private void llenarFormulario(Medico m) {
        idActual = m.getId(); // Guardamos el ID real de la base de datos
        txtCodigo.setText(m.getCodigo());
        txtNombre.setText(m.getNombreFormal()); // O getNombre() segun tu modelo
        txtEmail.setText(m.getEmail());
        txtTelefono.setText(m.getTelefono());
        comboEspecialidad.setValue(m.getEspecialidad());
        
        // Convertir "M"/"F" a texto completo para el combo
        if ("M".equals(m.getSexo())) comboSexo.setValue("Masculino");
        else if ("F".equals(m.getSexo())) comboSexo.setValue("Femenino");
    }

    @FXML
    private void guardar() {
        // Validaciones
        if (txtNombre.getText().isEmpty() || comboSexo.getValue() == null) {
            mostrarAlerta("Error", "El nombre y el sexo son obligatorios.");
            return;
        }

        String sexoDb = comboSexo.getValue().equals("Masculino") ? "M" : "F";
        String especialidadFinal = comboEspecialidad.getValue();
        if (especialidadFinal == null || especialidadFinal.trim().isEmpty()) {
            especialidadFinal = "General";
        }

        // Creamos el objeto.
        // OJO: Si idActual es null, ponemos 0. Si no, usamos el idActual.
        int idParaGuardar = (idActual == null) ? 0 : idActual;
        
        Medico m = new Medico(
            idParaGuardar, 
            txtCodigo.getText(), // Pasamos el código que hay en pantalla (vacío si es nuevo)
            txtNombre.getText(), 
            especialidadFinal, 
            txtEmail.getText(),
            txtTelefono.getText(),
            sexoDb
        );

        boolean exito;
        
        // --- LÓGICA INSERT VS UPDATE ---
        if (idActual == null) {
            // Es NUEVO -> INSERT
            exito = repo.guardar(m);
        } else {
            // Es EDICIÓN -> UPDATE
            // IMPORTANTE: Asegúrate de tener el método 'actualizar' en tu repo
            exito = repo.actualizar(m); 
        }

        if(exito) {
            mostrarAlerta("Éxito", "Operación realizada correctamente.");
            limpiar(); // Limpia formulario y variable idActual
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
        }
    }

    @FXML
    private void eliminar() {
        Medico seleccionado = tablaMedicos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if(repo.eliminar(seleccionado.getId())) {
                mostrarAlerta("Eliminado", "Médico eliminado.");
                limpiar(); // Limpiar por si acaso estaba seleccionado en el form
                cargarTabla();
            }
        } else {
            mostrarAlerta("Aviso", "Selecciona un médico de la tabla.");
        }
    }

    private void limpiar() {
        idActual = null; // IMPORTANTE: Volver a null para que el próximo sea INSERT
        
        txtCodigo.clear(); 
        txtNombre.clear(); 
        txtEmail.clear();
        txtTelefono.clear();
        comboEspecialidad.setValue(null);
        comboSexo.setValue(null);
        
        // Deseleccionar tabla para evitar confusiones
        tablaMedicos.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String titulo, String msj) {
        new Alert(Alert.AlertType.INFORMATION, msj).show();
    }
}