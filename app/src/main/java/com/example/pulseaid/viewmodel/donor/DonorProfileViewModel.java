package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DonorProfileViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private MutableLiveData<Boolean> isSaveSuccess = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getSaveStatus() { return isSaveSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    public void updateDonorProfile(String name, String nic, String phone, String gender,
                                   String blood, String weight, String dob, String address , int age) {

        String uid = mAuth.getCurrentUser().getUid();


        Map<String, Object> donorData = new HashMap<>();
        donorData.put("name", name);
        donorData.put("nic", nic);
        donorData.put("phoneNumber", phone);
        donorData.put("gender", gender);
        donorData.put("bloodGroup", blood);
        donorData.put("weight", weight);
        donorData.put("dob", dob);
        donorData.put("address", address);
        donorData.put("age", age);
        donorData.put("isProfileComplete", true);


        db.collection("Users").document(uid)
                .update(donorData)
                .addOnSuccessListener(aVoid -> isSaveSuccess.setValue(true))
                .addOnFailureListener(e -> {
                    isSaveSuccess.setValue(false);
                    errorMessage.setValue(e.getMessage());
                });
    }
}