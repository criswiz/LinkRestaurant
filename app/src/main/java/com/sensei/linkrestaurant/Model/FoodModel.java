package com.sensei.linkrestaurant.Model;

import java.util.List;

public class FoodModel {
    private boolean success;
    private String result;
    private List<Food> message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Food> getMessage() {
        return message;
    }

    public void setMessage(List<Food> message) {
        this.message = message;
    }
}
