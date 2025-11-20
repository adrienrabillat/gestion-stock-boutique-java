package com.womenshop.models;

public class Clothes extends Product {
    private String size;
    private String color;

    public Clothes(long id, String name, double purchasePrice, double sellPrice, int stock, long categoryId, String size, String color) {
        super(id, name, purchasePrice, sellPrice, stock, categoryId);
        this.size = size;
        this.color = color;
    }
    public Clothes() {}
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}