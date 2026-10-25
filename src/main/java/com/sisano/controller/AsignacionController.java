package com.sisano.controller;

import com.sisano.config.RepositoryFactory;
import com.sisano.model.Disponibilidad;
import com.sisano.model.Medico;
import com.sisano.model.Sala;
import com.sisano.repository.IDisponibilidadRepository;
import com.sisano.repository.IMedicoRepository;
import com.sisano.repository.ISalaRepository;
import com.sisano.config.RepositoryFactory;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AsignacionController {

    // --- CONTENEDORES DE VISTAS ---
    @FXML private VBox panelSeleccion;      // Vista de Tarjetas
    @FXML private BorderPane panelCalendario; // Vista de Calendario
    @FXML private FlowPane containerTarjetas; // Donde van los botones

    // --- CONTROLES DEL CALENDARIO ---
    @FXML private Label lblPeriodo;
    @FXML private ComboBox<Item> comboSalas;
    @FXML private ComboBox<Item> comboMedicos;
    @FXML private DatePicker datePicker;
    @FXML private GridPane gridCalendario;

    private IDisponibilidadRepository disponibRepo = RepositoryFactory.getDisponibilidadRepo();
    private ISalaRepository salaRepo = RepositoryFactory.getSalaRepo();
    private IMedicoRepository medicoRepo = RepositoryFactory.getMedicoRepo();
    private List<Disponibilidad> cambiosPendientes = new ArrayList<>();
    private LocalDate lunesActual; 

    @FXML
    public void initialize() {
        // Cargar datos
        cargarCombos();
        
        // Cargar Tarjetas de Inicio
        generarTarjetasDeSalas();

        // Configuración inicial de fecha
        lunesActual = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        datePicker.setValue(lunesActual);
        
        // Listeners existentes
        comboSalas.setOnAction(e -> renderizarCalendario());
        datePicker.setOnAction(e -> {
            if(datePicker.getValue() != null) {
                lunesActual = datePicker.getValue().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                renderizarCalendario();
            }
        });
        
        // ESTADO INICIAL: Mostrar tarjetas, ocultar calendario
        panelSeleccion.setVisible(true);
        panelCalendario.setVisible(false);
    }

    // --- LÓGICA DE TARJETAS (NUEVO) ---
    private void generarTarjetasDeSalas() {
        containerTarjetas.getChildren().clear();
        
        // Usamos los items que ya cargamos en el combo para no consultar BD dos veces
        for (Item salaItem : comboSalas.getItems()) {
            Button card = new Button(salaItem.nombre);
            card.getStyleClass().add("card-sala"); // Estilo CSS
            
            // ACCIÓN AL HACER CLIC EN TARJETA
            card.setOnAction(e -> {
                // 1. Seleccionar la sala en el combo (esto disparará renderizarCalendario)
                comboSalas.setValue(salaItem);
                
                // 2. Cambiar de vista
                panelSeleccion.setVisible(false);
                panelCalendario.setVisible(true);
            });
            
            containerTarjetas.getChildren().add(card);
        }
    }

    @FXML
    private void volverASeleccion() {
        // Limpiar selección para evitar errores visuales
        cambiosPendientes.clear();
        panelCalendario.setVisible(false);
        panelSeleccion.setVisible(true);
    }

    // --- TU LÓGICA EXISTENTE (Sin cambios mayores) ---

    @FXML private void semanaAnterior() {
        lunesActual = lunesActual.minusWeeks(1);
        datePicker.setValue(lunesActual);
        renderizarCalendario();
    }

    @FXML private void semanaSiguiente() {
        lunesActual = lunesActual.plusWeeks(1);
        datePicker.setValue(lunesActual);
        renderizarCalendario();
    }



    private void cargarCombos() {
        comboSalas.getItems().clear();
        comboMedicos.getItems().clear();

        // Salas desde repo
        for (Sala s : salaRepo.listarTodas()) {
            if (s.isActiva()) { // si quieres filtrar activas aquí
                comboSalas.getItems().add(new Item(s.getId(), s.getNombre()));
            }
        }

        // Médicos desde repo
        for (Medico m : medicoRepo.listarTodos()) {
            comboMedicos.getItems().add(new Item(m.getId(), m.getNombre()));
        }
    }


    private void renderizarCalendario() {
        // Si no estamos en la vista de calendario o no hay sala, no hacemos nada
        if (comboSalas.getValue() == null) return;

        gridCalendario.getChildren().clear();
        gridCalendario.getRowConstraints().clear();
        cambiosPendientes.clear();
        
        int idSala = comboSalas.getValue().id;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        lblPeriodo.setText("Semana del " + lunesActual.format(fmt) + " al " + lunesActual.plusDays(6).format(fmt));
        List<Disponibilidad> existentes = disponibRepo.buscarPorSalaYRango(idSala, lunesActual, lunesActual.plusDays(6));

        // 1. Cabecera
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(50); 
        headerRow.setVgrow(Priority.NEVER);
        gridCalendario.getRowConstraints().add(headerRow);

        String[] dias = {"Hora", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < dias.length; i++) {
            Label lbl = new Label(dias[i]);
            if (i > 0) lbl.setText(dias[i] + "\n" + lunesActual.plusDays(i-1).format(DateTimeFormatter.ofPattern("dd/MM")));
            lbl.getStyleClass().add("calendario-header");
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridCalendario.add(lbl, i, 0); 
        }

        // 2. Cuerpo
        LocalTime hora = LocalTime.of(8, 0);
        int row = 1;
        while (hora.isBefore(LocalTime.of(20, 0))) {
            RowConstraints r = new RowConstraints();
            r.setMinHeight(60); 
            r.setVgrow(Priority.ALWAYS);
            gridCalendario.getRowConstraints().add(r);

            Label lblHora = new Label(hora.toString());
            lblHora.getStyleClass().add("calendario-header");
            lblHora.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridCalendario.add(lblHora, 0, row);

            for (int col = 0; col < 7; col++) {
                LocalDate fechaCelda = lunesActual.plusDays(col);
                Pane slot = crearSlot(idSala, fechaCelda, hora, existentes);
                gridCalendario.add(slot, col + 1, row);
            }
            hora = hora.plusMinutes(30);
            row++;
        }
    }

    private Pane crearSlot(int idSala, LocalDate fecha, LocalTime hora, List<Disponibilidad> existentes) {
        StackPane pane = new StackPane();
        pane.setPrefHeight(50);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.getStyleClass().add("calendario-celda"); 

        Disponibilidad actual = existentes.stream()
            .filter(d -> d.getFecha().equals(fecha) && d.getHoraInicio().equals(hora))
            .findFirst().orElse(null);

        if (actual != null) {
            pane.getStyleClass().add("slot-ocupado");
            String nombreMedico = obtenerNombreMedico(actual.getIdMedico());
            Label lbl = new Label(nombreMedico);
            lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #154360;");
            lbl.setWrapText(true);
            pane.getChildren().add(lbl);

            ContextMenu contextMenu = new ContextMenu();
            MenuItem itemEliminar = new MenuItem("Eliminar Asignación");
            itemEliminar.setOnAction(e -> confirmarEliminacion(actual));
            contextMenu.getItems().add(itemEliminar);

            pane.setOnContextMenuRequested(e -> contextMenu.show(pane, e.getScreenX(), e.getScreenY()));
        } else {
            // El slot está vacío (Blanco)
            pane.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    
                    // Verificamos qué hay en el Combo Superior
                    Item medicoSeleccionado = comboMedicos.getValue();
                    
                    // CONDICIÓN: Si hay un médico seleccionado Y NO ES la opción "Ver Todos" (ID 0)
                    if (medicoSeleccionado != null && medicoSeleccionado.id != 0) {
                        
                        // MODO RÁPIDO: Asignar directo
                        procesarAsignacion(pane, idSala, fecha, hora, medicoSeleccionado);
                        
                    } else {
                        
                        // MODO SELECCIÓN: No hay médico fijo, abrir ventana
                        abrirDialogoSeleccionMedico(pane, idSala, fecha, hora);
                    }
                }
            });
        }
        return pane;        
    }

    private String obtenerNombreMedico(int idMedico) {
        return comboMedicos.getItems().stream()
                .filter(item -> item.id == idMedico)
                .map(item -> item.nombre)
                .findFirst()
                .orElse("Médico ID: " + idMedico);
    }

    private void abrirDialogoSeleccionMedico(StackPane pane, int idSala, LocalDate fecha, LocalTime hora) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Asignar Médico");
        dialog.setHeaderText("Turno: " + fecha + " " + hora);

        ComboBox<Item> comboDialog = new ComboBox<>();
        // Llenamos el combo, excluyendo la opción "Ver Todos" (ID 0)
        for(Item i : comboMedicos.getItems()) {
            if(i.id != 0) comboDialog.getItems().add(i);
        }
        
        VBox content = new VBox(10, new Label("Seleccione el médico:"), comboDialog);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> (btn == ButtonType.OK) ? comboDialog.getValue() : null);

        Optional<Item> result = dialog.showAndWait();
        
        // Si seleccionó a alguien, llamamos al método común
        result.ifPresent(medico -> procesarAsignacion(pane, idSala, fecha, hora, medico));
    }
    private void confirmarEliminacion(Disponibilidad d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Quitar turno al médico?");
        alert.setContentText("Esta acción borrará la asignación de la base de datos.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (disponibRepo.eliminar(d.getId())) { 
                renderizarCalendario(); 
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("No se pudo eliminar");
                errorAlert.setHeaderText("Operación denegada");
                errorAlert.setContentText("No se puede eliminar porque hay CITAS asociadas.");
                errorAlert.show();
            }
        }
    }
    // Este método se encarga de la lógica final (Visual + Lista)
    private void procesarAsignacion(StackPane pane, int idSala, LocalDate fecha, LocalTime hora, Item medico) {
        // 1. Cambio Visual
        pane.getStyleClass().add("slot-nuevo");
        pane.getChildren().clear();
        pane.getChildren().add(new Label(medico.nombre));

        // 2. Limpieza de duplicados (Lógica crítica que arreglamos antes)
        cambiosPendientes.removeIf(d -> 
            d.getIdSala() == idSala && 
            d.getFecha().equals(fecha) && 
            d.getHoraInicio().equals(hora)
        );

        // 3. Agregar a la lista de guardar
        Disponibilidad nueva = new Disponibilidad(medico.id, idSala, fecha, hora, hora.plusMinutes(30));
        cambiosPendientes.add(nueva);
    }

    @FXML
    private void guardarCambios() {
        if (cambiosPendientes.isEmpty()) return;
        int count = 0;
        for (Disponibilidad d : cambiosPendientes) {
            if (disponibRepo.guardar(d)) count++;
        }
        new Alert(Alert.AlertType.INFORMATION, "Guardados " + count + " turnos.").showAndWait();
        renderizarCalendario();
    }

    static class Item {
        int id; String nombre;
        public Item(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}