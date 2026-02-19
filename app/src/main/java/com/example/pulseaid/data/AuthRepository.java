package com.example.pulseaid.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Interface for callbacks
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        fetchUserDetails(uid, callback);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    private void fetchUserDetails(String uid, AuthCallback callback) {
        // Assuming your users are stored in a "users" collection with doc ID = uid
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Convert Firestore document to User object
                            User user = document.toObject(User.class);
                            callback.onSuccess(user);
                        } else {
                            callback.onError("User record not found in database.");
                        }
                    } else {
                        callback.onError("Failed to fetch user data: " + task.getException().getMessage());
                    }
                });
    }
}