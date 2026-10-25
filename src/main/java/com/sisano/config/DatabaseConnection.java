package com.sisano.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Volatile asegura visibilidad correcta en hilos concurrentes
    private static volatile DatabaseConnection instance;
    private Connection connection;

    // Desacoplamiento de configuración 
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SISANO_DB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "123456";

    // Constructor vacío:  solo inicializa la estructura
    private DatabaseConnection() {
        
    }

    // Paso 2: getInstance simple y puro. Solo controla la instancia de la CLASE.
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Paso 1: La lógica de conexión/reconexión se mueve aquí
    public Connection getConnection() {
        try {
            // Verificamos si es null O si está cerrada para reconectar
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión a SQL Server establecida/renovada.");
            }
        } catch (SQLException e) {
            System.err.println("Error conectando a la BD: " + e.getMessage());
            // Opcional: Podrías retornar null o lanzar una RuntimeException aquí
        }
        return connection;
    }
}