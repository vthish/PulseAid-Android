package com.pulseaid.viewmodel.hospital;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pulseaid.data.hospital.HospitalBankModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class BloodRequestViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public MutableLiveData<List<HospitalBankModel>> suggestedBanks = new MutableLiveData<>();
    public MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public void findSuggestedBanks(String bloodType, int requiredUnits, boolean includeCompatible) {
        try {
            if (auth.getCurrentUser() == null) {
                statusMessage.setValue("User not authenticated!");
                return;
            }

            String currentHospitalUid = auth.getCurrentUser().getUid();

            db.collection("Users").document(currentHospitalUid).get()
                    .addOnSuccessListener(hospitalDoc -> {
                        try {
                            Double hLat = hospitalDoc.getDouble("latitude");
                            Double hLng = hospitalDoc.getDouble("longitude");

                            if (hLat == null || hLng == null) {
                                statusMessage.setValue("Hospital location not found");
                                return;
                            }

                            List<String> targetTypes = new ArrayList<>();
                            targetTypes.add(bloodType);

                            if (includeCompatible) {
                                targetTypes.addAll(getCompatibleTypes(bloodType));
                            }

                            fetchBanksWithPacketCounts(targetTypes, bloodType, requiredUnits, hLat, hLng);

                        } catch (Exception e) {
                            statusMessage.setValue("Data error: " + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            statusMessage.setValue("Execution error: " + e.getMessage());
        }
    }

    private List<String> getCompatibleTypes(String type) {
        Map<String, List<String>> map = new HashMap<>();
        map.put("O+", Arrays.asList("O-"));
        map.put("A+", Arrays.asList("A-", "O+", "O-"));
        map.put("B+", Arrays.asList("B-", "O+", "O-"));
        map.put("AB+", Arrays.asList("AB-", "A+", "A-", "B+", "B-", "O+", "O-"));
        map.put("A-", Arrays.asList("O-"));
        map.put("B-", Arrays.asList("O-"));
        map.put("AB-", Arrays.asList("A-", "B-", "O-"));
        map.put("O-", new ArrayList<>());
        return map.getOrDefault(type, new ArrayList<>());
    }

    private void fetchBanksWithPacketCounts(List<String> targetTypes, String originalType,
                                            int requiredUnits, double hLat, double hLng) {

        db.collection("Users")
                .whereEqualTo("role", "Blood Bank")
                .get()
                .addOnSuccessListener(task -> {

                    List<HospitalBankModel> allBanks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task) {
                        try {
                            HospitalBankModel bank = doc.toObject(HospitalBankModel.class);
                            bank.setUid(doc.getId());

                            double distance = calculateDistance(
                                    hLat, hLng,
                                    bank.getLocationLat(),
                                    bank.getLocationLng()
                            );

                            bank.setDistanceFromHospital(Math.round(distance * 10.0) / 10.0);

                            allBanks.add(bank);

                        } catch (Exception ignored) {}
                    }

                    countPacketsForBanks(allBanks, targetTypes, originalType, requiredUnits);

                })
                .addOnFailureListener(e ->
                        statusMessage.setValue("Fetch error: " + e.getMessage()));
    }

    private void countPacketsForBanks(List<HospitalBankModel> banks,
                                      List<String> targetTypes,
                                      String originalType,
                                      int requiredUnits) {

        Map<String, Map<String, Integer>> bankInventoryMap = new HashMap<>();

        db.collection("BloodPackets")
                .whereEqualTo("status", "AVAILABLE")
                .get()
                .addOnSuccessListener(packetDocs -> {

                    for (QueryDocumentSnapshot doc : packetDocs) {
                        try {
                            String bankId = doc.getString("centerId");
                            String type = doc.getString("bloodGroup");

                            if (bankId == null || type == null) continue;

                            if (!targetTypes.contains(type)) continue;

                            bankInventoryMap.putIfAbsent(bankId, new HashMap<>());
                            Map<String, Integer> typeMap = bankInventoryMap.get(bankId);

                            int count = typeMap.getOrDefault(type, 0);
                            typeMap.put(type, count + 1);

                        } catch (Exception ignored) {}
                    }

                    applySmartAccumulationWithPackets(
                            banks,
                            bankInventoryMap,
                            targetTypes,
                            originalType,
                            requiredUnits
                    );
                })
                .addOnFailureListener(e ->
                        statusMessage.setValue("Packet fetch error: " + e.getMessage()));
    }

    private void applySmartAccumulationWithPackets(
            List<HospitalBankModel> banks,
            Map<String, Map<String, Integer>> inventoryMap,
            List<String> targetTypes,
            String originalType,
            int requiredUnits) {

        try {
            Collections.sort(banks, (b1, b2) ->
                    Double.compare(b1.getDistanceFromHospital(), b2.getDistanceFromHospital()));

            List<HospitalBankModel> selected = new ArrayList<>();
            int remaining = requiredUnits;

            for (HospitalBankModel bank : banks) {

                Map<String, Integer> inv = inventoryMap.get(bank.getUid());
                if (inv == null) continue;

                String typeToTake = "";

                if (inv.containsKey(originalType) && inv.get(originalType) > 0) {
                    typeToTake = originalType;
                } else {
                    for (String t : targetTypes) {
                        if (inv.containsKey(t) && inv.get(t) > 0) {
                            typeToTake = t;
                            break;
                        }
                    }
                }

                if (!typeToTake.isEmpty()) {

                    int available = inv.get(typeToTake);
                    int take = Math.min(available, remaining);

                    bank.setUnitsToContribute(take);
                    bank.setProvidedBloodType(typeToTake);

                    selected.add(bank);
                    remaining -= take;
                }

                if (remaining <= 0) break;
            }

            suggestedBanks.setValue(selected);

        } catch (Exception e) {
            statusMessage.setValue("Logic error: " + e.getMessage());
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        try {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            return dist * 60 * 1.1515 * 1.609344;
        } catch (Exception e) {
            return 999.9;
        }
    }

    public void placeBloodOrder(String originalRequestType, int totalUnits,
                                String urgency, List<HospitalBankModel> selectedBanks) {

        try {
            String uid = auth.getCurrentUser().getUid();

            db.collection("Users").document(uid).get()
                    .addOnSuccessListener(doc -> {

                        String hospitalName = doc.getString("name");

                        Map<String, Object> order = new HashMap<>();
                        order.put("hospitalId", uid);
                        order.put("hospitalName", hospitalName);
                        order.put("requestedBloodGroup", originalRequestType);
                        order.put("totalUnits", totalUnits);
                        order.put("requestDate", System.currentTimeMillis());
                        order.put("status", "Pending");
                        order.put("urgency", urgency);

                        List<Map<String, Object>> banks = new ArrayList<>();

                        for (HospitalBankModel b : selectedBanks) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("bankId", b.getUid());
                            map.put("bankName", b.getName());
                            map.put("bloodTypeProvided", b.getProvidedBloodType());
                            map.put("unitsProvided", b.getUnitsToContribute());
                            map.put("deliveryStatus", "Pending");
                            banks.add(map);
                        }

                        order.put("assignedBanks", banks);

                        db.collection("BloodRequests").add(order);

                    });

        } catch (Exception e) {
            statusMessage.setValue("Order error: " + e.getMessage());
        }
    }
}