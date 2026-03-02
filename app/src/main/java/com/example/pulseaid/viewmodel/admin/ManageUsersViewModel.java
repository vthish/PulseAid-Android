package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.Donor;
import com.example.pulseaid.data.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<User>> getUsersByRole(String role) {
        MutableLiveData<List<User>> userList = new MutableLiveData<>();

        db.collection("Users")
                .whereEqualTo("role", role)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        List<User> tempRef = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            user.setUid(doc.getId()); // Using setUid() from User.java
                            tempRef.add(user);
                        }
                        userList.setValue(tempRef);
                    }
                });
        return userList;
    }

    public LiveData<List<Donor>> getDonors() {
        MutableLiveData<List<Donor>> donorList = new MutableLiveData<>();
        db.collection("Users")
                .whereEqualTo("role", "Donor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Donor> donors = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Donor donor = doc.toObject(Donor.class);
                        donors.add(donor);
                    }
                    donorList.setValue(donors);
                })
                .addOnFailureListener(e -> donorList.setValue(null));
        return donorList;
    }

    public void deleteUser(String uid) {
        if (uid != null) {
            db.collection("Users").document(uid).delete();
        }
    }
}