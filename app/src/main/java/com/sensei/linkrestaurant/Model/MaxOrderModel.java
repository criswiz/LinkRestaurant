package com.sensei.linkrestaurant.Model;

import java.util.List;

public class MaxOrderModel {
    private boolean success;
    private String result;
    private List<MaxOrder> message;

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

    public List<MaxOrder> getMessage() {
        return message;
    }

    public void setMessage(List<MaxOrder> message) {
        this.message = message;
    }
}
