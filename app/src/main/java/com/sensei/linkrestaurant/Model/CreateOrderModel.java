package com.sensei.linkrestaurant.Model;

import java.util.List;

public class CreateOrderModel {
    private boolean success;
    private List<CreateOrder> message;
    private String result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CreateOrder> getMessage() {
        return message;
    }

    public void setMessage(List<CreateOrder> message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
