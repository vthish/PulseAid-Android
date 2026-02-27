package com.example.pulseaid.data.admin;

public class Donor {
    private String id;
    private String name;
    private String email;
    private String bloodGroup;

    public Donor() {
    }

    public Donor(String id, String name, String email, String bloodGroup) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bloodGroup = bloodGroup;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
}