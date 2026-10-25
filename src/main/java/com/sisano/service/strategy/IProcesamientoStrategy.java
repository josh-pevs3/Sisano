package com.sisano.service.strategy;

import com.sisano.model.Cita;

public interface IProcesamientoStrategy {
    void procesar(Cita cita);
    String getNombreMovimiento();
}