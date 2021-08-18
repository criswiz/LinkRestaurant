package com.sensei.linkrestaurant.Model;

public class User {

    private String fbid,name,key,userPhone,address;

    public User() {
    }

    public User(String fbid, String name, String key, String userPhone, String address) {
        this.fbid = fbid;
        this.name = name;
        this.key = key;
        this.userPhone = userPhone;
        this.address = address;
    }

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        this.fbid = fbid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
