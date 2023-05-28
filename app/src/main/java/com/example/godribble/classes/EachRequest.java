package com.example.godribble.classes;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.model.LatLng;

public class EachRequest {
    private String uid;
    private double latitude, longitude;


    public EachRequest(String uid, double latitude, double longitude) {
        this.uid = uid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng getLatLong(){
        return new LatLng(latitude,longitude);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
