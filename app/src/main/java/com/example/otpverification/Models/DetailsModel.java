package com.example.otpverification.Models;

public class DetailsModel {
    String userName, email, password, userId;

    public DetailsModel(){}

    public DetailsModel(String email, String password, String userId) {
        this.email = email;
        this.password = password;
        this.userId = userId;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
