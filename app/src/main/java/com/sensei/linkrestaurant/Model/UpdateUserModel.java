package com.sensei.linkrestaurant.Model;

public class UpdateUserModel {

    private boolean success;
    private String message;

    public UpdateUserModel() {
    }

    public UpdateUserModel(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

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
}
