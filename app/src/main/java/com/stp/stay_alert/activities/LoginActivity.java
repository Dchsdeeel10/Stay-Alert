package com.stp.stay_alert.activities;

;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stp.stay_alert.R;
import com.stp.stay_alert.databinding.ActivityLoginBinding;
import com.stp.stay_alert.network.NetworkChecker;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    private LocationManager locationManager;
    private FirebaseAuth mAuth;
    private BroadcastReceiver broadcastReceiver;
    private String uuid;
    FirebaseFirestore database;
    ArrayList<CharSequence> arrayListCollection = new ArrayList<>();
    ArrayAdapter<CharSequence> adapter;
    EditText txt; // user input bar
    private DatabaseReference mDatabase;
    private String mVerificationId;
    private String phoneNumber;
    private String current_user_type;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        broadcastReceiver = new NetworkChecker();
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        preferenceManager = new PreferenceManager(getApplicationContext());

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
                    showToast("Invalid Request!");
                    loading(false);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showToast("OTP request exceeded");
                    loading(false);
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
                showToast("OTP Successfully send.");
                mVerificationId = verificationId;
                mResendToken = token;
                loading(false);
                Intent intent = new Intent(LoginActivity.this, OTPVerification.class);
                intent.putExtra("OTP", mVerificationId);
                intent.putExtra("resendToken", mResendToken);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("AUTH_TYPE", "LOGIN");
                startActivity(intent);
            }
        };
        setListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void checkUserIfExisting(String number){
        database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_CONTACT, "+63"+number.substring(1))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot document = task.getResult();

                            if(document.size() == 1){
                                for (QueryDocumentSnapshot user_data : task.getResult()) {
                                    current_user_type = Objects.requireNonNull(user_data.get("user_type")).toString();
                                }
                                if(current_user_type.equals("1")){
                                    showToast("Admin user is not allowed!");
                                    loading(false);
                                }else{
                                    PhoneAuthOptions options =
                                            PhoneAuthOptions.newBuilder(mAuth)
                                                    .setPhoneNumber("+63"+number.substring(1))       // Phone number to verify
                                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                    .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                                    .build();
                                    PhoneAuthProvider.verifyPhoneNumber(options);
                                }
                            }else{
                                showToast("User with this Phone Number doesn't exists!");
                                loading(false);
                            }
                        } else {
                            Log.d("ERROR-USER", "Error getting documents: ", task.getException());
                            showToast(Objects.requireNonNull(task.getException()).toString());
                            loading(false);
                        }
                    }
                });
    }
    private void setListener() {
        binding.textView23.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.signin.setOnClickListener(v -> {
            loading(true);
            binding.phoneNumber.onEditorAction(EditorInfo.IME_ACTION_DONE);
            phoneNumber = binding.phoneNumber.getText().toString().trim();
            if(isValidSignInDetails()){
                if(!phoneNumber.isEmpty()){
                    if(phoneNumber.length() == 11){
                        checkUserIfExisting(phoneNumber);
                    }else{
                        showToast("Number must valid!");
                        loading(false);
                    }
                }else{
                    showToast("Number must not be empty!");
                    loading(false);
                }
            }else{
                showToast("Phone Number must valid!");
                loading(false);
            }
        });
//        binding.fPwd.setOnClickListener(v -> forgotPassword());
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
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
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
//    private void signIn(){
//        loading(true);
//        database = FirebaseFirestore.getInstance();
//        mAuth.signInWithEmailAndPassword(binding.email.getText().toString(), binding.password1.getText().toString())
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            FirebaseUser currentUser = mAuth.getCurrentUser();
//                            if(currentUser != null && currentUser.isEmailVerified()){
//                                uuid = currentUser.getUid();
//                                DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_USERS).document(uuid);
//                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                        if (task.isSuccessful()) {
//                                            DocumentSnapshot document = task.getResult();
//                                            if (document.exists()) {
//                                                preferenceManager.putString(Constants.KEY_USER_ID, uuid);
//                                                preferenceManager.putString(Constants.KEY_EMAIL, document.getString(Constants.KEY_EMAIL));
//                                                preferenceManager.putString(Constants.KEY_NAME, document.getString(Constants.KEY_NAME));
//                                                preferenceManager.putString(Constants.KEY_IMAGE, document.getString(Constants.KEY_IMAGE));
//                                                preferenceManager.putString(Constants.KEY_ADDRESS, document.getString(Constants.KEY_ADDRESS));
//                                                preferenceManager.putString(Constants.KEY_CONTACT, document.getString(Constants.KEY_CONTACT));
//                                                preferenceManager.putString(Constants.KEY_USER_TYPE, document.getString(Constants.KEY_USER_TYPE));
//                                                preferenceManager.putString(Constants.KEY_FCM_TOKEN, document.getString(Constants.KEY_FCM_TOKEN));
//                                                mDatabase = FirebaseDatabase.getInstance().getReference();
//                                                HashMap<String, Object> user1 = new HashMap<>();
//                                                user1.put(Constants.KEY_AVAILABILITY, 1);
//                                                user1.put(Constants.KEY_FCM_TOKEN, document.getString(Constants.KEY_FCM_TOKEN));
//                                                mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);
//                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                                startActivity(intent);
//                                            } else {
//                                                showToast("No such document");
//                                            }
//                                        } else {
//                                            showToast("get failed with " + task.getException());
//                                        }
//                                    }
//                                });
//                            }else{
//                                showToast("User is not verified!");
//                            }
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("login", "signInWithEmail:failure", task.getException());
//                            Toast.makeText(LoginActivity.this, "Invalid Email or Password",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        loading(false);
//                    }
//                });
//    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.signin.setVisibility(View.GONE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.GONE);
            binding.signin.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }
    private Boolean isValidSignInDetails(){
        if (binding.phoneNumber.getText().toString().trim().length() != 11) {
            return false;
        }else{
            return true;
        }
    }

    private void forgotPassword(){
        Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);

    }
}