package com.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.bloodBank.StockMonitorRepository;

import java.util.Map;

public class StockMonitorViewModel extends ViewModel {

    private StockMonitorRepository repository;

    private MutableLiveData<Map<String, String>> stockData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public StockMonitorViewModel() {
        repository = new StockMonitorRepository();
        // Automatically fetch data when ViewModel is created
        loadStockData();
    }

    public LiveData<Map<String, String>> getStockData() { return stockData; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadStockData() {
        isLoading.setValue(true);
        repository.fetchBloodStock(new StockMonitorRepository.StockCallback() {
            @Override
            public void onSuccess(Map<String, String> data) {
                isLoading.setValue(false);
                stockData.setValue(data);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}