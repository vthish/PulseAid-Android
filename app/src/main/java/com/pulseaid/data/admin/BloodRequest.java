package com.pulseaid.data.admin;

public class BloodRequest {
    private String id;
    private String hospitalId;
    private String hospitalName;
    private String bloodGroup;
    private int quantity;
    private String urgency;
    private String reason;
    private String status;
    private long requestDate;

    public BloodRequest() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getRequestDate() { return requestDate; }
    public void setRequestDate(long requestDate) { this.requestDate = requestDate; }
}