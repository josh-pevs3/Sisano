package com.sisano.config;

import com.sisano.repository.*;
import com.sisano.repository.impl.*;

public class RepositoryFactory {
    // Devuelve siempre las interfaces, nunca las implementaciones
    public static IDisponibilidadRepository getDisponibilidadRepo() {
        return new DisponibilidadRepositoryImpl();
    }

    public static ISalaRepository getSalaRepo() {
        return new SalaRepositoryImpl();
    }

    public static IMedicoRepository getMedicoRepo() {
        return new MedicoRepositoryImpl();
    }
    public static IDisponibilidadRepository getDisponibilidadRepository() {
        return new DisponibilidadRepositoryImpl();
    }
    public static ICitaRepository getCitaRepo() {
        return new CitaRepositoryImpl();
    }
    public static IUsuarioRepository getUsuarioRepo() {
        return new UsuarioRepositoryImpl();
    }
    public static IPacienteRepository getPacienteRepo() {
        return new PacienteRepositoryImpl();
    }

}