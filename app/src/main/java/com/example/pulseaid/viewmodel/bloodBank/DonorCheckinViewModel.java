package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.bloodBank.DonorCheckInRepository;
import com.google.firebase.firestore.DocumentSnapshot;

public class DonorCheckinViewModel extends ViewModel {
    private DonorCheckInRepository repository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<DocumentSnapshot> appointmentData = new MutableLiveData<>();
    private MutableLiveData<DocumentSnapshot> donorData = new MutableLiveData<>();
    private MutableLiveData<Boolean> transactionSuccess = new MutableLiveData<>();

    public DonorCheckinViewModel() {
        repository = new DonorCheckInRepository();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<DocumentSnapshot> getAppointmentData() { return appointmentData; }
    public LiveData<DocumentSnapshot> getDonorData() { return donorData; }
    public LiveData<Boolean> getTransactionSuccess() { return transactionSuccess; }

    public void verifyAppointmentQR(String appointmentId, String currentCenterId) {
        isLoading.setValue(true);
        repository.getAppointmentWithDonor(appointmentId, new DonorCheckInRepository.AppointmentDetailsCallback() {
            @Override
            public void onSuccess(DocumentSnapshot appointmentDoc, DocumentSnapshot donorDoc) {
                isLoading.setValue(false);
                String status = appointmentDoc.getString("status");
                String centerId = appointmentDoc.getString("centerId");

                if ("COMPLETED".equals(status)) {
                    errorMessage.setValue("Already Completed: This appointment has been processed.");
                    return;
                }

                if (!"CONFIRMED".equals(status)) {
                    errorMessage.setValue("Error: Appointment is still PENDING or REJECTED.");
                    return;
                }

                if (centerId == null || !centerId.equals(currentCenterId)) {
                    errorMessage.setValue("Invalid Center: This appointment belongs to another branch.");
                    return;
                }

                appointmentData.setValue(appointmentDoc);
                donorData.setValue(donorDoc);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void completeDonation(String appointmentId, String donorUid, String bloodBankId, String bloodType, int units) {
        isLoading.setValue(true);
        repository.completeDonationTransaction(appointmentId, donorUid, bloodBankId, bloodType, units, new DonorCheckInRepository.TransactionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                transactionSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}