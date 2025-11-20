package com.womenshop.services;

import com.womenshop.dao.ProductDAO;
import com.womenshop.dao.impl.ProductDAOImpl;
import com.womenshop.models.*;

import java.util.List;

public class ProductServiceImpl implements ProductService {

    private ProductDAO productDAO;
    private FinancialService financialService;

    public ProductServiceImpl() {
        this.productDAO = new ProductDAOImpl();
        this.financialService = new FinancialServiceImpl();
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.getAll();
    }

    @Override
    public List<Product> getProductsByCategory(long categoryId) {
        // Si categoryId est 0 ou -1 (ex: "Toutes"), on renvoie tout
        if (categoryId <= 0) return getAllProducts();
        return productDAO.getAllByCategory(categoryId);
    }

    @Override
    public List<Product> getProductsByCategorySortedByPrice(long categoryId, String order) {
        List<Product> products;

        if (categoryId <= 0) {
            // CORRECTION ICI : Si "Toutes les catégories", on charge tout et on trie en Java
            products = productDAO.getAll();

            products.sort((p1, p2) -> {
                if ("DESC".equalsIgnoreCase(order)) {
                    return Double.compare(p2.getSellPrice(), p1.getSellPrice()); // Décroissant
                } else {
                    return Double.compare(p1.getSellPrice(), p2.getSellPrice()); // Croissant
                }
            });
        } else {
            // Sinon, on laisse la base de données faire le tri (plus performant)
            products = productDAO.getAllSortedByPrice(categoryId, order);
        }

        return products;
    }

    @Override
    public void saveProduct(Product p) {
        // Validation basique
        if (p.getPurchasePrice() < 0 || p.getSellPrice() < 0) {
            throw new IllegalArgumentException("Les prix ne peuvent pas être négatifs.");
        }
        productDAO.save(p);
    }

    @Override
    public void updateProduct(Product p) {
        productDAO.update(p);
    }

    @Override
    public void deleteProduct(Product p) {
        productDAO.delete(p);
    }

    @Override
    public void sellItem(Product p, int quantity) {
        if (quantity > p.getStock()) {
            throw new IllegalArgumentException("Stock insuffisant !");
        }
        // 1. Mettre à jour le stock
        p.setStock(p.getStock() - quantity);
        productDAO.update(p);

        // 2. Ajouter aux revenus (Income)
        double income = p.getSellPrice() * quantity;
        financialService.addIncome(income);
    }

    @Override
    public void purchaseItem(Product p, int quantity) {
        // 1. Mettre à jour le stock
        p.setStock(p.getStock() + quantity);
        productDAO.update(p);

        // 2. Ajouter aux coûts (Cost)
        double cost = p.getPurchasePrice() * quantity;
        financialService.addCost(cost);
    }

    @Override
    public void applyDiscounts() {
        List<Product> allProducts = productDAO.getAll();
        for (Product p : allProducts) {
            // On restaure d'abord le prix original pour ne pas appliquer la réduction deux fois
            double original = p.getOriginalSellPrice();
            double newPrice = original;

            // Règles fixes énoncées
            if (p instanceof Clothes) {
                newPrice = original * 0.70; // -30%
            } else if (p instanceof Shoes) {
                newPrice = original * 0.80; // -20%
            } else if (p instanceof Accessory) {
                newPrice = original * 0.50; // -50%
            }

            p.setSellPrice(newPrice);
            productDAO.update(p);
        }
    }

    @Override
    public void stopDiscounts() {
        List<Product> allProducts = productDAO.getAll();
        for (Product p : allProducts) {
            // On remet le prix de vente au prix original
            p.setSellPrice(p.getOriginalSellPrice());
            productDAO.update(p);
        }
    }
}