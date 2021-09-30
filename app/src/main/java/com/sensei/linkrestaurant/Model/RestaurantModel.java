package com.sensei.linkrestaurant.Model;

import java.util.List;

public class RestaurantModel {
    private boolean success;
    private String result;
    private List<Restaurant> message;

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

    public List<Restaurant> getMessage() {
        return message;
    }

    public void setMessage(List<Restaurant> message) {
        this.message = message;
    }
}
