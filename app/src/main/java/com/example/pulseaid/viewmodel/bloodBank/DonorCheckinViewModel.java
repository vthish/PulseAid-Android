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
    private MutableLiveData<DocumentSnapshot> validAppointment = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public DonorCheckinViewModel() {
        repository = new DonorCheckInRepository();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<DocumentSnapshot> getValidAppointment() { return validAppointment; }
    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }

    public void verifyAppointmentQR(String appointmentId, String currentCenterId) {
        isLoading.setValue(true);
        String cleanAppointmentId = appointmentId.trim();

        repository.getAppointment(cleanAppointmentId, new DonorCheckInRepository.AppointmentCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                isLoading.setValue(false);
                if (document.exists()) {
                    String dbCenterId = document.getString("centerId");

                    if (dbCenterId != null && dbCenterId.equals(currentCenterId)) {
                        validAppointment.setValue(document);
                    } else {
                        errorMessage.setValue("Do not scan! This appointment belongs to another center.");
                    }
                } else {
                    errorMessage.setValue("Invalid QR: Appointment not found in database.");
                }
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void updateStatus(String appointmentId, String status) {
        isLoading.setValue(true);
        repository.updateAppointmentStatus(appointmentId.trim(), status, new DonorCheckInRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                updateSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}