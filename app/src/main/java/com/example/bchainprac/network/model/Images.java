package com.example.bchainprac.network.model;

import java.io.Serializable;
import java.util.List;

public class Images implements Serializable {
    List<String> urls;

    public Images(){

    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
