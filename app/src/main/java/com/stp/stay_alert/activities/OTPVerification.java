package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.R;
import com.stp.stay_alert.databinding.ActivityOtpverificationBinding;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OTPVerification extends AppCompatActivity {
    private ActivityOtpverificationBinding binding;
    private String OTP;
    private PhoneAuthProvider.ForceResendingToken resentToken;
    private String phoneNumber;
    private FirebaseAuth auth;
    private FirebaseUser auth_user;
    private PreferenceManager preferenceManager;
    private String AUTH_TYPE;
    private DatabaseReference mDatabase;
    FirebaseFirestore database;
    private CountDownTimer resendTimer;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpverificationBinding.inflate((getLayoutInflater()));
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        OTP = getIntent().getStringExtra("OTP").toString();
        resentToken = getIntent().getParcelableExtra("mResendToken");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        AUTH_TYPE = getIntent().getStringExtra("AUTH_TYPE");
        database = FirebaseFirestore.getInstance();
        init();
        setListener();

        // set up timer for resend otp
        long duration = TimeUnit.MINUTES.toMillis(3);
        resendTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                String sDuration = String.format(Locale.ENGLISH, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l),
                        TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));

                binding.tvTimerOTP.setText(sDuration);
            }

            @Override
            public void onFinish() {
                binding.tvTimerOTP.setVisibility(View.GONE);
                binding.resendBtn.setVisibility(View.VISIBLE);
            }
        };
        resendTimer.start();
        auth = FirebaseAuth.getInstance();
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
                    loading(false);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showToast("SMS quota has been exceeded");
                    loading(false);
                }
                showToast("Something went wrong!");
                loading(false);
                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                showToast("OTP send Successfully");
                OTP = verificationId;
                resentToken = token;
                resendTimer.start();
                binding.resendBtn.setVisibility(View.GONE);
                binding.tvTimerOTP.setVisibility(View.VISIBLE);
            }
        };
    }
    private void resendVerificationCode(){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+63"+phoneNumber.substring(1))       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(resentToken)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.verifyOTPButton.setVisibility(View.GONE);
            binding.progressBar3.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar3.setVisibility(View.GONE);
            binding.verifyOTPButton.setVisibility(View.VISIBLE);
        }
    }
    private void setListener(){
        binding.resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Resending OTP...");
                resendVerificationCode();
            }
        });
        binding.verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.otpCode5.onEditorAction(EditorInfo.IME_ACTION_DONE);
                String inputOTP = (binding.otpCode.getText().toString() + binding.otpCode1.getText().toString() + binding.otpCode2.getText().toString() + binding.otpCode3.getText().toString() + binding.otpCode4.getText().toString() + binding.otpCode5.getText().toString());
                if(!inputOTP.isEmpty()){
                    if(inputOTP.length() == 6){
                        loading(true);
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(
                                OTP, inputOTP
                        );
                        signInWithPhoneAuthCredential(credential);
                    }else {
                        Toast.makeText(OTPVerification.this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show();
                        loading(false);
                    }
                }else{
                    Toast.makeText(OTPVerification.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                    loading(false);
                }
            }
        });
        binding.otpCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    binding.otpCode1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otpCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    binding.otpCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otpCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    binding.otpCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otpCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    binding.otpCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.otpCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){
                    binding.otpCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            showToast("Authenticate Successfully");
                            auth_user = task.getResult().getUser();
                            if(AUTH_TYPE.equals("LOGIN")){
                                DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_USERS).document(auth_user.getUid());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                preferenceManager.putString(Constants.KEY_USER_ID, auth_user.getUid());
                                                preferenceManager.putString(Constants.KEY_FULL_NAME, document.getString(Constants.KEY_FULL_NAME));
                                                preferenceManager.putString(Constants.KEY_IMAGE, document.getString(Constants.KEY_IMAGE));
                                                preferenceManager.putString(Constants.KEY_ADDRESS, document.getString(Constants.KEY_ADDRESS));
                                                preferenceManager.putString(Constants.KEY_CONTACT, document.getString(Constants.KEY_CONTACT));
                                                preferenceManager.putString(Constants.KEY_USER_TYPE, document.getString(Constants.KEY_USER_TYPE));
                                                preferenceManager.putString(Constants.KEY_FCM_TOKEN, document.getString(Constants.KEY_FCM_TOKEN));
                                                mDatabase = FirebaseDatabase.getInstance().getReference();
                                                HashMap<String, Object> user1 = new HashMap<>();
                                                user1.put(Constants.KEY_AVAILABILITY, 1);
                                                user1.put(Constants.KEY_FCM_TOKEN, document.getString(Constants.KEY_FCM_TOKEN));
                                                mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);
                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            } else {
                                                showToast("No user exist!");
                                                loading(false);
                                            }
                                        } else {
                                            showToast("get failed with " + task.getException());
                                            loading(false);
                                        }
                                    }
                                });
                            }else{
                                FirebaseFirestore database = FirebaseFirestore.getInstance();
                                HashMap<String, Object> user = new HashMap<>();
                                user.put(Constants.KEY_FULL_NAME, getIntent().getStringExtra(Constants.KEY_FULL_NAME));
                                user.put(Constants.KEY_ADDRESS, getIntent().getStringExtra(Constants.KEY_ADDRESS));
                                user.put(Constants.KEY_CONTACT, phoneNumber);
                                user.put(Constants.KEY_USER_TYPE, "2"); // 1 = superuser 2 = common user | 3 = admin
                                user.put(Constants.KEY_IMAGE, getIntent().getStringExtra(Constants.KEY_IMAGE));
                                user.put(Constants.KEY_AVAILABILITY, 0);
                                user.put(Constants.KEY_TIMESTAMP, new Date());
                                database.collection(Constants.KEY_COLLECTION_USERS).document(auth_user.getUid()).set(user).addOnSuccessListener(documentReference -> {
                                    binding.progressBar3.setVisibility(View.GONE);
                                    binding.verifyOTPButton.setVisibility(View.VISIBLE);
                                    preferenceManager.putString(Constants.KEY_USER_ID, auth_user.getUid());
                                    preferenceManager.putString(Constants.KEY_IMAGE, getIntent().getStringExtra(Constants.KEY_IMAGE));
                                    preferenceManager.putString(Constants.KEY_USER_TYPE, "2");
                                    preferenceManager.putString(Constants.KEY_FULL_NAME, getIntent().getStringExtra(Constants.KEY_FULL_NAME));
                                    Intent intent = new Intent(OTPVerification.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }).addOnFailureListener(exception -> {
                                    binding.progressBar3.setVisibility(View.GONE);
                                    binding.verifyOTPButton.setVisibility(View.VISIBLE);
                                    showToast(exception.getMessage());
                                });
                            }

                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            showToast("Something Went Wrong!");
                            loading(false);
                            Log.d("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                showToast("Verification code entered was invalid");
                                loading(false);
                            }
                        }
                    }
                });
    }
    private void sendToLogin(){
        startActivity(new Intent(this, LoginActivity.class));
    }
    private void showToast(String message) {
        Toast.makeText(OTPVerification.this, message, Toast.LENGTH_SHORT).show();
    }
}