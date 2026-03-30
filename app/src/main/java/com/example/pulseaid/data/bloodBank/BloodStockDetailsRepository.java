package com.example.pulseaid.data.bloodBank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodStockDetailsRepository {

    // Simulating fetching data (this could be from Firebase later)
    public interface DetailsCallback {
        void onSuccess(Map<String, String> compatibilityInfo, List<DummyDonor> donors);
        void onFailure(String error);
    }

    public void fetchDetails(String bloodGroup, DetailsCallback callback) {
        try {
            // 1. Fetch Compatibility Info
            Map<String, String> info = getCompatibilityInfo(bloodGroup);

            // 2. Fetch Donors (Dummy for now)
            List<DummyDonor> donors = getDummyDonors(bloodGroup);

            callback.onSuccess(info, donors);
        } catch (Exception e) {
            callback.onFailure("Failed to load details.");
        }
    }

    private Map<String, String> getCompatibilityInfo(String bg) {
        Map<String, String> info = new HashMap<>();
        String donateTo = "";
        String receiveFrom = "";

        switch (bg) {
            case "A+":
                donateTo = "A+, AB+";
                receiveFrom = "A+, A-, O+, O-";
                break;
            case "A-":
                donateTo = "A+, A-, AB+, AB-";
                receiveFrom = "A-, O-";
                break;
            case "B+":
                donateTo = "B+, AB+";
                receiveFrom = "B+, B-, O+, O-";
                break;
            case "B-":
                donateTo = "B+, B-, AB+, AB-";
                receiveFrom = "B-, O-";
                break;
            case "AB+":
                donateTo = "AB+";
                receiveFrom = "Everyone (Universal Recipient)";
                break;
            case "AB-":
                donateTo = "AB+, AB-";
                receiveFrom = "AB-, A-, B-, O-";
                break;
            case "O+":
                donateTo = "O+, A+, B+, AB+";
                receiveFrom = "O+, O-";
                break;
            case "O-":
                donateTo = "Everyone (Universal Donor)";
                receiveFrom = "O-";
                break;
            default:
                donateTo = "Unknown";
                receiveFrom = "Unknown";
        }
        info.put("donateTo", donateTo);
        info.put("receiveFrom", receiveFrom);
        return info;
    }

    private List<DummyDonor> getDummyDonors(String bg) {
        List<DummyDonor> donorList = new ArrayList<>();
        donorList.add(new DummyDonor("Kamal Perera", "0771234567", bg, "Last Donated: 2 Months Ago"));
        donorList.add(new DummyDonor("Nimal Silva", "0719876543", bg, "Last Donated: 5 Months Ago"));
        donorList.add(new DummyDonor("Kasun Kalhara", "0751122334", bg, "Last Donated: 1 Year Ago"));
        return donorList;
    }

    // Model class for Donor (Moved here from Activity)
    public static class DummyDonor {
        public String name, phone, bloodGroup, lastDonated;

        public DummyDonor(String name, String phone, String bloodGroup, String lastDonated) {
            this.name = name;
            this.phone = phone;
            this.bloodGroup = bloodGroup;
            this.lastDonated = lastDonated;
        }
    }
}