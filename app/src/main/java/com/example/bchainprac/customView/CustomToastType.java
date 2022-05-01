package com.example.bchainprac.customView;

public enum CustomToastType {
    SUCCESS("SUCCESS"),
    ERROR("FAILED"),
    WARNING("WARNING"),
    INFO("INFO"),
    DELETE("DELETE"),
    NO_INTERNET("NO INTERNET");

    String str;
    CustomToastType(String s) {
        str=s;
    }
    String getValue(){
        return str;
    }
}
