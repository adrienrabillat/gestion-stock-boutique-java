package com.womenshop.dao;

import com.womenshop.models.Product;
import java.util.List;

public interface ProductDAO extends DAO<Product> {

    // Récupérer tous les produits d'une catégorie spécifique
    List<Product> getAllByCategory(long categoryId);

    // Récupérer les produits d'une catégorie, triés par prix
    // order sera "ASC" (croissant) ou "DESC" (décroissant)
    List<Product> getAllSortedByPrice(long categoryId, String order);
}