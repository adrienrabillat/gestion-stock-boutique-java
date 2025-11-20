package com.womenshop.controllers;

import com.womenshop.dao.impl.CategoryDAOImpl;
import com.womenshop.models.Category;
import com.womenshop.models.Product;
import com.womenshop.services.FinancialService;
import com.womenshop.services.FinancialServiceImpl;
import com.womenshop.services.ProductService;
import com.womenshop.services.ProductServiceImpl;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    // --- Éléments de l'interface (liés au FXML via fx:id) ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Long> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPurchasePrice;
    @FXML private TableColumn<Product, Double> colSellPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colCategory;

    @FXML private ComboBox<Category> categoryFilter;

    // Labels pour les finances
    @FXML private Label lblCapital;
    @FXML private Label lblIncome;
    @FXML private Label lblCost;

    // --- Services ---
    private ProductService productService;
    private CategoryDAOImpl categoryDAO;
    private FinancialService financialService;

    // Liste observable pour la table
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private List<Category> allCategories;

    @FXML
    public void initialize() {
        // 1. Instancier les services
        productService = new ProductServiceImpl();
        categoryDAO = new CategoryDAOImpl();
        financialService = new FinancialServiceImpl();

        // 2. Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPurchasePrice.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        colSellPrice.setCellValueFactory(new PropertyValueFactory<>("sellPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Astuce pour afficher le NOM de la catégorie
        colCategory.setCellValueFactory(cellData -> {
            long catId = cellData.getValue().getCategoryId();
            return new SimpleStringProperty(getCategoryNameById(catId));
        });

        // 3. Charger les données initiales
        loadCategories();
        refreshProductTable();
        refreshFinancials();

        // 4. Écouteur sur le filtre
        categoryFilter.setOnAction(event -> filterProducts());
    }

    private void loadCategories() {
        allCategories = categoryDAO.getAll();
        categoryFilter.getItems().clear();

        Category all = new Category(-1, "Toutes les catégories");
        categoryFilter.getItems().add(all);
        categoryFilter.getItems().addAll(allCategories);
        categoryFilter.getSelectionModel().selectFirst();
    }

    private void refreshProductTable() {
        // On garde en mémoire le filtre actuel si possible, sinon on recharge tout
        filterProducts();
    }

    private void filterProducts() {
        Category selected = categoryFilter.getSelectionModel().getSelectedItem();
        productList.clear();

        if (selected == null || selected.getId() == -1) {
            productList.addAll(productService.getAllProducts());
        } else {
            productList.addAll(productService.getProductsByCategory(selected.getId()));
        }
        productTable.setItems(productList);
    }

    public void refreshFinancials() {
        lblCapital.setText(String.format("%.2f €", financialService.getCapital()));
        lblIncome.setText(String.format("%.2f €", financialService.getGlobalIncome()));
        lblCost.setText(String.format("%.2f €", financialService.getGlobalCost()));
    }

    private String getCategoryNameById(long id) {
        if (allCategories == null) return "";
        for (Category c : allCategories) {
            if (c.getId() == id) return c.getName();
        }
        return "Inconnue";
    }

    // ==============================================================
    // --- GESTION DES ACTIONS (CRUD & Vente/Achat) ---
    // ==============================================================

    @FXML
    public void handleAddProduct() {
        openProductForm(null); // null = Mode Création
    }

    @FXML
    public void handleModifyProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un produit à modifier.");
            return;
        }
        openProductForm(selected); // Mode Édition
    }

    @FXML
    public void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucune sélection", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer : " + selected.getName() + " ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            productService.deleteProduct(selected);
            refreshProductTable();
        }
    }

    @FXML
    public void handleSell() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un produit à vendre.");
            return;
        }

        if (selected.getStock() <= 0) {
            showAlert("Stock épuisé", "Ce produit n'est plus en stock !");
            return;
        }

        String qtyStr = showInputIds("Vendre Produit", "Combien d'articles voulez-vous vendre ?");
        if (qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0 && qty <= selected.getStock()) {
                    productService.sellItem(selected, qty);
                    refreshProductTable();
                    refreshFinancials(); // Mise à jour de l'argent !
                } else {
                    showAlert("Erreur", "Quantité invalide ou stock insuffisant.");
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Veuillez entrer un nombre entier valide.");
            }
        }
    }

    @FXML
    public void handlePurchase() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un produit à approvisionner.");
            return;
        }

        String qtyStr = showInputIds("Acheter Stock", "Combien d'articles voulez-vous acheter ?");
        if (qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    productService.purchaseItem(selected, qty);
                    refreshProductTable();
                    refreshFinancials(); // Mise à jour des coûts !
                } else {
                    showAlert("Erreur", "La quantité doit être positive.");
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Veuillez entrer un nombre entier valide.");
            }
        }
    }

    @FXML
    public void handleSortAsc() {
        Category selected = categoryFilter.getSelectionModel().getSelectedItem();
        long catId = (selected != null) ? selected.getId() : -1;
        productList.clear();
        productList.addAll(productService.getProductsByCategorySortedByPrice(catId, "ASC"));
        productTable.setItems(productList);
    }

    @FXML
    public void handleSortDesc() {
        Category selected = categoryFilter.getSelectionModel().getSelectedItem();
        long catId = (selected != null) ? selected.getId() : -1;
        productList.clear();
        productList.addAll(productService.getProductsByCategorySortedByPrice(catId, "DESC"));
        productTable.setItems(productList);
    }

    @FXML
    public void handleApplyDiscount() {
        productService.applyDiscounts();
        refreshProductTable();
    }

    @FXML
    public void handleStopDiscount() {
        productService.stopDiscounts();
        refreshProductTable();
    }

    // --- Méthodes Utilitaires ---

    private void openProductForm(Product productToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/womenshop/views/ProductForm.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            if (productToEdit != null) {
                controller.setProductToEdit(productToEdit);
            }

            Stage stage = new Stage();
            stage.setTitle(productToEdit == null ? "Ajouter Produit" : "Modifier Produit");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshProductTable();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String showInputIds(String title, String content) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}