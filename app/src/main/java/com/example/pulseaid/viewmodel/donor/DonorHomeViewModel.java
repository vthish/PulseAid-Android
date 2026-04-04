package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.donor.DonorRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DonorHomeViewModel extends ViewModel {

    private final DonorRepository repository;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Integer> donationCount = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> livesSaved = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> remainingDays = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isEligible = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> hasPendingAppointment = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasUnreadUrgentAlerts = new MutableLiveData<>(false);
    private final MutableLiveData<BookingDetails> upcomingBookingDetails = new MutableLiveData<>();

    public DonorHomeViewModel() {
        this.repository = new DonorRepository();
    }

    public void loadDashboardData(String userId, Set<String> seenAlertIds) {
        repository.getProfile(userId, new DonorRepository.OnProfileCallback() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc.exists()) {
                    userName.setValue(doc.getString("name"));

                    Long count = doc.getLong("donationCount");
                    int donationTimes = count != null ? count.intValue() : 0;
                    donationCount.setValue(donationTimes);
                    livesSaved.setValue(donationTimes * 3);

                    Long lastDate = doc.getLong("lastDonationDate");
                    calculateEligibility(lastDate);

                    String donorBloodGroup = doc.getString("bloodGroup");
                    loadUnreadUrgentAlerts(donorBloodGroup, seenAlertIds);
                } else {
                    hasUnreadUrgentAlerts.setValue(false);
                    hasPendingAppointment.setValue(false);
                    upcomingBookingDetails.setValue(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                hasUnreadUrgentAlerts.setValue(false);
            }
        });

        repository.getAppointmentsForDonor(userId, new DonorRepository.OnAppointmentsCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                boolean foundPending = false;
                DocumentSnapshot latestPending = null;
                long latestTimestamp = 0;

                if (!querySnapshot.isEmpty()) {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");

                        if ("PENDING".equalsIgnoreCase(status)) {
                            Long timestamp = doc.getLong("timestamp");
                            long currentTimestamp = timestamp != null ? timestamp : 0;

                            if (latestPending == null || currentTimestamp > latestTimestamp) {
                                latestPending = doc;
                                latestTimestamp = currentTimestamp;
                                foundPending = true;
                            }
                        }
                    }
                }

                hasPendingAppointment.setValue(foundPending);

                if (latestPending != null) {
                    fetchCenterDetailsForBooking(latestPending);
                } else {
                    upcomingBookingDetails.setValue(null);
                }
            }

            @Override
            public void onFailure(Exception e) {
                hasPendingAppointment.setValue(false);
                upcomingBookingDetails.setValue(null);
            }
        });
    }

    private void loadUnreadUrgentAlerts(String donorBloodGroup, Set<String> seenAlertIds) {
        if (donorBloodGroup == null || donorBloodGroup.trim().isEmpty()) {
            hasUnreadUrgentAlerts.setValue(false);
            return;
        }

        repository.getResolvedEmergencyRequests(new DonorRepository.OnEmergencyRequestsCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                boolean hasUnread = false;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String alertId = doc.getId();
                    String requestBloodGroup = doc.getString("bloodGroup");

                    if (requestBloodGroup == null || requestBloodGroup.trim().isEmpty()) {
                        continue;
                    }

                    if (isDonorCompatibleForRequest(donorBloodGroup, requestBloodGroup)) {
                        if (seenAlertIds == null || !seenAlertIds.contains(alertId)) {
                            hasUnread = true;
                            break;
                        }
                    }
                }

                hasUnreadUrgentAlerts.setValue(hasUnread);
            }

            @Override
            public void onFailure(Exception e) {
                hasUnreadUrgentAlerts.setValue(false);
            }
        });
    }

    private void fetchCenterDetailsForBooking(DocumentSnapshot appointmentDoc) {
        String centerId = appointmentDoc.getString("centerId");

        if (centerId != null && !centerId.trim().isEmpty()) {
            repository.getProfile(centerId, new DonorRepository.OnProfileCallback() {
                @Override
                public void onSuccess(DocumentSnapshot centerDoc) {
                    String name = "Blood Center";
                    if (centerDoc.exists() && centerDoc.getString("name") != null) {
                        name = centerDoc.getString("name");
                    }

                    String date = appointmentDoc.getString("date");
                    String slot = appointmentDoc.getString("timeSlot");

                    if (date == null) {
                        date = "-";
                    }

                    if (slot == null) {
                        slot = "-";
                    }

                    upcomingBookingDetails.setValue(new BookingDetails(name, date + " | " + slot));
                }

                @Override
                public void onFailure(Exception e) {
                    String date = appointmentDoc.getString("date");
                    String slot = appointmentDoc.getString("timeSlot");

                    if (date == null) {
                        date = "-";
                    }

                    if (slot == null) {
                        slot = "-";
                    }

                    upcomingBookingDetails.setValue(new BookingDetails("Blood Center", date + " | " + slot));
                }
            });
        } else {
            String date = appointmentDoc.getString("date");
            String slot = appointmentDoc.getString("timeSlot");

            if (date == null) {
                date = "-";
            }

            if (slot == null) {
                slot = "-";
            }

            upcomingBookingDetails.setValue(new BookingDetails("Blood Center", date + " | " + slot));
        }
    }

    private void calculateEligibility(Long lastDateMs) {
        if (lastDateMs == null || lastDateMs == 0) {
            remainingDays.setValue(0);
            isEligible.setValue(true);
            return;
        }

        long diffInMs = System.currentTimeMillis() - lastDateMs;
        long daysPassed = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

        int remDays = (int) (90 - daysPassed);
        int safeRemainingDays = Math.max(remDays, 0);

        remainingDays.setValue(safeRemainingDays);
        isEligible.setValue(safeRemainingDays <= 0);
    }

    private boolean isDonorCompatibleForRequest(String donorGroup, String requestedGroup) {
        donorGroup = donorGroup.trim().toUpperCase();
        requestedGroup = requestedGroup.trim().toUpperCase();

        switch (requestedGroup) {
            case "O-":
                return donorGroup.equals("O-");
            case "O+":
                return donorGroup.equals("O-") || donorGroup.equals("O+");
            case "A-":
                return donorGroup.equals("O-") || donorGroup.equals("A-");
            case "A+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("A-") || donorGroup.equals("A+");
            case "B-":
                return donorGroup.equals("O-") || donorGroup.equals("B-");
            case "B+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("B-") || donorGroup.equals("B+");
            case "AB-":
                return donorGroup.equals("O-") || donorGroup.equals("A-")
                        || donorGroup.equals("B-") || donorGroup.equals("AB-");
            case "AB+":
                return donorGroup.equals("O-") || donorGroup.equals("O+")
                        || donorGroup.equals("A-") || donorGroup.equals("A+")
                        || donorGroup.equals("B-") || donorGroup.equals("B+")
                        || donorGroup.equals("AB-") || donorGroup.equals("AB+");
            default:
                return false;
        }
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<Integer> getDonationCount() {
        return donationCount;
    }

    public LiveData<Integer> getLivesSaved() {
        return livesSaved;
    }

    public LiveData<Integer> getRemainingDays() {
        return remainingDays;
    }

    public LiveData<Boolean> getIsEligible() {
        return isEligible;
    }

    public LiveData<Boolean> getHasPendingAppointment() {
        return hasPendingAppointment;
    }

    public LiveData<Boolean> getHasUnreadUrgentAlerts() {
        return hasUnreadUrgentAlerts;
    }

    public LiveData<BookingDetails> getUpcomingBookingDetails() {
        return upcomingBookingDetails;
    }

    public static class BookingDetails {
        private final String centerName;
        private final String dateTime;

        public BookingDetails(String name, String time) {
            this.centerName = name;
            this.dateTime = time;
        }

        public String getCenterName() {
            return centerName;
        }

        public String getDateTime() {
            return dateTime;
        }
    }
}