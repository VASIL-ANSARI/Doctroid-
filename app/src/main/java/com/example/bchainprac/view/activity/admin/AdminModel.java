package com.example.bchainprac.view.activity.admin;

import java.util.List;

public class AdminModel {
    private List<String> lists;
    private String type;
    private String senderId;
    private String id;
    private boolean done;

    public AdminModel(){

    }

    public AdminModel(List<String> lists, String type, String senderId) {
        this.lists = lists;
        this.type = type;
        this.senderId = senderId;
        done=false;
    }


    public List<String> getLists() {
        return lists;
    }

    public void setLists(List<String> lists) {
        this.lists = lists;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
