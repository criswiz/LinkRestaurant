package com.sensei.linkrestaurant.Model;

public class FavoriteOnlyId {
    private int foodId;

    public FavoriteOnlyId(int foodId) {
        this.foodId = foodId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }
}
