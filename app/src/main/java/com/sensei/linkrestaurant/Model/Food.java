package com.sensei.linkrestaurant.Model;

public class Food {
    private int id;
    private String name, description, image;
    private Double price;
    private boolean isSize, isAddon;
    private Double discount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean isSize() {
        return isSize;
    }

    public void setSize(boolean size) {
        this.isSize = size;
    }

    public boolean isAddon() {
        return isAddon;
    }

    public void setAddon(boolean addon) {
        isAddon = addon;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}
