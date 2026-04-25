package com.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.bloodBank.BloodStockDetailsRepository;
import com.pulseaid.data.bloodBank.BloodStockDetailsRepository.BloodPacket;

import java.util.List;
import java.util.Map;

public class BloodStockDetailsViewModel extends ViewModel {

    private final BloodStockDetailsRepository repository;

    private final MutableLiveData<Map<String, String>> compatibilityInfo = new MutableLiveData<>();
    private final MutableLiveData<List<BloodPacket>> packetList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public BloodStockDetailsViewModel() {
        this.repository = new BloodStockDetailsRepository();
    }

    public LiveData<Map<String, String>> getCompatibilityInfo() { return compatibilityInfo; }
    public LiveData<List<BloodPacket>> getPacketList() { return packetList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadDetails(String bloodGroup) {
        isLoading.setValue(true);
        repository.fetchDetails(bloodGroup, new BloodStockDetailsRepository.DetailsCallback() {
            @Override
            public void onSuccess(Map<String, String> info, List<BloodPacket> packets) {
                isLoading.setValue(false);
                compatibilityInfo.setValue(info);
                packetList.setValue(packets);
            }

            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}