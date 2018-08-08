package com.divinedev.meetapp.Helper;

public class Marker {
    private final String phone;
    private final String lat;
    private final String lon;

    public Marker(String phone, String lat, String lon){

        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
    }

    public String getPhone() {
        return phone;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
}
