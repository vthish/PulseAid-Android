package com.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.donor.DonorRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class DonorAccountViewModel extends ViewModel {

    private final DonorRepository repository;
    private final MutableLiveData<DocumentSnapshot> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private ListenerRegistration profileListener;

    public DonorAccountViewModel() {
        this.repository = new DonorRepository();
    }

    public void startProfileListener(String userId) {
        stopProfileListener();
        isLoading.setValue(true);

        profileListener = repository.listenToProfile(userId, new DonorRepository.OnProfileListenerCallback() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                isLoading.setValue(false);
                userProfile.setValue(document);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                errorMessage.setValue(e.getMessage());
            }
        });
    }

    public void stopProfileListener() {
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }

    public void updateProfile(String userId, Map<String, Object> updates) {
        isLoading.setValue(true);
        repository.updateProfile(userId, updates)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    updateSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(e.getMessage());
                    updateSuccess.setValue(false);
                });
    }

    public LiveData<DocumentSnapshot> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void resetUpdateStatus() {
        updateSuccess.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopProfileListener();
    }
}