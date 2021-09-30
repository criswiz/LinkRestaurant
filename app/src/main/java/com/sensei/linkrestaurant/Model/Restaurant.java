package com.sensei.linkrestaurant.Model;

public class Restaurant {
    //Remember all variable names has to be exactly as Json property return from API
    //That will help Gson to parse it correctly

    private int id;
    private String name;
    private String address;
    private String phone;
    private Float lat;
    private Float lng;
    private int userowner;
    private String image;
    private String paymentUrl;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public int getUserowner() {
        return userowner;
    }

    public void setUserowner(int userowner) {
        this.userowner = userowner;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
