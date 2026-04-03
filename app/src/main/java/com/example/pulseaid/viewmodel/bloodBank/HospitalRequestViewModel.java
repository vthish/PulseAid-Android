package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pulseaid.data.bloodBank.HospitalRequestRepository;
import com.example.pulseaid.data.bloodBank.HospitalRequestRepository.HospitalRequest;
import java.util.List;

public class HospitalRequestViewModel extends ViewModel {
    private final HospitalRequestRepository repository;
    private final MutableLiveData<List<HospitalRequest>> requestList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public HospitalRequestViewModel() {
        this.repository = new HospitalRequestRepository();
    }

    public LiveData<List<HospitalRequest>> getRequestList() { return requestList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getActionMessage() { return actionMessage; }

    public void loadRequests() {
        isLoading.setValue(true);
        repository.fetchHospitalRequests(new HospitalRequestRepository.RequestCallback() {
            @Override
            public void onSuccess(List<HospitalRequest> requests) {
                isLoading.setValue(false);
                requestList.setValue(requests);
            }
            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void confirmBlood(HospitalRequest request) {
        isLoading.setValue(true);
        repository.confirmBloodUnits(request.id, request.type, request.qty, new HospitalRequestRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                actionMessage.setValue("Blood Confirmed and Dispatched!");
                loadRequests();
            }
            @Override
            public void onFailure(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}