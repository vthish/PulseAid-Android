package com.example.pulseaid.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.AuthRepository;
import com.example.pulseaid.data.User;

public class LoginViewModel extends ViewModel {

    private AuthRepository authRepository;
    private MutableLiveData<User> loginSuccessData = new MutableLiveData<>();
    private MutableLiveData<String> loginErrorData = new MutableLiveData<>();
    // LiveData for loading state
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<User> getLoginSuccessData() {
        return loginSuccessData;
    }

    public LiveData<String> getLoginErrorData() {
        return loginErrorData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void login(String email, String password) {
        isLoading.setValue(true);

        authRepository.loginUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                loginSuccessData.setValue(user);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                loginErrorData.setValue(message);
            }
        });
    }
}