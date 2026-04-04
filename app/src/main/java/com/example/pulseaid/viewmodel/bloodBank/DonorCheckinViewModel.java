package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pulseaid.data.bloodBank.DonorCheckInRepository;
import com.google.firebase.firestore.DocumentSnapshot;

public class DonorCheckinViewModel extends ViewModel {
    private final DonorCheckInRepository repository = new DonorCheckInRepository();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> transactionSuccess = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> appointmentData = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> donorData = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getTransactionSuccess() { return transactionSuccess; }
    public LiveData<DocumentSnapshot> getAppointmentData() { return appointmentData; }
    public LiveData<DocumentSnapshot> getDonorData() { return donorData; }

    public void verifyAppointmentQR(String appointmentId, String currentCenterId) {
        repository.getAppointmentWithDonor(appointmentId, new DonorCheckInRepository.AppointmentDetailsCallback() {
            @Override
            public void onSuccess(DocumentSnapshot appointmentDoc, DocumentSnapshot donorDoc) {
                String status = appointmentDoc.getString("status");
                if ("COMPLETED".equals(status) || "REJECTED".equals(status)) {
                    errorMessage.setValue("Error: Appointment is already " + status);
                    return;
                }
                donorData.setValue(donorDoc);
                appointmentData.setValue(appointmentDoc);
            }
            @Override
            public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void completeDonation(String appId, String donorId, String bankId, String type, int units) {
        repository.completeDonationTransaction(appId, donorId, bankId, type, units, new DonorCheckInRepository.TransactionCallback() {
            @Override public void onSuccess() { transactionSuccess.setValue(true); }
            @Override public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void rejectAppointment(String appId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            errorMessage.setValue("Please enter a reason for rejection.");
            return;
        }
        repository.rejectAppointmentTransaction(appId, reason, new DonorCheckInRepository.TransactionCallback() {
            @Override public void onSuccess() { transactionSuccess.setValue(true); }
            @Override public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void clearErrorMessage() { errorMessage.setValue(null); }
    public void resetNavigationData() { appointmentData.setValue(null); donorData.setValue(null); }
}