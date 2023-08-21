package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.databinding.ActivityChatProfileBinding;
import com.stp.stay_alert.utilities.Constants;

public class chatProfile extends AppCompatActivity {
    private ActivityChatProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageButton10.setOnClickListener(v -> onBackPressed());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Bundle extras = getIntent().getExtras();
        String id = extras.getString("receiverID");
        DocumentReference docRef = db.collection(Constants.KEY_COLLECTION_USERS).document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        binding.tvFullName.setText(document.getString(Constants.KEY_FULL_NAME));
                        binding.chatProfileName.setText(document.getString(Constants.KEY_FULL_NAME));
                        binding.profileTVAddress.setText(document.getString(Constants.KEY_ADDRESS));
                        binding.profileTVMobileNumber.setText(document.getString(Constants.KEY_CONTACT));
                        if(document.getString(Constants.KEY_IMAGE) != null){
                            byte[] bytes = Base64.decode(document.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            binding.ProfilePhoto.setImageBitmap(bitmap);
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

    }
}