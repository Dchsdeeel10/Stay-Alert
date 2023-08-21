package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stp.stay_alert.databinding.ActivityReAunthenticateBinding;

public class ReAunthenticateActivity extends AppCompatActivity {
    private ActivityReAunthenticateBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReAunthenticateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }
    private void init(){
        binding.imageButton3.setOnClickListener(v -> onBackPressed());
        binding.reaBtn.setOnClickListener(v -> {
            String email = binding.reaEmail.getText().toString();
            String password = binding.reaPassword.getText().toString();
            if(isValidSignInDetails()){
                reAuthenticate(email, password);
            }
        });
    }
    private void reAuthenticate(String email, String password){
        loading(false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);
        if(user != null){
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("rea", "User re-authenticated.");
                            loading(true);
                            showToast("User re-authenticated successfully.");
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.putExtra("re-authenticated", true);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("Something wrong! "+ e.getMessage());
                        }
                    });
        }else{
            showToast("User cannot found!");
        }

    }
    private Boolean isValidSignInDetails(){
        if(binding.reaEmail.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.reaEmail.getText().toString()).matches()){
            showToast("Enter valid Email");
            return false;
        }else if (binding.reaPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }else{
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.reaBtn.setVisibility(View.INVISIBLE);
            binding.progressBar2.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar2.setVisibility(View.INVISIBLE);
            binding.reaBtn.setVisibility(View.VISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}