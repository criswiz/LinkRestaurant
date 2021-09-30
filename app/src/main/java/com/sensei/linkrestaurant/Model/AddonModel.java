package com.sensei.linkrestaurant.Model;

import java.util.List;

public class AddonModel {
    private boolean success;
    private String result;
    private List<Addon> message;

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

    public List<Addon> getMessage() {
        return message;
    }

    public void setMessage(List<Addon> message) {
        this.message = message;
    }
}
