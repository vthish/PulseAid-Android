package com.pulseaid.data.admin;

public class Donor {
    private String uid;
    private String name;
    private String email;
    private String role;
    private String bloodGroup;

    public Donor() {
    }

    public Donor(String uid, String name, String email, String role, String bloodGroup) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.bloodGroup = bloodGroup;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getBloodGroup() { return bloodGroup; }

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
}