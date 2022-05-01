package com.example.bchainprac.network.model;

import com.algorand.algosdk.crypto.Address;
import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class wallet {

    @Exclude
    @SerializedName("address")
    @Expose
    Address address;
    @SerializedName("menomic")
    @Expose
    String menomic;
    @SerializedName("encrypt")
    @Expose
    boolean encrypt;

    public wallet(){

    }

    public wallet(Address address, String menomic) {
        this.address = address;
        this.menomic = menomic;
        encrypt=false;
    }


    @Exclude
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getMenomic() {
        return menomic;
    }

    public void setMenomic(String menomic) {
        this.menomic = menomic;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }
}
