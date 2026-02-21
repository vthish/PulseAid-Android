package com.example.pulseaid.data.admin;

public class Donor {
    private String donorId;
    private String name;
    private String bloodType;
    private String email;

    public Donor() {
        // Default constructor required for Firestore
    }

    public Donor(String donorId, String name, String bloodType, String email) {
        this.donorId = donorId;
        this.name = name;
        this.bloodType = bloodType;
        this.email = email;
    }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}