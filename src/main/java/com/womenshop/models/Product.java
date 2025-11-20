package com.womenshop.models;

public abstract class Product {
    protected long id;
    protected String name;
    protected double purchasePrice;
    protected double sellPrice;
    protected double originalSellPrice;
    protected int stock;
    protected long categoryId;

    public Product(long id, String name, double purchasePrice, double sellPrice, int stock, long categoryId) {
        this.id = id;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.originalSellPrice = sellPrice;
        this.stock = stock;
        this.categoryId = categoryId;
    }
    public Product() {}

    // Getters/Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double pp) { this.purchasePrice = pp; }
    public double getSellPrice() { return sellPrice; }
    public void setSellPrice(double sp) { this.sellPrice = sp; }
    public double getOriginalSellPrice() { return originalSellPrice; }
    public void setOriginalSellPrice(double osp) { this.originalSellPrice = osp; }
    public int getStock() { return stock; }
    public void setStock(int s) { this.stock = s; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long cId) { this.categoryId = cId; }
}