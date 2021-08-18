package com.sensei.linkrestaurant.Model;

public class Addon {
    private int id;
    private String name, description;
    private Float extraPrice;

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

    public Float getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(Float extraPrice) {
        this.extraPrice = extraPrice;
    }
}
