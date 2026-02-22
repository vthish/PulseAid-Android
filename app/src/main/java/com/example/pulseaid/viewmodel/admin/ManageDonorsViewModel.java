package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.Donor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageDonorsViewModel extends ViewModel {

    private MutableLiveData<List<Donor>> donorList = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Donor>> getDonors() {
        loadDonors();
        return donorList;
    }

    private void loadDonors() {

        db.collection("Users")
                .whereEqualTo("role", "Donor")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        List<Donor> tempRef = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Donor donor = doc.toObject(Donor.class);
                            donor.setDonorId(doc.getId());
                            tempRef.add(donor);
                        }
                        donorList.setValue(tempRef);
                    }
                });
    }

    // Method to delete a donor
    public void deleteDonor(String donorId) {
        if (donorId != null) {

            db.collection("Users").document(donorId).delete();
        }
    }
}