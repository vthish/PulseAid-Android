package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.donor.DonorRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonorBookingViewModel extends ViewModel {

    private final DonorRepository repository;
    private final MutableLiveData<List<DocumentSnapshot>> centers = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> selectedCenter = new MutableLiveData<>();
    private final MutableLiveData<DocumentSnapshot> donorProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public DonorBookingViewModel() {
        this.repository = new DonorRepository();
    }

    public void fetchDonorProfile(String uid) {
        repository.getProfile(uid, new DonorRepository.OnProfileCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                donorProfile.setValue(document);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    public void onDistrictSelected(String district) {
        if (district.equals("Select District")) {
            centers.setValue(new ArrayList<>());
            return;
        }

        isLoading.setValue(true);

        repository.getBloodCentersByDistrict(district, new DonorRepository.OnCentersCallback() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                centers.setValue(querySnapshot.getDocuments());
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
            }
        });
    }

    public void onCenterSelected(int position) {
        if (centers.getValue() != null && position >= 0 && position < centers.getValue().size()) {
            selectedCenter.setValue(centers.getValue().get(position));
        }
    }

    public LiveData<List<DocumentSnapshot>> getCenters() { return centers; }
    public LiveData<DocumentSnapshot> getSelectedCenter() { return selectedCenter; }
    public LiveData<DocumentSnapshot> getDonorProfile() { return donorProfile; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}