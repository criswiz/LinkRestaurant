package com.sensei.linkrestaurant.Model;

import java.util.List;

public class FavoriteOnlyIdModel {
    private boolean success;
    private String result;
    private List<FavoriteOnlyId> message;

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

    public List<FavoriteOnlyId> getMessage() {
        return message;
    }

    public void setMessage(List<FavoriteOnlyId> message) {
        this.message = message;
    }
}
