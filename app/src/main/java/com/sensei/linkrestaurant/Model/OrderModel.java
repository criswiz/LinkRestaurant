package com.sensei.linkrestaurant.Model;


import java.util.List;

public class OrderModel {
    private boolean success;
    private String result;
    private List<Order> message;

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

    public List<Order> getMessage() {
        return message;
    }

    public void setMessage(List<Order> message) {
        this.message = message;
    }
}
