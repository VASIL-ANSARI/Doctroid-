package com.example.bchainprac.network.model;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MedicalAnalysis implements Serializable {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("test_title")
    @Expose
    private String title;
    @SerializedName("test_period")
    @Expose
    private String period;
    @SerializedName("test_price")
    @Expose
    private String price;
    @SerializedName("test_description")
    @Expose
    @Exclude
    private String description;
    @SerializedName("test_precautions")
    @Expose
    private String precautions;

    public MedicalAnalysis(){

    }

    public MedicalAnalysis(String id, String title, String period, String price, String precautions) {
        this.id = id;
        this.title = title;
        this.period = period;
        this.price = price;
        this.description = description;
        this.precautions = precautions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }
}
