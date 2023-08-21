package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.stp.stay_alert.R;
import com.stp.stay_alert.databinding.ActivityForgotPasswordBinding;
import com.stp.stay_alert.databinding.ActivityMainBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        init();
    }
    private void init(){
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email;
                email = binding.editTextTextEmailAddress.getText().toString();
                ProgressDialog progressDialog
                        = new ProgressDialog(ForgotPasswordActivity.this);
                progressDialog.setMessage("Sending password recovery instructions to "+ email);
                progressDialog.show();

                if(!email.equals("")){
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ForgotPasswordActivity.this, "Email send success", Toast.LENGTH_SHORT).show();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                    builder.setMessage("Please check your email and follow the instructions provided.");
                                    builder.setTitle("Reset password complete");
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Got it", (DialogInterface.OnClickListener) (dialog, which) -> {
                                        finish();
                                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    });
                                    builder.show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Failed to send due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }else{
                    Toast.makeText(ForgotPasswordActivity.this, "Email Address is required!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}