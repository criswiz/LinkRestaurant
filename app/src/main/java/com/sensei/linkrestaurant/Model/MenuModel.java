package com.sensei.linkrestaurant.Model;

import java.util.List;

public class MenuModel {
    private boolean success;
    private String result;
    private List<Category> message;

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

    public List<Category> getMessage() {
        return message;
    }

    public void setMessage(List<Category> message) {
        this.message = message;
    }
}
