package com.pulseaid.ui.hospital;

public class ActiveRequestModel {
    private String bloodGroup;
    private String units;
    private String dateRequested;
    private String statusMessage;
    // 0 = Requested, 1 = Accepted by Bank, 2 = Dispatched
    private int currentStep;

    public ActiveRequestModel(String bloodGroup, String units, String dateRequested, String statusMessage, int currentStep) {
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.dateRequested = dateRequested;
        this.statusMessage = statusMessage;
        this.currentStep = currentStep;
    }

    public String getBloodGroup() { return bloodGroup; }
    public String getUnits() { return units; }
    public String getDateRequested() { return dateRequested; }
    public String getStatusMessage() { return statusMessage; }
    public int getCurrentStep() { return currentStep; }
}