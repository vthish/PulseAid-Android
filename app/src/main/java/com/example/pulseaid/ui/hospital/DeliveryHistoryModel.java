package com.example.pulseaid.ui.hospital;

public class DeliveryHistoryModel {
    private String bloodGroup;
    private String units;
    private String sourceBank;
    private String requestedDate;
    private String receivedDateTime;

    public DeliveryHistoryModel(String bloodGroup, String units, String sourceBank, String requestedDate, String receivedDateTime) {
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.sourceBank = sourceBank;
        this.requestedDate = requestedDate;
        this.receivedDateTime = receivedDateTime;
    }

    public String getBloodGroup() { return bloodGroup; }
    public String getUnits() { return units; }
    public String getSourceBank() { return sourceBank; }
    public String getRequestedDate() { return requestedDate; }
    public String getReceivedDateTime() { return receivedDateTime; }
}