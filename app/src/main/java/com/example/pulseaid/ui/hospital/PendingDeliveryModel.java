package com.example.pulseaid.ui.hospital;

public class PendingDeliveryModel {
    private String bloodGroup;
    private String units;
    private String sourceBank;
    private String time;

    public PendingDeliveryModel(String bloodGroup, String units, String sourceBank, String time) {
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.sourceBank = sourceBank;
        this.time = time;
    }

    public String getBloodGroup() { return bloodGroup; }
    public String getUnits() { return units; }
    public String getSourceBank() { return sourceBank; }
    public String getTime() { return time; }
}