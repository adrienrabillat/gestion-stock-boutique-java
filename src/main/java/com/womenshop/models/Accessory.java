package com.womenshop.models;

public class Accessory extends Product {
    private String type;

    public Accessory(long id, String name, double purchasePrice, double sellPrice, int stock, long categoryId, String type) {
        super(id, name, purchasePrice, sellPrice, stock, categoryId);
        this.type = type;
    }
    public Accessory() {}
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}