package com.example.bchainprac.network.model;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Hospital {
    private String hospital_id;
    private String hospital_name;
    private String hospital_location;
    private String hospital_phone;
    private String hospital_website;
    private String hospital_facebook;
    private String hospital_email;
    private String hospital_generalManager;
    private String hospital_adminstratonManager;
    private String hospital_itManager;
    private String hospital_MarketingManager;
    private String hospital_PurchasingManager;
    private double latitude;
    private double longitude;
    private Boolean verified;
    private int ownerVerified;


    public Hospital() {

    }

    public Hospital(String hospital_name, String hospital_location, String hospital_phone,double latitude,double longitude, String hospital_website, String hospital_facebook, String hospital_email, String hospital_generalManager, String hospital_adminstratonManager, String hospital_itManager, String hospital_MarketingManage, String hospital_PurchasingManager) {
        this.longitude=longitude;
        this.latitude=latitude;
        this.hospital_name = hospital_name;
        this.hospital_location = hospital_location;
        this.hospital_phone = hospital_phone;
        this.hospital_website = hospital_website;
        this.hospital_facebook = hospital_facebook;
        this.hospital_email = hospital_email;
        this.hospital_generalManager = hospital_generalManager;
        this.hospital_adminstratonManager = hospital_adminstratonManager;
        this.hospital_itManager = hospital_itManager;
        this.hospital_MarketingManager = hospital_MarketingManage;
        this.hospital_PurchasingManager = hospital_PurchasingManager;
    }

    public Hospital(String hospital_name, String hospital_location, String hospital_phone,double latitude,double longitude) {
        this.hospital_name = hospital_name;
        this.hospital_location = hospital_location;
        this.hospital_phone = hospital_phone;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public String getHospital_id() {
        return hospital_id;
    }

    public void setHospital_id(String hospital_id) {
        this.hospital_id = hospital_id;
    }

    public String getHospital_name() {
        return hospital_name;
    }

    public void setHospital_name(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    public String getHospital_location() {
        return hospital_location;
    }

    public void setHospital_location(String hospital_location) {
        this.hospital_location = hospital_location;
    }

    public String getHospital_phone() {
        return hospital_phone;
    }

    public void setHospital_phone(String hospital_phone) {
        this.hospital_phone = hospital_phone;
    }

    public String getHospital_website() {
        return hospital_website;
    }

    public void setHospital_website(String hospital_website) {
        this.hospital_website = hospital_website;
    }

    public String getHospital_facebook() {
        return hospital_facebook;
    }

    public void setHospital_facebook(String hospital_facebook) {
        this.hospital_facebook = hospital_facebook;
    }

    public String getHospital_email() {
        return hospital_email;
    }

    public void setHospital_email(String hospital_email) {
        this.hospital_email = hospital_email;
    }

    public String getHospital_generalManager() {
        return hospital_generalManager;
    }

    public void setHospital_generalManager(String hospital_generalManager) {
        this.hospital_generalManager = hospital_generalManager;
    }

    public String getHospital_adminstratonManager() {
        return hospital_adminstratonManager;
    }

    public void setHospital_adminstratonManager(String hospital_adminstratonManager) {
        this.hospital_adminstratonManager = hospital_adminstratonManager;
    }

    public String getHospital_itManager() {
        return hospital_itManager;
    }

    public void setHospital_itManager(String hospital_itManager) {
        this.hospital_itManager = hospital_itManager;
    }

    public String getHospital_MarketingManager() {
        return hospital_MarketingManager;
    }

    public void setHospital_MarketingManage(String hospital_MarketingManage) {
        this.hospital_MarketingManager = hospital_MarketingManage;
    }

    public String getHospital_PurchasingManager() {
        return hospital_PurchasingManager;
    }

    public void setHospital_PurchasingManager(String hospital_PurchasingManager) {
        this.hospital_PurchasingManager = hospital_PurchasingManager;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setHospital_MarketingManager(String hospital_MarketingManager) {
        this.hospital_MarketingManager = hospital_MarketingManager;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public int getOwnerVerified() {
        return ownerVerified;
    }

    public void setOwnerVerified(int ownerVerified) {
        this.ownerVerified = ownerVerified;
    }

    public void incOwner(){
        ownerVerified+=1;
    }
}
