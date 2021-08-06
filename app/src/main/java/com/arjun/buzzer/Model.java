package com.arjun.buzzer;

public class Model {
    String name;
    String Image;
    String UID;
    String Status;

    public Model(String name, String image, String uid, String status) {
        this.name = name;
        this.Image = image;
        this.UID = uid;
        this.Status = status;
    }

    public Model() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }

    public String getUid() {
        return UID;
    }

    public void setUid(String uid) {
        this.UID = uid;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }
}
