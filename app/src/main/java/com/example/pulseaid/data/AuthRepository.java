package com.example.pulseaid.data;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class AuthRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Log.d("PULSEAID_DEBUG", "Login UID: " + uid);
                        fetchUserDetails(uid, callback);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void registerUser(User user, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        user.setUid(uid);

                        db.collection("Users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    private void fetchUserDetails(String uid, AuthCallback callback) {
        // Force Firebase to fetch from the server, ignoring the offline cache
        db.collection("Users").document(uid)
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            User user = document.toObject(User.class);

                            if (user != null) {
                                callback.onSuccess(user);
                            } else {
                                callback.onError("Failed to map user data.");
                            }
                        } else {
                            callback.onError("User record not found in database.");
                        }
                    } else {
                        callback.onError("Failed to fetch user data: " + task.getException().getMessage());
                    }
                });
    }
}