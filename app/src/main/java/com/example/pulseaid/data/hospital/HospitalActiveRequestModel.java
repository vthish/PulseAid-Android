package com.example.pulseaid.data.hospital;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HospitalActiveRequestModel {

    @DocumentId
    private String requestId;

    private String hospitalId;
    private String hospitalName;
    private String requestedBloodGroup;
    private Long totalUnits;
    private Long requestDate;
    private Long completedDate;
    private String status;
    private String urgency;
    private List<Map<String, Object>> assignedBanks;

    public HospitalActiveRequestModel() {
    }

    public String getRequestId() {
        return requestId != null ? requestId : "";
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHospitalId() {
        return hospitalId != null ? hospitalId : "";
    }

    public void setHospitalId(String hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName != null ? hospitalName : "";
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getRequestedBloodGroup() {
        return requestedBloodGroup != null ? requestedBloodGroup : "";
    }

    public void setRequestedBloodGroup(String requestedBloodGroup) {
        this.requestedBloodGroup = requestedBloodGroup;
    }

    public Long getTotalUnits() {
        return totalUnits != null ? totalUnits : 0L;
    }

    public void setTotalUnits(Long totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Long getRequestDate() {
        return requestDate != null ? requestDate : 0L;
    }

    public void setRequestDate(Long requestDate) {
        this.requestDate = requestDate;
    }

    public Long getCompletedDate() {
        return completedDate != null ? completedDate : 0L;
    }

    public void setCompletedDate(Long completedDate) {
        this.completedDate = completedDate;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrgency() {
        return urgency != null ? urgency : "";
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public List<Map<String, Object>> getAssignedBanks() {
        return assignedBanks != null ? assignedBanks : new ArrayList<>();
    }

    public void setAssignedBanks(List<Map<String, Object>> assignedBanks) {
        this.assignedBanks = assignedBanks;
    }
}