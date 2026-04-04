package com.example.pulseaid.data.donor;

public class DonorHistoryModel {

    private String centerName;
    private String date;
    private String time;
    private String status;
    private int donatedUnits;
    private String rejectReason;

    public DonorHistoryModel(String centerName, String date, String time,
                             String status, int donatedUnits, String rejectReason) {
        this.centerName = centerName;
        this.date = date;
        this.time = time;
        this.status = status;
        this.donatedUnits = donatedUnits;
        this.rejectReason = rejectReason;
    }

    public String getCenterName() {
        return centerName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public int getDonatedUnits() {
        return donatedUnits;
    }

    public String getRejectReason() {
        return rejectReason;
    }
}