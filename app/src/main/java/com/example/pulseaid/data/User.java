package com.example.pulseaid.data;

public class User {
    private String uid;
    private String customId;
    private String name;
    private String email;
    private String role; // Roles: Admin, Blood Staff, Hospital Staff, Donor
    private String bloodType; // Only required if the user is a donor

    // Default constructor
    public User() {
    }

    public User(String uid, String customId, String name, String email, String role, String bloodType) {
        this.uid = uid;
        this.customId = customId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.bloodType = bloodType;
    }

    //Getters
    public String getUid() { return uid; }
    public String getCustomId() { return customId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getBloodType() { return bloodType; }

    public void setUid(String uid) { this.uid = uid; }
    public void setCustomId(String customId) { this.customId = customId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
}