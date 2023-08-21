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
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.R;
import com.stp.stay_alert.databinding.ActivityRegAdminBinding;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegAdminActivity extends AppCompatActivity {
    private ActivityRegAdminBinding binding;
    private PreferenceManager preferenceManager;
    private String u_type;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    showToast("Invalid request");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showToast("The SMS quota for the project has been exceeded");
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                loading(false);
                Intent intent = new Intent(RegAdminActivity.this, OTPVerification.class);
                intent.putExtra("OTP", mVerificationId);
                intent.putExtra("resendToken", mResendToken);
                intent.putExtra("phoneNumber",phoneNumber);
                intent.putExtra("AUTH_TYPE", "BYADMIN");
                intent.putExtra(Constants.KEY_FULL_NAME,  binding.addUsername.getText().toString());
                intent.putExtra(Constants.KEY_ADDRESS,  binding.addAddress.getText().toString());
                intent.putExtra(Constants.KEY_USER_TYPE, u_type); // 1 = superuser 2 = common user | 3 = admin
//                intent.putExtra(Constants.KEY_IMAGE, encodedImage);
                startActivity(intent);
            }
        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            showToast("Authenticate Successfully");
                            FirebaseUser auth_user = task.getResult().getUser();
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            HashMap<String, Object> user = new HashMap<>();
                            user.put(Constants.KEY_FULL_NAME, binding.addUsername.getText().toString());
                            user.put(Constants.KEY_ADDRESS, binding.addAddress.getText().toString());
                            user.put(Constants.KEY_CONTACT, binding.addContact.getText().toString());
                            user.put(Constants.KEY_USER_TYPE, u_type); // 1 = superuser 2 = common user | 3 = admin
//                            user.put(Constants.KEY_IMAGE, encodedImage);
                            user.put(Constants.KEY_AVAILABILITY, 0);
                            user.put(Constants.KEY_TIMESTAMP, new Date());
                            database.collection(Constants.KEY_COLLECTION_USERS).document(auth_user.getUid()).set(user).addOnSuccessListener(documentReference -> {
                                loading(false);
                                preferenceManager.putString(Constants.KEY_USER_ID, auth_user.getUid());
//                                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }).addOnFailureListener(exception -> {
                                loading(false);
                                showToast(exception.getMessage());
                            });
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.d("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
    private void init(){
        binding.imageButton2.setOnClickListener(v -> onBackPressed());
        binding.checkBoxAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.checkBoxCommon.setChecked(false);
                binding.checkBoxResponder.setChecked(false);
                u_type = "1";
            }
        });
        binding.checkBoxCommon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.checkBoxAdmin.setChecked(false);
                binding.checkBoxResponder.setChecked(false);
                u_type = "2";
            }
        });
        binding.checkBoxResponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.checkBoxAdmin.setChecked(false);
                binding.checkBoxCommon.setChecked(false);
                u_type = "3";
            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading(true);
                if(isValidSignUpDetails()){
                    phoneNumber = binding.addContact.getText().toString().trim();
                    if(!phoneNumber.isEmpty()){
                        if(phoneNumber.length() == 11){
                            phoneNumber = "+63"+phoneNumber.substring(1);
                            registerUserV2(phoneNumber);
                        }
                    }else{
                        loading(false);
                        showToast("Number must not be empty!");
                    }
                }else{
                    loading(false);
                    showToast("Please complete the form.");
                }
            }
        });
    }
    private void registerUserV2(String number){
        loading(true);
        FirebaseAuth.getInstance().signOut();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
//    private void registerUser() {
//        loading(true);
//        FirebaseAuth.getInstance().signOut();
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        auth.createUserWithEmailAndPassword(binding.addEmail.getText().toString(),binding.addPassword.getText().toString()).addOnCompleteListener(RegAdminActivity.this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if(task.isSuccessful()){
//                    Objects.requireNonNull(auth.getCurrentUser()).reload();
//                    FirebaseUser new_user = auth.getCurrentUser();
//                    if(new_user != null){
//                        new_user.sendEmailVerification()
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Log.d("log", "Email sent.");
//                                            showToast("Email sent.");
//                                        }
//                                    }
//                                });
//
//                        FirebaseFirestore database = FirebaseFirestore.getInstance();
//                        HashMap<String, Object> user = new HashMap<>();
////                        user.put(Constants.KEY_EMAIL, binding.addEmail.getText().toString());
//                        user.put(Constants.KEY_NAME, binding.addUsername.getText().toString());
//                        user.put(Constants.KEY_ADDRESS, binding.addAddress.getText().toString());
//                        user.put(Constants.KEY_CONTACT, binding.addContact.getText().toString());
//                        user.put(Constants.KEY_USER_TYPE, u_type); // 1 = superuser 2 = common user | 3 = admin
//                        user.put(Constants.KEY_IMAGE, "");
//                        user.put(Constants.KEY_TIMESTAMP, new Date());
//                        database.collection(Constants.KEY_COLLECTION_USERS).document(new_user.getUid()).set(user).addOnSuccessListener(documentReference -> {
//                            loading(false);
//                            preferenceManager.putString(Constants.KEY_USER_ID, new_user.getUid());
//                            preferenceManager.putString(Constants.KEY_NAME, binding.addUsername.getText().toString());
//                            preferenceManager.putString(Constants.KEY_IMAGE, "");
//                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                        }).addOnFailureListener(exception -> {
//                            loading(false);
//                            showToast(exception.getMessage());
//                        });
//                    }else{
//                        showToast("Something went wrong");
//                        loading(false);
//                    }
//                }else{
//                    showToast(Objects.requireNonNull(task.getException()).getMessage());
//                    loading(false);
//                }
//            }
//        });
//    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.submitBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
            binding.submitBtn.setVisibility(View.VISIBLE);
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignUpDetails() {
        if (binding.addUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter Username");
            return false;
        } else if (binding.addAddress.getText().toString().trim().isEmpty()) {
            showToast("Enter Address");
            return false;
        }else if (binding.addContact.length() != 11) {
            showToast("Number should  be 11 digits");
            return false;
        }else if(!Patterns.PHONE.matcher(binding.addContact.getText().toString()).matches()) {
            showToast("Number exist!");
            return false;
        }else{
            return true;
        }
    }

}