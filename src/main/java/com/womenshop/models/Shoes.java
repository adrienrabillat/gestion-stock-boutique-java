package com.womenshop.models;

public class Shoes extends Product {
    private double size;

    public Shoes(long id, String name, double purchasePrice, double sellPrice, int stock, long categoryId, double size) {
        super(id, name, purchasePrice, sellPrice, stock, categoryId);
        this.size = size;
    }
    public Shoes() {}
    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }
}