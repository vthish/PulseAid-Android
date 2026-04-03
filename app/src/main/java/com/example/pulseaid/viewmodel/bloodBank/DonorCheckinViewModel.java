package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pulseaid.data.bloodBank.DonorCheckInRepository;
import com.google.firebase.firestore.DocumentSnapshot;

public class DonorCheckinViewModel extends ViewModel {
    private final DonorCheckInRepository repository = new DonorCheckInRepository();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> appointmentData = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> donorData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> transactionSuccess = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<DocumentSnapshot> getAppointmentData() { return appointmentData; }
    public LiveData<DocumentSnapshot> getDonorData() { return donorData; }
    public LiveData<Boolean> getTransactionSuccess() { return transactionSuccess; }

    public void verifyAppointmentQR(String appointmentId, String currentCenterId) {
        try {
            repository.getAppointmentWithDonor(appointmentId, new DonorCheckInRepository.AppointmentDetailsCallback() {
                @Override
                public void onSuccess(DocumentSnapshot appointmentDoc, DocumentSnapshot donorDoc) {
                    try {
                        String status = appointmentDoc.getString("status");
                        String centerId = appointmentDoc.getString("centerId");

                        if ("COMPLETED".equals(status)) {
                            errorMessage.setValue("Error: This appointment is already COMPLETED.");
                            return;
                        }

                        if (!"CONFIRMED".equals(status) && !"PENDING".equals(status)) {
                            errorMessage.setValue("Error: Invalid Appointment Status (" + status + ")");
                            return;
                        }

                        // Strictly matching the appointment's centerId with the logged-in center's ID
                        if (centerId == null || !centerId.equals(currentCenterId)) {
                            errorMessage.setValue("Unauthorized: This appointment belongs to a different Blood Bank Branch!");
                            return;
                        }

                        donorData.setValue(donorDoc);
                        appointmentData.setValue(appointmentDoc);

                    } catch (Exception e) {
                        errorMessage.setValue("Data Processing Error: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(String error) {
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("Verification Error: " + e.getMessage());
        }
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void resetNavigationData() {
        appointmentData.setValue(null);
        donorData.setValue(null);
    }

    public void completeDonation(String appId, String donorId, String bankId, String type, int units) {
        try {
            repository.completeDonationTransaction(appId, donorId, bankId, type, units, new DonorCheckInRepository.TransactionCallback() {
                @Override
                public void onSuccess() {
                    transactionSuccess.setValue(true);
                }
                @Override
                public void onFailure(String error) {
                    errorMessage.setValue(error);
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("Transaction Error: " + e.getMessage());
        }
    }
}