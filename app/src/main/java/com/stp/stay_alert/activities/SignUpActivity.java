package com.stp.stay_alert.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stp.stay_alert.R;
import com.stp.stay_alert.databinding.ActivitySignUpBinding;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private DocumentReference documentReference;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private String phoneNumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    public SignUpActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth.getInstance().signOut();

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
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
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
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
                Intent intent = new Intent(SignUpActivity.this, OTPVerification.class);
                intent.putExtra("OTP", mVerificationId);
                intent.putExtra("resendToken", mResendToken);
                intent.putExtra("phoneNumber",phoneNumber);
                intent.putExtra("AUTH_TYPE", "SIGNUP");
                intent.putExtra(Constants.KEY_FULL_NAME,  binding.fullName.getText().toString());
                intent.putExtra(Constants.KEY_ADDRESS,  binding.address.getText().toString());
                intent.putExtra(Constants.KEY_USER_TYPE, "2"); // 1 = superuser 2 = common user | 3 = admin
                intent.putExtra(Constants.KEY_IMAGE, encodedImage);
                startActivity(intent);
            }
        };
    }

    private void setListeners() {
        binding.signup.setEnabled(false);
        binding.imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.term.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                Context context = new ContextThemeWrapper(SignUpActivity.this, R.style.AppTheme2);
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
                dialogBuilder.setTitle("Terms and Conditions");
                dialogBuilder.setMessage("Last updated March 19, 2023\n" +
                        "\n" +
                        "AGREEMENT TO OUR LEGAL TERMS\n" +
                        "\n" +
                        "We are Scared to Compile (\"Company,\" \"we,\" \"us,\" \"our\"), a company registered in the Philippines at Daet, Camarines Norte, Daet, Camarines Norte 4600. We operate the mobile application Stay-Alert Application (the \"App\"), as well as any other related products and services that refer or link to these legal terms (the \"Legal Terms\") (collectively, the \"Services\").\n" +
                        "Stay-Alert is an app that you can install on your mobile devices. It allows you to report any emergencies in a dire situation.\n" +
                        "\n" +
                        "You can contact us by phone at 09814101644, email at scaredtocompile03@gmail.com, or by mail to Daet, Camarines Norte, Daet, Camarines Norte 4600, Philippines.\n" +
                        "\n" +
                        "These Legal Terms constitute a legally binding agreement made between you, whether personally or on behalf of an entity (\"you\"), and Scared to Compile, concerning your access to and use of the Services. You agree that by accessing the Services, you have read, understood, and agreed to be bound by all of these Legal Terms. IF YOU DO NOT AGREE WITH ALL OF THESE LEGAL TERMS, THEN YOU ARE EXPRESSLY PROHIBITED FROM USING THE SERVICES AND YOU MUST DISCONTINUE USE IMMEDIATELY.\n" +
                        "\n" +
                        "We will provide you with prior notice of any scheduled changes to the Services you are using. Changes to Legal Terms will become effective seven (7) days after the notice is given, except if the changes apply to new functionality, security updates, bug fixes, and a court order, in which case the changes will be effective immediately. By continuing to use the Services after the effective date of any changes, you agree to be bound by the modified terms. If you disagree with such changes, you may terminate Services as per the section \"TERM AND TERMINATION.\"\n" +
                        "All users who are minors in the jurisdiction in which they reside (generally under the age of 18) must have the permission of, and be directly supervised by, their parent or guardian to use the Services. If you are a minor, you must have your parent or guardian read and agree to these Legal Terms prior to you using the Services.\n" +
                        "We recommend that you print a copy of these Legal Terms for your records.\n" +
                        "\uFEFF\n" +
                        "1. OUR SERVICES\n" +
                        "\n" +
                        "The information provided when using the Services is not intended for distribution to or use by any person or entity in any jurisdiction or country where such distribution or use would be contrary to law or regulation or which would subject us to any registration requirement within such jurisdiction or country. Accordingly, those persons who choose to access the Services from other locations do so on their own initiative and are solely responsible for compliance with local laws, if and to the extent local laws are applicable.\n" +
                        "\n" +
                        "2. INTELLECTUAL PROPERTY RIGHTS\n" +
                        "\n" +
                        "Our intellectual property\n" +
                        "We are the owner or the licensee of all intellectual property rights in our Services, including all source code, databases, functionality, software, website designs, audio, video, text, photographs, and graphics in the Services (collectively, the \"Content\"), as well as the trademarks, service marks, and logos contained therein (the \"Marks\").\n" +
                        "Our Content and Marks are protected by copyright and trademark laws (and various other intellectual property rights and unfair competition laws) and treaties in the United States and around the world.\n" +
                        "The Content and Marks are provided in or through the Services \"AS IS\" for your personal, non- commercial use or internal business purpose only.\n" +
                        "\uFEFF\n" +
                        "Your use of our Services\n" +
                        "Subject to your compliance with these Legal Terms, including the \"PROHIBITED ACTIVITIES\" section below, we grant you a non-exclusive, non-transferable, revocable license to:\n" +
                        "⚫ access the Services; and\n" +
                        "⚫ download or print a copy of any portion of the Content to which you have properly gained access. Solely for your personal, non-commercial use or internal business purpose.\n" +
                        "\n" +
                        "Except as set out in this section or elsewhere in our Legal Terms, no part of the Services and no Content or Marks may be copied, reproduced, aggregated, republished, uploaded, posted, publicly displayed, encoded, translated, transmitted, distributed, sold, licensed, or otherwise exploited for any commercial purpose whatsoever, without our express prior written permission. If you wish to make any use of the Services, Content, or Marks other than as set out in this section or elsewhere in our Legal Terms, please address your request to: scaredtocompile03@gmail.com. If we ever grant you the permission to post, reproduce, or publicly display any part of our Services or Content, you must identify us as the owners or licensors of the Services, Content, or Marks and ensure that any copyright or proprietary notice appears or is visible on posting, reproducing, or displaying our Content.\n" +
                        "We reserve all rights not expressly granted to you in and to the Services, Content, and Marks. Any breach of these Intellectual Property Rights will constitute a material breach of our Legal Terms and your right to use our Services will terminate immediately.\n" +
                        "\uFEFF\n" +
                        "If you provide any information that is untrue, inaccurate, not current, or incomplete, we have the right to suspend or terminate your account and refuse any and all current or future use of the Services (or any portion thereof).\n" +
                        "\n" +
                        "3. USER REGISTRATION\n" +
                        "\n" +
                        "You may be required to register to use the Services. You agree to keep your password confidential and will be responsible for all use of your account and password. We reserve the right to remove, reclaim, or change a username you select if we determine, in our sole discretion, that such username is inappropriate, obscene, or otherwise objectionable.\n" +
                        "\n" +
                        "4. PROHIBITED ACTIVITIES\n" +
                        "\n" +
                        "You may not access or use the Services for any purpose other than that for which we make the Services available. The Services may not be used in connection with any commercial endeavors except those that are specifically endorsed or approved by us.\n" +
                        "\n" +
                        "As a user of the Services, you agree not to:\n" +
                        "\n" +
                        "⚫ Systematically retrieve data or other content from the Services to create or compile, directly or indirectly, a collection, compilation, database, or directory without written permission from us.\n" +
                        "⚫ Trick, defraud, or mislead us and other users, especially in any attempt to learn sensitive account information such as user passwords.\n" +
                        "\n" +
                        "Circumvent, disable, or otherwise interfere with security-related features of the Services, including features that prevent or restrict the use or copying of any Content or enforce limitations on the use of the Services and/or the Content contained therein. Disparage tarnish, or otherwise harm in our opinion, us and/or the Services\n" +
                        "\uFEFF5. SERVICES MANAGEMENT\n" +
                        "\n" +
                        "We reserve the right, but not the obligation, to: (1) monitor the Services for violations of these Legal Terms; (2) take appropriate legal action against anyone who, in our sole discretion, violates the law or these Legal Terms, including without limitation, reporting such user to law enforcement authorities; (3) in our sole discretion and without limitation, refuse, restrict access to, limit the availability of, or disable (to the extent technologically feasible) any of your Contributions or any portion thereof, (4) in our sole discretion and without limitation, notice, or liability, to remove from the Services or otherwise disable all files and content that are excessive in size or are in any way burdensome to our systems; and (5) otherwise manage the Services in a manner designed to protect our rights and property and to facilitate the proper functioning of the Services.\n" +
                        "\n" +
                        "6. PRIVACY POLICY\n" +
                        "\n" +
                        "We care about data privacy and security. Please review our Privacy\n" +
                        "Policy: https://legacy.senate.gov.ph/lisdata/32706295691.pdf. By using the Services, you agree to be bound by our Privacy Policy, which is incorporated into these Legal Terms. Please be advised the Services are hosted in the Philippines. If you access the Services from any other region of the world with laws or other requirements governing personal data collection, use, or disclosure that differ from applicable laws in the Philippines, then through your continued use of the Services, you are transferring your data to the Philippines, and you expressly consent to have your data transferred to and processed in the Philippines.\n" +
                        "\uFEFF\n" +
                        "7. TERM AND TERMINATION\n" +
                        "\n" +
                        "These Legal Terms shall remain in full force and effect while you use the Services. WITHOUT LIMITING ANY OTHER PROVISION OF THESE LEGAL TERMS, WE RESERVE THE RIGHT TO, IN OUR SOLE DISCRETION AND WITHOUT NOTICE OR LIABILITY, DENY ACCESS TO AND USE OF THE SERVICES (INCLUDING BLOCKING CERTAIN IP ADDRESSES), TO ANY PERSON FOR ANY REASON OR FOR NO REASON, INCLUDING WITHOUT LIMITATION FOR BREACH OF ANY REPRESENTATION, WARRANTY, OR COVENANT CONTAINED IN THESE LEGAL TERMS OR OF ANY APPLICABLE LAW OR REGULATION. WE MAY TERMINATE YOUR USE OR PARTICIPATION IN THE SERVICES OR DELETE YOUR ACCOUNT AND ANY CONTENT OR INFORMATION THAT YOU POSTED AT ANY TIME, WITHOUT WARNING, IN OUR SOLE DISCRETION.\n" +
                        "If we terminate or suspend your account for any reason, you are prohibited from registering and creating a new account under your name, a fake or borrowed name, or the name of any third party, even if you may be acting on behalf of the third party. In addition to terminating or suspending your account, we reserve the right to take appropriate legal action, including without limitation pursuing civil, criminal, and injunctive redress.\n" +
                        "\n" +
                        "8. MODIFICATIONS AND INTERRUPTIONS\n" +
                        "\n" +
                        "We reserve the right to change, modify, or remove the contents of the Services at any time or for any reason at our sole discretion without notice. However, we have no obligation to update any information on our Services. We will not be liable to you or any third party for any modification, price change, suspension, or discontinuance of the Services.\n" +
                        "\uFEFF\n" +
                        "9. GOVERNING LAW\n" +
                        "\n" +
                        "These Legal Terms shall be governed by and defined following the laws of the Philippines. Scared to Compile and yourself irrevocably consent that the courts of the Philippines shall have exclusive jurisdiction to resolve any dispute which may arise in connection with these Legal Terms.\n" +
                        "\n" +
                        "10. DISPUTE RESOLUTION\n" +
                        "\n" +
                        "You agree to irrevocably submit all disputes related to these Legal Terms or the legal relationship established by these Legal Terms to the jurisdiction of the Philippines courts. Scared to Compile shall also maintain the right to bring proceedings as to the substance of the matter in the courts of the country where you reside or, if these Legal Terms are entered into in the course of your trade or profession, the state of your principal place of business.\n" +
                        "\n" +
                        "11. CORRECTIONS\n" +
                        "\n" +
                        "There may be information on the Services that contains typographical errors, inaccuracies, or omissions, including descriptions, pricing, availability, and various other information. We reserve the right to correct any errors, inaccuracies, or omissions and to change or update the information on the Services at any time, without prior notice.\n" +
                        "\uFEFF\n" +
                        "12. DISCLAIMER\n" +
                        "\n" +
                        "THE SERVICES ARE PROVIDED ON AN AS-IS AND AS-AVAILABLE BASIS. YOU AGREE THAT YOUR USE OF THE SERVICES WILL BE AT YOUR SOLE RISK. TO THE FULLEST EXTENT PERMITTED BY LAW, WE DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, IN CONNECTION WITH THE SERVICES AND YOU’RE USE THEREOF, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT. WE MAKE NO WARRANTIES OR REPRESENTATIONS ABOUT THE ACCURACY OR COMPLETENESS OF THE SERVICES CONTENT OR THE CONTENT OF ANY WEBSITES OR MOBILE APPLICATIONS LINKED TO THE SERVICES AND WE WILL ASSUME NO LIABILITY OR RESPONSIBILITY FOR ANY (1) ERRORS, MISTAKES, OR INACCURACIES OF CONTENT AND MATERIALS, (2) PERSONAL INJURY OR PROPERTY DAMAGE, OF ANY NATURE WHATSOEVER, RESULTING FROM YOUR ACCESS TO AND USE OF THE SERVICES, (3) ANY UNAUTHORIZED ACCESS TO OR USE OF OUR SECURE SERVERS AND/OR ANY AND ALL PERSONAL INFORMATION AND/OR FINANCIAL INFORMATION STORED THEREIN, (4) ANY INTERRUPTION OR CESSATION OF TRANSMISSION TO OR FROM THE SERVICES, (5) ANY BUGS, VIRUSES, TROJAN HORSES, OR THE LIKE WHICH MAY BE TRANSMITTED TO OR THROUGH THE SERVICES BY ANY THIRD PARTY, AND/OR (6) ANY ERRORS OR OMISSIONS IN ANY CONTENT AND MATERIALS OR FOR ANY LOSS OR DAMAGE OF ANY KIND INCURRED AS A RESULT OF THE USE OF ANY CONTENT POSTED, TRANSMITTED, OR OTHERWISE MADE AVAILABLE VIA THE SERVICES. WE DO NOT WARRANT, ENDORSE, GUARANTEE, OR ASSUME RESPONSIBILITY FOR ANY PRODUCT OR SERVICE ADVERTISED OR OFFERED BY A THIRD PARTY THROUGH THE SERVICES, ANY HYPERLINKED WEBSITE, OR ANY WEBSITE OR MOBILE APPLICATION FEATURED IN ANY BANNER OR OTHER ADVERTISING, AND WE WILL NOT BE A PARTY TO OR IN ANY WAY BE RESPONSIBLE FOR MONITORING ANY TRANSACTION BETWEEN YOU AND ANY THIRD-PARTY PROVIDERS OF PRODUCTS OR SERVICES. AS WITH THE PURCHASE OF A PRODUCT OR SERVICE THROUGH ANY MEDIUM OR IN ANY ENVIRONMENT, YOU SHOULD USE YOUR BEST JUDGMENT AND EXERCISE CAUTION WHERE APPROPRIATE.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "13. LIMITATIONS OF LIABILITY\n" +
                        "\n" +
                        "IN NO EVENT WILL WE OR OUR DIRECTORS, EMPLOYEES, OR AGENTS BE LIABLE TO YOU OR ANY THIRD PARTY FOR ANY DIRECT, INDIRECT, CONSEQUENTIAL, EXEMPLARY, INCIDENTAL, SPECIAL, OR PUNITIVE DAMAGES, INCLUDING LOST PROFIT, LOST REVENUE, LOSS OF DATA, OR OTHER DAMAGES ARISING FROM YOUR USE OF THE SERVICES, EVEN IF WE HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\n" +
                        "\n" +
                        "14. INDEMNIFICATION\n" +
                        "\n" +
                        "You agree to defend, indemnify, and hold us harmless, including our subsidiaries, affiliates, and all of our respective officers, agents, partners, and employees, from and against any loss, damage, liability, claim, or demand, including reasonable attorneys' fees and expenses, made by any third party due to or arising out of: (1) your Contributions; (2) use of the Services; (3) breach of these Legal Terms; (4) any breach of your representations and warranties set forth in these Legal Terms; (5) your violation of the rights of a third party, including but not limited to intellectual property rights; or (6) any overt harmful act toward any other user of the Services with whom you connected via the Services. Notwithstanding the foregoing, we reserve the right, at your expense, to assume the exclusive defense and control of any matter for which you are required to indemnify us, and you agree to cooperate, at your expense, with our defense of such claims. We will use reasonable efforts to notify you of any such claim, action, or proceeding which is subject to this indemnification upon becoming aware of it.");
                dialogBuilder.setPositiveButton("Accept", (dialogInterface, i) -> {
                    binding.signup.setEnabled(true);
                    dialogInterface.dismiss();
                });
                dialogBuilder.setNegativeButton("Decline", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    binding.term.setChecked(false);
                });
                dialogBuilder.show();
            }else{
                binding.signup.setEnabled(false);
            }
        });
        binding.signin.setOnClickListener(v -> onBackPressed());
        binding.signup.setOnClickListener(v ->{
            if(isValidSignUpDetails()){
                phoneNumber = binding.contact.getText().toString().trim();
                if(!phoneNumber.isEmpty()){
                    if(phoneNumber.length() == 11){
                        phoneNumber = "+63"+phoneNumber.substring(1);
                        registerUserV2(phoneNumber);
                    }
                }else{
                    showToast("Number must not be empty!");
                }
            }else{
                showToast("Invalid Sign-up details provided!");
            }
        });
        binding.profileLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//    private void signup() {
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> admin = new HashMap<>();
//        admin.put(Constants.KEY_EMAIL, binding.email.getText().toString());
////        admin.put(Constants.KEY_NAME, binding.username.getText().toString());
//        admin.put(Constants.KEY_ADDRESS, binding.address.getText().toString());
//        admin.put(Constants.KEY_PASSWORD, binding.password.getText().toString());
//        admin.put(Constants.KEY_CONTACT, binding.contact.getText().toString());
//        admin.put(Constants.KEY_IMAGE, encodedImage);
//        database.collection(Constants.KEY_COLLECTION_ADMIN).add(admin).addOnSuccessListener(documentReference -> {
//            loading(false);
//            preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN, true);
//            preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
////            preferenceManager.putString(Constants.KEY_NAME, binding.username.getText().toString());
//            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
//            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }).addOnFailureListener(exception -> {
//            loading(false);
//            showToast(exception.getMessage());
//
//        });
//    }

    private String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                            binding.imageProfile.setImageBitmap(bitmap);
                            Glide.with(getApplicationContext()).load(bitmap).into(binding.imageProfile);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void registerUserV2(String phoneNumber){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_CONTACT, phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot document = task.getResult();
                            if(document.size() == 0){
                                PhoneAuthOptions options =
                                        PhoneAuthOptions.newBuilder(mAuth)
                                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                                .setActivity(SignUpActivity.this)                 // Activity (for callback binding)
                                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                                .build();
                                PhoneAuthProvider.verifyPhoneNumber(options);
                            }else{
                                showToast("User with this phone# already exists!");
                                loading(false);
                            }
                        } else {
                            Log.d("ERROR-USER", "Error getting documents: ", task.getException());
                            loading(false);
                        }
                    }
                });
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
                            user.put(Constants.KEY_FULL_NAME, binding.fullName.getText().toString());
                            user.put(Constants.KEY_ADDRESS, binding.address.getText().toString());
                            user.put(Constants.KEY_CONTACT, binding.contact.getText().toString());
                            user.put(Constants.KEY_USER_TYPE, "2"); // 1 = superuser 2 = common user | 3 = admin
                            user.put(Constants.KEY_IMAGE, encodedImage);
                            user.put(Constants.KEY_AVAILABILITY, 0);
                            user.put(Constants.KEY_TIMESTAMP, new Date());
                            database.collection(Constants.KEY_COLLECTION_USERS).document(auth_user.getUid()).set(user).addOnSuccessListener(documentReference -> {
                                loading(false);
                                preferenceManager.putString(Constants.KEY_USER_ID, auth_user.getUid());
                                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
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
                            loading(false);
                            showToast("Authentication failed");
                            Log.d("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                loading(false);
                                showToast("Verification code entered was invalid");
                            }
                        }
                    }
                });
    }
    private void sendToLogin(){
        startActivity(new Intent(this, MainActivity.class));
    }
//    private void registerUser() {
//        loading(true);
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        auth.createUserWithEmailAndPassword(binding.email.getText().toString(),binding.password.getText().toString()).addOnCompleteListener(SignUpActivity.this, task -> {
//            if(task.isSuccessful()){
//                Objects.requireNonNull(auth.getCurrentUser()).reload();
//                FirebaseUser new_user = auth.getCurrentUser();
//                if(new_user != null){
//                    new_user.sendEmailVerification()
//                            .addOnCompleteListener(task1 -> {
//                                if (task1.isSuccessful()) {
//                                    Log.d("log", "Email sent.");
//                                }
//                            });
//
//                    FirebaseFirestore database = FirebaseFirestore.getInstance();
//                    HashMap<String, Object> user = new HashMap<>();
//                    user.put(Constants.KEY_EMAIL, binding.email.getText().toString());
////                    user.put(Constants.KEY_NAME, binding.username.getText().toString());
//                    user.put(Constants.KEY_ADDRESS, binding.address.getText().toString());
//                    user.put(Constants.KEY_CONTACT, binding.contact.getText().toString());
//                    user.put(Constants.KEY_USER_TYPE, "2"); // 1 = superuser 2 = common user | 3 = admin
//                    user.put(Constants.KEY_IMAGE, encodedImage);
//                    user.put(Constants.KEY_AVAILABILITY, 0);
//                    user.put(Constants.KEY_TIMESTAMP, new Date());
//                    database.collection(Constants.KEY_COLLECTION_USERS).document(new_user.getUid()).set(user).addOnSuccessListener(documentReference -> {
//                        loading(false);
//                        preferenceManager.putString(Constants.KEY_USER_ID, new_user.getUid());
////                        preferenceManager.putString(Constants.KEY_NAME, binding.username.getText().toString());
//                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
//                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }).addOnFailureListener(exception -> {
//                        loading(false);
//                        showToast(exception.getMessage());
//                    });
//                }else{
//                    showToast("Something went wrong");
//                    loading(false);
//                }
//            }else{
//                showToast(Objects.requireNonNull(task.getException()).getMessage());
//                loading(false);
//            }
//        });
//    }
    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile Image");
            return false;
        } else if (binding.fullName.getText().toString().trim().isEmpty()) {
            showToast("Enter Full Name");
            return false;
        }else if (binding.address.getText().toString().trim().isEmpty()) {
            showToast("Enter Address");
            return false;
        }else if (binding.contact.getText().toString().trim().length() != 11) {
            showToast("Number should  be 11 digits");
            return false;
        }else if(!Patterns.PHONE.matcher(binding.contact.getText().toString()).matches()) {
            showToast("Number exist!");
            return false;
        }else{
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.signup.setVisibility(View.INVISIBLE);
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.INVISIBLE);
            binding.signup.setVisibility(View.VISIBLE);
        }
    }
}
