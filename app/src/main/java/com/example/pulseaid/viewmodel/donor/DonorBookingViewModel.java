package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.donor.DonorRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonorBookingViewModel extends ViewModel {

    private final DonorRepository repository;
    private final MutableLiveData<List<DocumentSnapshot>> centers = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> selectedCenter = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> donorProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> nextQueueNumber = new MutableLiveData<>();
    private final MutableLiveData<String> bookingStatus = new MutableLiveData<>();
    private final MutableLiveData<String> confirmedAppointmentId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEligible = new MutableLiveData<>(true);
    private final MutableLiveData<String> lockReason = new MutableLiveData<>("");

    public DonorBookingViewModel() {
        this.repository = new DonorRepository();
    }

    public void checkDonorEligibility(String uid) {
        isLoading.setValue(true);
        repository.getAppointmentsForDonor(uid, new DonorRepository.OnAppointmentsCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                boolean hasActive = false;
                long lastCompletionDate = 0;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String status = doc.getString("status");
                    Long timestamp = doc.getLong("timestamp");

                    if ("PENDING".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status)) {
                        hasActive = true;
                        break;
                    }

                    if ("COMPLETED".equalsIgnoreCase(status) && timestamp != null) {
                        if (timestamp > lastCompletionDate) {
                            lastCompletionDate = timestamp;
                        }
                    }
                }

                if (hasActive) {
                    isEligible.setValue(false);
                    lockReason.setValue("ACTIVE_EXISTS");
                } else if (lastCompletionDate != 0) {
                    long ninetyDaysInMs = 90L * 24 * 60 * 60 * 1000;
                    long nextEligibleDate = lastCompletionDate + ninetyDaysInMs;

                    if (System.currentTimeMillis() < nextEligibleDate) {
                        isEligible.setValue(false);
                        lockReason.setValue("UNDER_90_DAYS");
                    } else {
                        isEligible.setValue(true);
                        lockReason.setValue("");
                    }
                } else {
                    isEligible.setValue(true);
                    lockReason.setValue("");
                }

                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
            }
        });
    }

    public void fetchDonorProfile(String uid) {
        repository.getProfile(uid, new DonorRepository.OnProfileCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                donorProfile.setValue(document);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void onDistrictSelected(String district) {
        selectedCenter.setValue(null);

        if (district == null || district.equals("Select District") || district.isEmpty()) {
            centers.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);
        repository.getBloodCentersByDistrict(district, new DonorRepository.OnCentersCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot != null) {
                    centers.setValue(querySnapshot.getDocuments());
                } else {
                    centers.setValue(new ArrayList<>());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                centers.setValue(new ArrayList<>());
                isLoading.setValue(false);
            }
        });
    }

    public void onCenterSelected(int position) {
        if (centers.getValue() != null && position >= 0 && position < centers.getValue().size()) {
            selectedCenter.setValue(centers.getValue().get(position));
        } else {
            selectedCenter.setValue(null);
        }
    }

    public void checkAvailability(String centerId, String date, String timeSlot) {
        isLoading.setValue(true);
        repository.getAppointmentsForSlot(centerId, date, timeSlot, new DonorRepository.OnAppointmentsCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                int currentBookings = 0;
                int maxQueue = 0;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String status = doc.getString("status");

                    if ("PENDING".equalsIgnoreCase(status)) {
                        currentBookings++;

                        Long q = doc.getLong("queueNo");
                        if (q != null && q > maxQueue) {
                            maxQueue = q.intValue();
                        }
                    }
                }

                if (currentBookings >= 10) {
                    bookingStatus.setValue("FULL");
                    nextQueueNumber.setValue(-1);
                } else {
                    nextQueueNumber.setValue(maxQueue + 1);
                    bookingStatus.setValue("AVAILABLE");
                }

                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
            }
        });
    }

    public void confirmBooking(String donorUid, String centerId, String date, String timeSlot, int queueNo) {
        isLoading.setValue(true);

        Map<String, Object> data = new HashMap<>();
        data.put("donorUid", donorUid);
        data.put("centerId", centerId);
        data.put("date", date);
        data.put("timeSlot", timeSlot);
        data.put("queueNo", queueNo);
        data.put("status", "PENDING");
        data.put("timestamp", System.currentTimeMillis());

        repository.saveAppointment(data)
                .addOnSuccessListener(docRef -> {
                    confirmedAppointmentId.setValue(docRef.getId());
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> isLoading.setValue(false));
    }

    public LiveData<List<DocumentSnapshot>> getCenters() {
        return centers;
    }

    public LiveData<DocumentSnapshot> getSelectedCenter() {
        return selectedCenter;
    }

    public LiveData<DocumentSnapshot> getDonorProfile() {
        return donorProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getNextQueueNumber() {
        return nextQueueNumber;
    }

    public LiveData<String> getBookingStatus() {
        return bookingStatus;
    }

    public LiveData<String> getConfirmedAppointmentId() {
        return confirmedAppointmentId;
    }

    public LiveData<Boolean> getIsEligible() {
        return isEligible;
    }

    public LiveData<String> getLockReason() {
        return lockReason;
    }
}