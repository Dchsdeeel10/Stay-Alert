package com.stp.stay_alert.adapater;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    PreferenceManager preferenceManager;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
//                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> user1 = new HashMap<>();
        user1.put(Constants.KEY_AVAILABILITY, 0);
        user1.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
        mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        HashMap<String, Object> user1 = new HashMap<>();
//        user1.put(Constants.KEY_AVAILABILITY, 1);
//        user1.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
//        mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);
    }
}
