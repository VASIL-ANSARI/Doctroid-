package com.example.bchainprac.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Precautions {
    @SerializedName("english")
    @Expose
    private String english;


    public Precautions(String english) {
        this.english = english;
    }

    public String getEnglish() {
        return english;
    }
}
