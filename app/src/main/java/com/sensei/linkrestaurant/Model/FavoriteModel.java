package com.sensei.linkrestaurant.Model;

import java.util.List;

public class FavoriteModel {
    private boolean success;
    private String result;
    private List<Favorite> message;

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

    public List<Favorite> getMessage() {
        return message;
    }

    public void setMessage(List<Favorite> message) {
        this.message = message;
    }
}
