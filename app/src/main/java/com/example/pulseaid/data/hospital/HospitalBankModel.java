package com.example.pulseaid.data.hospital;

public class HospitalBankModel {
    private String uid;
    private String name;
    private String address;
    private String contactNo;
    private double locationLat;
    private double locationLng;
    private double distanceFromHospital;
    private int unitsToContribute;
    private String providedBloodType;

    public HospitalBankModel() {}

    public String getUid() {
        return uid != null ? uid : "";
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNo() {
        return contactNo != null ? contactNo : "";
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(double locationLng) {
        this.locationLng = locationLng;
    }

    public double getDistanceFromHospital() {
        return distanceFromHospital;
    }

    public void setDistanceFromHospital(double distanceFromHospital) {
        this.distanceFromHospital = distanceFromHospital;
    }

    public int getUnitsToContribute() {
        return unitsToContribute;
    }

    public void setUnitsToContribute(int unitsToContribute) {
        this.unitsToContribute = unitsToContribute;
    }

    public String getProvidedBloodType() {
        return providedBloodType != null ? providedBloodType : "";
    }

    public void setProvidedBloodType(String providedBloodType) {
        this.providedBloodType = providedBloodType;
    }
}