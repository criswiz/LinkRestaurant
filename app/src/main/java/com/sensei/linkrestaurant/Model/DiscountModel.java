package com.sensei.linkrestaurant.Model;

import java.util.List;

public class DiscountModel {
    private boolean success;
    private String message;
    List<Discount> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Discount> getResult() {
        return result;
    }

    public void setResult(List<Discount> result) {
        this.result = result;
    }
}
