package com.example.bchainprac.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Medicine {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("medicine_Name")
    @Expose
    private String name;
    @SerializedName("medicine_Price")
    @Expose
    private String price;
    @SerializedName("medicine_Description")
    @Expose
    private String description;
    @SerializedName("medicine_Quantity")
    @Expose
    private String quantity;
    @SerializedName("hospital_id")
    @Expose
    private String hospitalId;
    @SerializedName("brand")
    @Expose
    private String brand;


    @Override
    public String toString() {
        return name;
    }

    public Medicine() {

    }

    public Medicine(String name) {
        this.name = name;
    }

    public Medicine(String id, String s) {
        this.id = id;
    }



    public Medicine(String id, String name, String price, String description, String quantity, String brand,String hospitalId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.brand = brand;
        this.hospitalId=hospitalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }
}
