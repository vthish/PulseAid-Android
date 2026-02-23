package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.AuthRepository;
import com.example.pulseaid.data.User;


public class RegisterViewModel extends ViewModel {


    private AuthRepository authRepo;


    private MutableLiveData<Boolean> loadingStatus = new MutableLiveData<>();
    private MutableLiveData<String> errorStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> successStatus = new MutableLiveData<>();

    public RegisterViewModel() {

        authRepo = new AuthRepository();
    }


    public LiveData<Boolean> getLoadingStatus() { return loadingStatus; }
    public LiveData<String> getErrorMessage() { return errorStatus; }
    public LiveData<Boolean> getSuccessStatus() { return successStatus; }


    public void registerDonor(String name, String email, String password) {

        loadingStatus.setValue(true);


        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setRole("Donor");
        newUser.setBloodType("");


        authRepo.registerUser(newUser, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                loadingStatus.setValue(false);
                successStatus.setValue(true);
            }

            @Override
            public void onError(String message) {
                loadingStatus.setValue(false);
                errorStatus.setValue(message);
            }
        });
    }
}