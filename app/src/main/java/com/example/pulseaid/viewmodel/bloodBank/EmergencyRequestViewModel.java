package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.repository.EmergencyRequestRepository;

public class EmergencyRequestViewModel extends ViewModel {

    private final EmergencyRequestRepository repository;

    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public EmergencyRequestViewModel() {
        repository = new EmergencyRequestRepository();
    }

    public LiveData<Boolean> getIsSuccess() {
        return isSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void submitEmergencyRequest(String bloodType, String qtyStr, String reason) {
        if (bloodType == null || bloodType.trim().isEmpty() || qtyStr == null || qtyStr.trim().isEmpty()) {
            errorMessage.setValue("Please fill required fields");
            return;
        }

        int units;
        try {
            units = Integer.parseInt(qtyStr.trim());
        } catch (NumberFormatException e) {
            errorMessage.setValue("Invalid unit quantity");
            return;
        }

        isLoading.setValue(true);

        repository.sendEmergencyRequest(bloodType, units, reason, new EmergencyRequestRepository.RequestCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                isSuccess.setValue(true);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}