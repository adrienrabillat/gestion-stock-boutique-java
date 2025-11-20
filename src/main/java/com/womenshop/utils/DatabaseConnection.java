package com.womenshop.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton pattern pour gérer la connexion unique à la DB.
 * Respecte l'organisation DAO demandée[cite: 28].
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Infos de connexion (A ADAPTER AVEC TES INFOS MYSQL)
    private static final String URL = "jdbc:mysql://localhost:3306/womenshop_db";
    private static final String USER = "root";
    private static final String PASSWORD = "***"; // Mets ton mot de passe ici

    private DatabaseConnection() {
        try {
            // Chargement explicite du driver (parfois nécessaire selon les versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données réussie !");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}