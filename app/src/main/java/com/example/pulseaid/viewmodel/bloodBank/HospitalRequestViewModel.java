package com.example.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pulseaid.data.bloodBank.HospitalRequestRepository;
import com.example.pulseaid.data.bloodBank.HospitalRequestRepository.HospitalRequest;
import java.util.ArrayList;
import java.util.List;

public class HospitalRequestViewModel extends ViewModel {
    private final HospitalRequestRepository repository;
    private final MutableLiveData<List<HospitalRequest>> originalList = new MutableLiveData<>();
    private final MutableLiveData<List<HospitalRequest>> filteredList = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    private String currentSearchQuery = "";
    private String currentUrgencyFilter = "All";

    public HospitalRequestViewModel() {
        this.repository = new HospitalRequestRepository();
    }

    public LiveData<List<HospitalRequest>> getRequestList() { return filteredList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getActionMessage() { return actionMessage; }

    public void loadRequests() {
        repository.fetchHospitalRequests(new HospitalRequestRepository.RequestCallback() {
            @Override
            public void onSuccess(List<HospitalRequest> requests) {
                originalList.setValue(requests);
                applyFilters();
            }
            @Override
            public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }

    public void setSearchQuery(String query) {
        this.currentSearchQuery = query.toLowerCase();
        applyFilters();
    }

    public void setUrgencyFilter(String urgency) {
        this.currentUrgencyFilter = urgency;
        applyFilters();
    }

    private void applyFilters() {
        List<HospitalRequest> fullList = originalList.getValue();
        if (fullList == null) return;

        List<HospitalRequest> results = new ArrayList<>();
        for (HospitalRequest req : fullList) {
            boolean matchesSearch = req.name.toLowerCase().contains(currentSearchQuery);
            boolean matchesUrgency = currentUrgencyFilter.equals("All") || req.urgency.equalsIgnoreCase(currentUrgencyFilter);

            if (matchesSearch && matchesUrgency) {
                results.add(req);
            }
        }
        filteredList.setValue(results);
    }

    public void confirmBlood(HospitalRequest request) {
        repository.confirmBloodUnits(request.id, request.type, request.qty, new HospitalRequestRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                actionMessage.setValue("Blood Confirmed!");
                loadRequests();
            }
            @Override
            public void onFailure(String error) { errorMessage.setValue(error); }
        });
    }
}