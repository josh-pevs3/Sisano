package com.sisano.controller;

import com.sisano.repository.impl.DashboardRepositoryImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.Map;

public class InicioController {

    @FXML private Label lblPacientes;
    @FXML private Label lblMedicos;
    @FXML private Label lblSalas;
    @FXML private Label lblAsignaciones;
    @FXML private Label lblCitas;
    @FXML private Label lblCitasHoy;

    private DashboardRepositoryImpl repo = new DashboardRepositoryImpl();

    @FXML
    public void initialize() {
        cargarIndicadores();
    }

    private void cargarIndicadores() {
        // Ejecutar en hilo de fondo si la BD es lenta, pero por ahora directo:
        Map<String, Integer> stats = repo.obtenerEstadisticas();

        lblPacientes.setText(String.valueOf(stats.getOrDefault("pacientes", 0)));
        lblMedicos.setText(String.valueOf(stats.getOrDefault("medicos", 0)));
        lblSalas.setText(String.valueOf(stats.getOrDefault("salas", 0)));
        lblAsignaciones.setText(String.valueOf(stats.getOrDefault("asignaciones", 0)));
        lblCitas.setText(String.valueOf(stats.getOrDefault("citas", 0)));
        lblCitasHoy.setText(String.valueOf(stats.getOrDefault("citasHoy", 0)));
    }
}