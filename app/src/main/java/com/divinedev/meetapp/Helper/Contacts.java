package com.divinedev.meetapp.Helper;

import android.support.annotation.NonNull;

public class Contacts implements Comparable<Contacts> {
    private String name = null;
    private String phone = null;
    private boolean active = false;
    private String profileURL = null;

    public Contacts(){

    }
    public Contacts(String name,String phone,boolean active,String profileURL){
        this.name = name;
        this.phone = phone;
        this.active = active;
        this.profileURL = profileURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    @Override
    public int compareTo(@NonNull Contacts o) {
        return name.compareTo(o.name);
    }
}
