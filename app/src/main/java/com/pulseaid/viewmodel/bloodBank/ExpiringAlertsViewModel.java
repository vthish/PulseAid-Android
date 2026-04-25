package com.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.bloodBank.ExpiringAlertsRepository;
import com.pulseaid.data.bloodBank.ExpiringAlertsRepository.AlertItem;

import java.util.List;

public class ExpiringAlertsViewModel extends ViewModel {

    private final ExpiringAlertsRepository repository;
    private final MutableLiveData<List<AlertItem>> alertList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ExpiringAlertsViewModel() {
        repository = new ExpiringAlertsRepository();
        loadAlerts();
    }

    public LiveData<List<AlertItem>> getAlertList() { return alertList; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadAlerts() {
        isLoading.setValue(true);
        repository.fetchExpiringAlerts(new ExpiringAlertsRepository.AlertsCallback() {
            @Override
            public void onSuccess(List<AlertItem> alerts) {
                isLoading.setValue(false);
                alertList.setValue(alerts);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}