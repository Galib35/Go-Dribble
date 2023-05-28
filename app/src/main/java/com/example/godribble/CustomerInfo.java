package com.example.godribble;

public class CustomerInfo {


    String name,email,phone;


    public CustomerInfo()
    {

    }

    public CustomerInfo(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
