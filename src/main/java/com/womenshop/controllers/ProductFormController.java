package com.womenshop.controllers;

import com.womenshop.dao.impl.CategoryDAOImpl;
import com.womenshop.models.*;
import com.womenshop.services.ProductService;
import com.womenshop.services.ProductServiceImpl;
import com.womenshop.utils.ValidationUtil; // Import de notre validateur

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProductFormController {

    @FXML private ComboBox<Category> comboCategory;
    @FXML private TextField txtName;
    @FXML private TextField txtPurchasePrice;
    @FXML private TextField txtSellPrice;
    @FXML private TextField txtStock;
    @FXML private VBox specificFieldsContainer;

    // Champs dynamiques
    private TextField txtSize = new TextField();
    private TextField txtColor = new TextField(); // Pour Clothes
    private TextField txtShoesSize = new TextField(); // Pour Shoes
    private TextField txtType = new TextField(); // Pour Accessory

    private ProductService productService;
    private Product productToEdit; // Si null = Création, sinon = Modification

    @FXML
    public void initialize() {
        productService = new ProductServiceImpl();
        CategoryDAOImpl categoryDAO = new CategoryDAOImpl();

        // 1. Charger les catégories
        comboCategory.getItems().addAll(categoryDAO.getAll());

        // 2. Écouter le changement de catégorie pour afficher les bons champs
        comboCategory.setOnAction(e -> updateSpecificFields());
    }

    private void updateSpecificFields() {
        specificFieldsContainer.getChildren().clear();
        Category selected = comboCategory.getValue();
        if (selected == null) return;

        String catName = selected.getName().toLowerCase();

        if (catName.equals("clothes")) {
            specificFieldsContainer.getChildren().addAll(new Label("Taille (S, M, L...):"), txtSize);
            specificFieldsContainer.getChildren().addAll(new Label("Couleur:"), txtColor);
        } else if (catName.equals("shoes")) {
            specificFieldsContainer.getChildren().addAll(new Label("Pointure (ex: 38.5):"), txtShoesSize);
        } else if (catName.equals("accessory")) {
            specificFieldsContainer.getChildren().addAll(new Label("Type (Sac, Ceinture...):"), txtType);
        }
    }

    @FXML
    private void handleSave() {
        // --- 1. VALIDATION DES DONNÉES (Exigence 8) ---

        if (comboCategory.getValue() == null) {
            ValidationUtil.showError("Erreur de saisie", "Veuillez sélectionner une catégorie.");
            return;
        }

        if (ValidationUtil.isStringEmpty(txtName.getText())) {
            ValidationUtil.showError("Erreur de saisie", "Le nom du produit ne peut pas être vide.");
            return;
        }

        if (!ValidationUtil.isPositiveDouble(txtPurchasePrice.getText())) {
            ValidationUtil.showError("Erreur de prix", "Le prix d'achat doit être un nombre valide (ex: 10.5).");
            return;
        }

        if (!ValidationUtil.isPositiveDouble(txtSellPrice.getText())) {
            ValidationUtil.showError("Erreur de prix", "Le prix de vente doit être un nombre valide.");
            return;
        }

        if (!ValidationUtil.isPositiveInt(txtStock.getText())) {
            ValidationUtil.showError("Erreur de stock", "Le stock doit être un nombre entier positif.");
            return;
        }

        // --- 2. TRAITEMENT ET SAUVEGARDE ---
        try {
            Category cat = comboCategory.getValue();
            String name = txtName.getText();
            double pPrice = Double.parseDouble(txtPurchasePrice.getText());
            double sPrice = Double.parseDouble(txtSellPrice.getText());
            int stock = Integer.parseInt(txtStock.getText());

            // Créer l'objet spécifique selon la catégorie
            Product p = null;
            String catName = cat.getName().toLowerCase();

            if (catName.equals("clothes")) {
                // Validation spécifique rapide pour clothes
                if (ValidationUtil.isStringEmpty(txtSize.getText())) {
                    ValidationUtil.showError("Erreur", "La taille est requise pour les vêtements.");
                    return;
                }
                p = new Clothes(0, name, pPrice, sPrice, stock, cat.getId(), txtSize.getText(), txtColor.getText());

            } else if (catName.equals("shoes")) {
                // Validation spécifique pour shoes
                if (!ValidationUtil.isPositiveDouble(txtShoesSize.getText())) {
                    ValidationUtil.showError("Erreur", "La pointure doit être un nombre valide.");
                    return;
                }
                p = new Shoes(0, name, pPrice, sPrice, stock, cat.getId(), Double.parseDouble(txtShoesSize.getText()));

            } else if (catName.equals("accessory")) {
                p = new Accessory(0, name, pPrice, sPrice, stock, cat.getId(), txtType.getText());
            }

            if (p != null) {
                // Si on était en mode édition, on garde l'ID et l'historique des prix
                if (productToEdit != null) {
                    p.setId(productToEdit.getId());
                    p.setOriginalSellPrice(productToEdit.getOriginalSellPrice());
                    productService.updateProduct(p);
                } else {
                    // Nouveau produit
                    productService.saveProduct(p);
                }
                closeWindow();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ValidationUtil.showError("Erreur Système", "Une erreur est survenue lors de la sauvegarde : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    // Cette méthode servira pour pré-remplir le formulaire (Mode Modifier)
    public void setProductToEdit(Product p) {
        this.productToEdit = p;
        txtName.setText(p.getName());
        txtPurchasePrice.setText(String.valueOf(p.getPurchasePrice()));
        txtSellPrice.setText(String.valueOf(p.getSellPrice()));
        txtStock.setText(String.valueOf(p.getStock()));

        // Sélectionner la bonne catégorie dans la liste
        for(Category c : comboCategory.getItems()) {
            if(c.getId() == p.getCategoryId()) {
                comboCategory.setValue(c);
                break;
            }
        }
        // Force la mise à jour des champs spécifiques
        updateSpecificFields();

        // Remplir les champs spécifiques selon le type
        if (p instanceof Clothes) {
            txtSize.setText(((Clothes) p).getSize());
            txtColor.setText(((Clothes) p).getColor());
        } else if (p instanceof Shoes) {
            txtShoesSize.setText(String.valueOf(((Shoes) p).getSize()));
        } else if (p instanceof Accessory) {
            txtType.setText(((Accessory) p).getType());
        }
    }
}