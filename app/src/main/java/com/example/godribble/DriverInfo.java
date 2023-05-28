package com.example.godribble;

public class DriverInfo {

    String name,email,phone;


    public DriverInfo()
    {

    }

    public DriverInfo(String name, String email, String phone) {
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
