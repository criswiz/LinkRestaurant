package com.sensei.linkrestaurant.Model;

import java.util.List;

public class DiscountModel {
    private boolean success;
    private String result;
    List<Discount> message;

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

    public List<Discount> getMessage() {
        return message;
    }

    public void setMessage(List<Discount> message) {
        this.message = message;
    }
}
