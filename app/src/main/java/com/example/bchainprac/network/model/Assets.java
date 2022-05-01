package com.example.bchainprac.network.model;


public class Assets {
    private String assetId;
    private String medicineId;
    private String id;
    private String userId;

    public Assets(){

    }

    public Assets(String assetId, String medicineId,String userId) {
        this.assetId = assetId;
        this.medicineId = medicineId;
        this.userId=userId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
