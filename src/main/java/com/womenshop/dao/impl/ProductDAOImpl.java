package com.womenshop.dao.impl;

import com.womenshop.dao.ProductDAO;
import com.womenshop.models.*;
import com.womenshop.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    private Connection connection;

    public ProductDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Cette requête SQL géante permet de récupérer toutes les infos
     * qu'il s'agisse d'un vêtement, d'une chaussure ou d'un accessoire.
     */
    private static final String SELECT_ALL_QUERY =
            "SELECT p.*, " +
                    "c.size as c_size, c.color as c_color, " +
                    "s.size_num as s_size, " +
                    "a.type as a_type " +
                    "FROM product p " +
                    "LEFT JOIN clothes c ON p.id = c.id " +
                    "LEFT JOIN shoes s ON p.id = s.id " +
                    "LEFT JOIN accessory a ON p.id = a.id";

    @Override
    public Product get(long id) {
        String sql = SELECT_ALL_QUERY + " WHERE p.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_QUERY)) {
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> getAllByCategory(long categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_ALL_QUERY + " WHERE p.category_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> getAllSortedByPrice(long categoryId, String order) {
        // Attention : on ne peut pas utiliser '?' pour ASC/DESC, on concatène la string
        // On valide l'ordre pour éviter les injections SQL
        String safeOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
        String sql = SELECT_ALL_QUERY + " WHERE p.category_id = ? ORDER BY p.sell_price " + safeOrder;

        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                if (p != null) products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public void save(Product p) {
        String insertParent = "INSERT INTO product (name, purchase_price, sell_price, original_sell_price, stock, category_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertParent, Statement.RETURN_GENERATED_KEYS)) {
            // 1. Sauvegarder dans la table parent 'product'
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getPurchasePrice());
            stmt.setDouble(3, p.getSellPrice());
            stmt.setDouble(4, p.getOriginalSellPrice());
            stmt.setInt(5, p.getStock());
            stmt.setLong(6, p.getCategoryId());
            stmt.executeUpdate();

            // Récupérer l'ID généré
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                p.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Échec de la création du produit, pas d'ID obtenu.");
            }

            // 2. Sauvegarder dans la table enfant correspondante
            if (p instanceof Clothes) {
                saveClothes((Clothes) p);
            } else if (p instanceof Shoes) {
                saveShoes((Shoes) p);
            } else if (p instanceof Accessory) {
                saveAccessory((Accessory) p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveClothes(Clothes c) throws SQLException {
        String sql = "INSERT INTO clothes (id, size, color) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, c.getId());
            stmt.setString(2, c.getSize());
            stmt.setString(3, c.getColor());
            stmt.executeUpdate();
        }
    }

    private void saveShoes(Shoes s) throws SQLException {
        String sql = "INSERT INTO shoes (id, size_num) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, s.getId());
            stmt.setDouble(2, s.getSize());
            stmt.executeUpdate();
        }
    }

    private void saveAccessory(Accessory a) throws SQLException {
        String sql = "INSERT INTO accessory (id, type) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, a.getId());
            stmt.setString(2, a.getType());
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Product p) {
        // Mise à jour du parent
        String updateParent = "UPDATE product SET name=?, purchase_price=?, sell_price=?, original_sell_price=?, stock=?, category_id=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(updateParent)) {
            stmt.setString(1, p.getName());
            stmt.setDouble(2, p.getPurchasePrice());
            stmt.setDouble(3, p.getSellPrice());
            stmt.setDouble(4, p.getOriginalSellPrice());
            stmt.setInt(5, p.getStock());
            stmt.setLong(6, p.getCategoryId());
            stmt.setLong(7, p.getId());
            stmt.executeUpdate();

            // Mise à jour de l'enfant
            if (p instanceof Clothes) {
                updateClothes((Clothes) p);
            } else if (p instanceof Shoes) {
                updateShoes((Shoes) p);
            } else if (p instanceof Accessory) {
                updateAccessory((Accessory) p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateClothes(Clothes c) throws SQLException {
        String sql = "UPDATE clothes SET size=?, color=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getSize());
            stmt.setString(2, c.getColor());
            stmt.setLong(3, c.getId());
            stmt.executeUpdate();
        }
    }

    private void updateShoes(Shoes s) throws SQLException {
        String sql = "UPDATE shoes SET size_num=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, s.getSize());
            stmt.setLong(2, s.getId());
            stmt.executeUpdate();
        }
    }

    private void updateAccessory(Accessory a) throws SQLException {
        String sql = "UPDATE accessory SET type=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, a.getType());
            stmt.setLong(2, a.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Product p) {
        // Grâce au "ON DELETE CASCADE" dans le SQL, supprimer le parent suffit !
        // MySQL supprimera automatiquement la ligne correspondante dans clothes/shoes/accessory.
        String sql = "DELETE FROM product WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode magique qui transforme une ligne SQL (avec plein de NULLs)
     * en le bon objet Java (Clothes, Shoes ou Accessory).
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        double purchasePrice = rs.getDouble("purchase_price");
        double sellPrice = rs.getDouble("sell_price");
        double originalSellPrice = rs.getDouble("original_sell_price");
        int stock = rs.getInt("stock");
        long categoryId = rs.getLong("category_id");

        // On regarde quelles colonnes ne sont pas nulles pour deviner le type
        String cSize = rs.getString("c_size");
        if (cSize != null) {
            Clothes c = new Clothes(id, name, purchasePrice, sellPrice, stock, categoryId, cSize, rs.getString("c_color"));
            c.setOriginalSellPrice(originalSellPrice);
            return c;
        }

        // Astuce : getDouble renvoie 0.0 si NULL, donc on checke l'objet
        Object sSizeObj = rs.getObject("s_size");
        if (sSizeObj != null) {
            Shoes s = new Shoes(id, name, purchasePrice, sellPrice, stock, categoryId, rs.getDouble("s_size"));
            s.setOriginalSellPrice(originalSellPrice);
            return s;
        }

        String aType = rs.getString("a_type");
        if (aType != null) {
            Accessory a = new Accessory(id, name, purchasePrice, sellPrice, stock, categoryId, aType);
            a.setOriginalSellPrice(originalSellPrice);
            return a;
        }

        return null; // Ne devrait pas arriver si la DB est intègre
    }
}