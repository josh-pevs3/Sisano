package com.sisano.service.impl;

import com.sisano.model.Cita;
import com.sisano.service.ICitaService;
import javafx.scene.control.Alert;

public class NotificacionCitaDecorator implements ICitaService {
    
    private ICitaService servicioDecorado; // Referencia al servicio original

    public NotificacionCitaDecorator(ICitaService servicioDecorado) {
        this.servicioDecorado = servicioDecorado;
    }

    @Override
    public boolean agendarCita(Cita cita) {
        // 1. Ejecutar la lógica base (Guardar en BD)
        boolean resultado = servicioDecorado.agendarCita(cita);

        // 2. Si guardó bien, añadir funcionalidad extra (Email)
        if (resultado) {
            enviarEmailSimulado(cita);
        }

        return resultado;
    }

    private void enviarEmailSimulado(Cita cita) {
        // Aquí iría la lógica real con JavaMail
        System.out.println(">>> [EMAIL SYSTEM] Enviando confirmación a paciente ID: " + cita.getIdPaciente());
        System.out.println(">>> Cuerpo: Su cita ha sido confirmada.");
        
        // Mostrar alerta visual para que veas que funciona
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notificación Enviada");
        alert.setHeaderText("Correo enviado exitosamente");
        alert.setContentText("Se ha notificado al paciente del turno asignado.");
        alert.show();
    }
}