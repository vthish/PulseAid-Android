package com.example.pulseaid.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.admin.BloodBank;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageBloodBanksViewModel extends ViewModel {

    private MutableLiveData<List<BloodBank>> bankList = new MutableLiveData<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<BloodBank>> getBloodBanks() {
        loadBloodBanks();
        return bankList;
    }

    private void loadBloodBanks() {

        db.collection("Users")
                .whereEqualTo("role", "Blood Staff")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        List<BloodBank> tempRef = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            BloodBank bank = doc.toObject(BloodBank.class);
                            bank.setId(doc.getId());
                            tempRef.add(bank);
                        }
                        bankList.setValue(tempRef);
                    }
                });
    }

    public void deleteBloodBank(String id) {
        if (id != null) {
            db.collection("Users").document(id).delete();
        }
    }
}