package com.sensei.linkrestaurant.Model;

import java.util.List;

public class SizeModel {
    private boolean success;
    private List<Size> message;
    private String result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Size> getMessage() {
        return message;
    }

    public void setMessage(List<Size> message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
