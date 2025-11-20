module com.example.projet_poo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    // --- Configuration des packages WomenShop ---

    // Autorise JavaFX à accéder à tes contrôleurs (pour les lier au FXML)
    opens com.womenshop.controllers to javafx.fxml;

    // Si jamais tu as des classes dans com.womenshop.views ou autre, ouvre le parent :
    opens com.womenshop to javafx.fxml;

    // Exporte ton package principal (où se trouve App.java) pour que Java puisse le lancer
    exports com.womenshop;

    // Exporte les autres packages utiles
    exports com.womenshop.models;
    exports com.womenshop.utils;
    exports com.womenshop.services;
    exports com.womenshop.controllers;
}