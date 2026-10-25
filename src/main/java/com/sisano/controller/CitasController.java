package com.sisano.controller;

import com.sisano.config.DatabaseConnection;
import com.sisano.model.*;
import com.sisano.repository.*;
import com.sisano.repository.impl.*;
import com.sisano.config.RepositoryFactory;
import com.sisano.service.impl.NotificacionCitaDecorator;
import com.sisano.service.impl.CitaService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

public class CitasController {

    @FXML private Label lblPeriodo, lblEstadoAccion;
    @FXML private ComboBox<Item> comboSalas, comboMedicos;
    @FXML private GridPane gridCalendario;

    private IDisponibilidadRepository disponibRepo = RepositoryFactory.getDisponibilidadRepo();
    private ICitaRepository citaRepo = RepositoryFactory.getCitaRepo();
    private IPacienteRepository pacienteRepo = RepositoryFactory.getPacienteRepo(); 

    
    private NotificacionCitaDecorator citaService = new NotificacionCitaDecorator(new CitaService());

    private LocalDate lunesActual;
    private Cita citaEnPortapapeles = null;
    
    // Variable temporal para guardar al paciente encontrado en el diálogo
    private Paciente pacienteSeleccionadoParaCita = null;

    @FXML
    public void initialize() {
        cargarCombos();
        lunesActual = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        comboSalas.setOnAction(e -> renderizarCalendario());
        comboMedicos.setOnAction(e -> renderizarCalendario());
        
        if(!comboSalas.getItems().isEmpty()) {
            comboSalas.getSelectionModel().selectFirst();
            renderizarCalendario();
        }
    }

    @FXML private void semanaAnterior() {
        lunesActual = lunesActual.minusWeeks(1);
        renderizarCalendario();
    }

    @FXML private void semanaSiguiente() {
        lunesActual = lunesActual.plusWeeks(1);
        renderizarCalendario();
    }

    @FXML public void renderizarCalendario() {
        gridCalendario.getChildren().clear();
        gridCalendario.getRowConstraints().clear();
        
        if (comboSalas.getValue() == null) return;
        int idSala = comboSalas.getValue().id;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        lblPeriodo.setText("Semana del " + lunesActual.format(fmt) + " al " + lunesActual.plusDays(6).format(fmt));

        List<Disponibilidad> horarios = disponibRepo.buscarPorSalaYRango(idSala, lunesActual, lunesActual.plusDays(6));

        dibujarCabeceras();

        LocalTime hora = LocalTime.of(8, 0);
        int row = 1;
        while (hora.isBefore(LocalTime.of(20, 0))) {
            RowConstraints r = new RowConstraints();
            r.setMinHeight(60); r.setVgrow(Priority.ALWAYS);
            gridCalendario.getRowConstraints().add(r);

            Label lblHora = new Label(hora.toString());
            lblHora.getStyleClass().add("calendario-header");
            lblHora.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridCalendario.add(lblHora, 0, row);

            for (int col = 0; col < 7; col++) {
                LocalDate fechaCelda = lunesActual.plusDays(col);
                Pane slot = crearSlot(fechaCelda, hora, horarios);
                gridCalendario.add(slot, col + 1, row);
            }
            hora = hora.plusMinutes(30);
            row++;
        }
    }

    private void dibujarCabeceras() {
        String[] dias = {"Hora", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < dias.length; i++) {
            Label lbl = new Label(dias[i]);
            if (i > 0) lbl.setText(dias[i] + "\n" + lunesActual.plusDays(i-1).format(DateTimeFormatter.ofPattern("dd/MM")));
            lbl.getStyleClass().add("calendario-header");
            lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            gridCalendario.add(lbl, i, 0);
        }
    }

    private Pane crearSlot(LocalDate fecha, LocalTime hora, List<Disponibilidad> horarios) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("calendario-celda");

        Disponibilidad d = horarios.stream()
                .filter(x -> x.getFecha().equals(fecha) && x.getHoraInicio().equals(hora))
                .findFirst().orElse(null);

        if (d != null && comboMedicos.getValue() != null) {
            if (d.getIdMedico() != comboMedicos.getValue().id) d = null;
        }

        if (d == null) {
            pane.getStyleClass().add("cita-vacia");
        } else {
            if ("OCUPADO".equals(d.getEstado()) || "RESERVADO".equals(d.getEstado())) {
                configurarSlotOcupado(pane, d);
            } else {
                configurarSlotDisponible(pane, d);
            }
        }
        return pane;
    }

    private void configurarSlotDisponible(StackPane pane, Disponibilidad d) {
        pane.getStyleClass().add("cita-disponible");
        String nombreDoc = obtenerNombreMedico(d.getIdMedico());
        
        Label lbl = new Label("DISPONIBLE\n" + nombreDoc);
        lbl.setStyle("-fx-text-alignment: center; -fx-font-size: 10px;");
        pane.getChildren().add(lbl);

        pane.setOnMouseClicked(e -> {
            if (citaEnPortapapeles != null) confirmarPostergacion(d);
            else abrirDialogoReserva(d);
        });
    }

    private void configurarSlotOcupado(StackPane pane, Disponibilidad d) {
        pane.getStyleClass().add("cita-ocupada");
        Cita cita = citaRepo.buscarPorDisponibilidad(d.getId());
        if (cita != null) {
            Label lbl = new Label(cita.getNombrePacienteAux() + "\n(" + d.getHoraInicio() + ")");
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #922b21; -fx-text-alignment: center;");
            pane.getChildren().add(lbl);

            ContextMenu menu = new ContextMenu();
            MenuItem itemPostergar = new MenuItem("📅 Postergar (Mover)");
            itemPostergar.setOnAction(e -> iniciarPostergacion(cita, pane));
            MenuItem itemEliminar = new MenuItem("Cancelar Cita");
            itemEliminar.setOnAction(e -> cancelarCita(cita, d));
            
            menu.getItems().addAll(itemPostergar, itemEliminar);
            pane.setOnContextMenuRequested(e -> menu.show(pane, e.getScreenX(), e.getScreenY()));
        }
    }

    // =========================================================================
    // LÓGICA DE RESERVA CON BÚSQUEDA Y REGISTRO
    // =========================================================================
    private void abrirDialogoReserva(Disponibilidad d) {
        pacienteSeleccionadoParaCita = null; // Resetear

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agendar Cita");
        dialog.setHeaderText("Cita: " + d.getFecha() + " a las " + d.getHoraInicio() + "\nMédico: " + obtenerNombreMedico(d.getIdMedico()));

        // --- UI ---
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        // Campo DNI y Botón Buscar
        TextField txtDniBuscar = new TextField(); 
        txtDniBuscar.setPromptText("Ingrese DNI");
        Button btnBuscar = new Button("Buscar");
        
        // Label para mostrar el nombre encontrado
        Label lblNombrePaciente = new Label("---");
        lblNombrePaciente.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        // Botón Registrar (Oculto al inicio)
        Button btnRegistrar = new Button("Registrar Nuevo Paciente");
        btnRegistrar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        btnRegistrar.setVisible(false);

        // Notas
        TextArea txtObs = new TextArea(); 
        txtObs.setPromptText("Motivo de consulta...");
        txtObs.setPrefHeight(60);

        // Armar Grid
        grid.add(new Label("Buscar DNI:"), 0, 0); 
        grid.add(txtDniBuscar, 1, 0); 
        grid.add(btnBuscar, 2, 0);
        
        grid.add(new Label("Paciente:"), 0, 1);
        grid.add(lblNombrePaciente, 1, 1, 2, 1);
        
        grid.add(btnRegistrar, 1, 2, 2, 1);

        grid.add(new Label("Notas:"), 0, 3);
        grid.add(txtObs, 1, 3, 2, 1);

        // --- LÓGICA DE BÚSQUEDA ---
        btnBuscar.setOnAction(e -> {
            String dni = txtDniBuscar.getText().trim();
            if (dni.isEmpty()) return;

            Paciente p = pacienteRepo.buscarPorDni(dni);
            if (p != null) {
                // ENCONTRADO
                pacienteSeleccionadoParaCita = p;
                lblNombrePaciente.setText("✅ " + p.getNombre());
                btnRegistrar.setVisible(false);
            } else {
                // NO ENCONTRADO
                pacienteSeleccionadoParaCita = null;
                lblNombrePaciente.setText("No encontrado");
                btnRegistrar.setVisible(true); // Mostrar botón de registro
            }
        });

        // --- LÓGICA DE REGISTRO---
        btnRegistrar.setOnAction(e -> {
            // Abrimos modal para registrar usando el DNI que ya escribió
            Paciente nuevo = abrirModalRegistroExpress(txtDniBuscar.getText());
            if (nuevo != null) {
                pacienteSeleccionadoParaCita = nuevo;
                lblNombrePaciente.setText("+" + nuevo.getNombre() + " (NUEVO)");
                btnRegistrar.setVisible(false);
            }
        });

        dialog.getDialogPane().setContent(grid);
        
        // Botón OK personalizado
        ButtonType btnAgendarType = new ButtonType("Agendar Cita", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgendarType, ButtonType.CANCEL);

        // Validar antes de cerrar
        final Button btnAgendar = (Button) dialog.getDialogPane().lookupButton(btnAgendarType);
        btnAgendar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (pacienteSeleccionadoParaCita == null) {
                mostrarAlerta("Error", "Debes buscar y seleccionar un paciente (o registrar uno nuevo).");
                event.consume(); // Evita que se cierre el diálogo
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == btnAgendarType) {
            // Guardar Cita
            Cita nueva = new Cita();
            nueva.setIdAsignacion(d.getId());
            nueva.setIdPaciente(pacienteSeleccionadoParaCita.getId());
            nueva.setObservaciones(txtObs.getText());
            
            if(citaService.agendarCita(nueva)) {
                d.setEstado("OCUPADO");
                disponibRepo.guardar(d);
                renderizarCalendario();
                mostrarAlerta("Éxito", "Cita agendada para: " + pacienteSeleccionadoParaCita.getNombre());
            }
        }
    }

    // --- VENTANA EMERGENTE PARA REGISTRO RÁPIDO ---
    private Paciente abrirModalRegistroExpress(String dniSugerido) {
        Dialog<Paciente> dialog = new Dialog<>();
        dialog.setTitle("Registro Rápido de Paciente");
        dialog.setHeaderText("Registrar nuevo paciente para agendar cita inmediata");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtDni = new TextField(dniSugerido);
        TextField txtNombre = new TextField(); txtNombre.setPromptText("Nombre Completo");
        TextField txtTelefono = new TextField(); txtTelefono.setPromptText("Celular / WhatsApp");
        TextField txtEmail = new TextField(); txtEmail.setPromptText("Correo (Opcional)");
        DatePicker dpNac = new DatePicker(); dpNac.setPromptText("F. Nacimiento");
        ComboBox<String> cbSexo = new ComboBox<>(); cbSexo.getItems().addAll("Masculino", "Femenino");

        grid.add(new Label("DNI:"), 0, 0); grid.add(txtDni, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1); grid.add(txtNombre, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2); grid.add(txtTelefono, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(new Label("Nacimiento:"), 0, 4); grid.add(dpNac, 1, 4);
        grid.add(new Label("Sexo:"), 0, 5); grid.add(cbSexo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);


        // Obtenemos el botón OK del diálogo
        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        
        // Agregamos un "Filtro" antes de que se ejecute la acción de cerrar
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validamos los campos
            if(txtDni.getText().isEmpty() || txtNombre.getText().isEmpty() || dpNac.getValue() == null) {
                mostrarAlerta("Error", "Faltan datos obligatorios (DNI, Nombre, Nacimiento).");
                event.consume(); // <--- Evita que el diálogo se cierre
            }
        });

        // Convertidor de resultado (Solo se ejecuta si la validación anterior pasó)
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Paciente p = new Paciente();
                p.setDni(txtDni.getText());
                p.setNombre(txtNombre.getText());
                p.setTelefono(txtTelefono.getText());
                p.setEmail(txtEmail.getText());
                p.setFechaNacimiento(dpNac.getValue());
                p.setGenero(cbSexo.getValue() != null && cbSexo.getValue().equals("Masculino") ? "M" : "F");
                
                // Guardar en BD inmediatamente
                if(pacienteRepo.guardar(p)) {
                    return pacienteRepo.buscarPorDni(p.getDni());
                }
            }
            return null;
        });

        Optional<Paciente> result = dialog.showAndWait();
        
        // Ya no necesitamos lógica compleja aquí, si result tiene valor es porque todo salió bien
        return result.orElse(null);
    }
    private void iniciarPostergacion(Cita c, StackPane paneVisual) {
        this.citaEnPortapapeles = c;
        lblEstadoAccion.setText("MODO MOVER: Selecciona un horario verde para " + c.getNombrePacienteAux());
        lblEstadoAccion.setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold;");
        paneVisual.getStyleClass().add("modo-mover");
    }

    private void confirmarPostergacion(Disponibilidad nuevaDisp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "¿Mover cita de " + citaEnPortapapeles.getNombrePacienteAux() + " a " + nuevaDisp.getFecha() + " " + nuevaDisp.getHoraInicio() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            
            // 1. Liberar el horario ANTERIOR 
            // Usamos el ID que tiene la cita actualmente
            disponibRepo.actualizarEstado(citaEnPortapapeles.getIdAsignacion(), "LIBRE"); 

            // 2. Ocupar el NUEVO horario (Ponerlo en 'OCUPADO' para que sea rojo)
            disponibRepo.actualizarEstado(nuevaDisp.getId(), "OCUPADO");

            // 3. Mover la CITA 
            citaRepo.actualizarDisponibilidadCita(citaEnPortapapeles.getId(), nuevaDisp.getId());

            // 4. Limpieza
            citaEnPortapapeles = null;
            lblEstadoAccion.setText("Modo: Lectura");
            
            // 5. Refrescar visualmente
            renderizarCalendario();
            mostrarAlerta("Éxito", "Cita reprogramada correctamente.");
            
        } else {
            // Cancelar la operación
            citaEnPortapapeles = null;
            lblEstadoAccion.setText("Modo: Lectura");
            renderizarCalendario(); // Para quitar el borde amarillo
        }
    }

    private void cancelarCita(Cita c, Disponibilidad d) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Cita");
        confirm.setHeaderText("¿Estás seguro de cancelar la cita?");
        confirm.setContentText("Paciente: " + c.getNombrePacienteAux() + "\nEl horario quedará libre para otros pacientes.");

        if(confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // 1. Borrar la cita de la tabla Cita
            citaRepo.eliminar(c.getId()); 
            
            // 2. ACTUALIZAR EL ESTADO DEL HORARIO A 'LIBRE'
            // Usamos actualizarEstado en lugar de guardar 
            disponibRepo.actualizarEstado(d.getId(), "LIBRE");
            
            // 3. Refrescar
            renderizarCalendario();
            mostrarAlerta("Éxito", "Cita eliminada. El horario ahora está disponible (Verde).");
        }
    }

    private String obtenerNombreMedico(int idMedico) {
        return comboMedicos.getItems().stream()
                .filter(i -> i.id == idMedico)
                .findFirst()
                .map(i -> i.nombre)
                .orElse("Doc ID:" + idMedico);
    }

    private void cargarCombos() {
        try (Connection con = DatabaseConnection.getInstance().getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT id_sala, nombre FROM Sala WHERE estado=1");
            while (rs.next()) comboSalas.getItems().add(new Item(rs.getInt(1), rs.getString(2)));
            ResultSet rs2 = con.createStatement().executeQuery("SELECT id_medico, nombre FROM Medico");
            while (rs2.next()) comboMedicos.getItems().add(new Item(rs2.getInt(1), rs2.getString(2)));
        } catch (Exception e) {}
    }
    
    private void actualizarEstadoDisponibilidad(int idDisp, String estado) {
        try (Connection con = DatabaseConnection.getInstance().getConnection()) {
            con.createStatement().executeUpdate("UPDATE Disponibilidad SET estado='"+estado+"' WHERE id_disponibilidad="+idDisp);
        } catch(Exception e){}
    }
    
    private void mostrarAlerta(String t, String c) { new Alert(Alert.AlertType.INFORMATION, c).show(); }

    static class Item {
        int id; String nombre;
        public Item(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}