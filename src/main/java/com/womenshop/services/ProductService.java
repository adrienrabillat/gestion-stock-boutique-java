package com.womenshop.services;

import com.womenshop.models.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(long categoryId);
    List<Product> getProductsByCategorySortedByPrice(long categoryId, String order);

    void saveProduct(Product p);
    void updateProduct(Product p);
    void deleteProduct(Product p);

    // Actions métier
    void sellItem(Product p, int quantity);
    void purchaseItem(Product p, int quantity);

    void applyDiscounts();
    void stopDiscounts();
}