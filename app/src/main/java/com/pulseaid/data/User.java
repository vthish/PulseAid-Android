package com.pulseaid.data;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String uid;
    private String name;
    private String email;
    private String role;
    private boolean isProfileComplete;


    public User() {
    }

    public User(String uid, String name, String email, String role, boolean isProfileComplete) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isProfileComplete = isProfileComplete;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean getIsProfileComplete() { return isProfileComplete; }

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setIsProfileComplete(boolean profileComplete) { isProfileComplete = profileComplete; }
}