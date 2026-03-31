package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.bloodBank.DonorCheckInRepository;

public class DonorCheckInViewModel extends ViewModel {

    private final DonorCheckInRepository repository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> verificationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEligible = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DonorCheckInViewModel() {
        this.repository = new DonorCheckInRepository();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getVerificationMessage() { return verificationMessage; }
    public LiveData<Boolean> getIsEligible() { return isEligible; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void verifyDonor(String donorId) {
        if (donorId == null || donorId.trim().isEmpty()) {
            errorMessage.setValue("Invalid QR Code or Donor ID.");
            return;
        }

        isLoading.setValue(true);
        repository.verifyDonorEligibility(donorId, new DonorCheckInRepository.EligibilityCallback() {
            @Override
            public void onResult(boolean eligibleStatus, String message) {
                isLoading.setValue(false);
                isEligible.setValue(eligibleStatus);
                verificationMessage.setValue(message);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}