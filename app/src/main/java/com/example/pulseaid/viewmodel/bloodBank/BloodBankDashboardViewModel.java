package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.bloodBank.BloodBankDashboardRepository;

public class BloodBankDashboardViewModel extends ViewModel {

    private BloodBankDashboardRepository repository;

    private MutableLiveData<Integer> totalStock = new MutableLiveData<>();
    private MutableLiveData<Integer> pendingOrders = new MutableLiveData<>();
    private MutableLiveData<Integer> todayAppointments = new MutableLiveData<>();
    private MutableLiveData<Integer> expireAlerts = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BloodBankDashboardViewModel() {
        repository = new BloodBankDashboardRepository();
        loadDashboardData();
    }

    public LiveData<Integer> getTotalStock() { return totalStock; }
    public LiveData<Integer> getPendingOrders() { return pendingOrders; }
    public LiveData<Integer> getTodayAppointments() { return todayAppointments; }
    public LiveData<Integer> getExpireAlerts() { return expireAlerts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadDashboardData() {
        isLoading.setValue(true);
        repository.fetchDashboardStats(new BloodBankDashboardRepository.DashboardStatsCallback() {
            @Override
            public void onSuccess(int stock, int pending, int appointments, int alerts) {
                isLoading.setValue(false);
                totalStock.setValue(stock);
                pendingOrders.setValue(pending);
                todayAppointments.setValue(appointments);
                expireAlerts.setValue(alerts);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}