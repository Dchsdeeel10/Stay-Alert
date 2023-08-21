package com.stp.stay_alert.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.databinding.ActivityProfileBinding;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        binding.logoutBtn.setOnClickListener(v -> signOut());
        preferenceManager = new PreferenceManager(getApplicationContext());

        init();
    }
    private void init(){
        if(preferenceManager.getString(Constants.KEY_IMAGE) != null){
            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            binding.viewImage.setImageBitmap(bitmap);
        }

        binding.textViewFullName.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        binding.tvFullname.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        binding.tvAddress.setText(preferenceManager.getString(Constants.KEY_ADDRESS));
        binding.tvMobileNumber.setText(preferenceManager.getString(Constants.KEY_CONTACT));
        binding.profileBackBtn.setOnClickListener(v -> onBackPressed());
        binding.btnProfileChange.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.saveChanges.setOnClickListener(v -> {
            binding.progressBar4.setVisibility(View.VISIBLE);
            binding.saveChanges.setVisibility(View.GONE);
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> user = new HashMap<>();
            user.put(Constants.KEY_IMAGE, encodedImage);
            database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(user).addOnSuccessListener(documentReference -> {
                binding.progressBar4.setVisibility(View.GONE);
                binding.saveChanges.setVisibility(View.GONE);
                binding.btnProfileChange.setVisibility(View.VISIBLE);
                binding.imageView10.setVisibility(View.VISIBLE);
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                showToast("Save changes");

            }).addOnFailureListener(exception -> {
                binding.progressBar4.setVisibility(View.GONE);
                binding.saveChanges.setVisibility(View.GONE);
                showToast(exception.getMessage());
            });
        });
//        binding.changePassword.setOnClickListener(v -> {
//            if(extras.getBoolean("re-authenticated")){
//                binding.changePassForm.setVisibility(View.VISIBLE);
//                binding.changePassword.setVisibility(View.GONE);
//            }else{
//                startActivity(new Intent(getApplicationContext(),ReAunthenticateActivity.class));
//            }
//        });
//        binding.changePassCancelBtn.setOnClickListener(v -> {
//            binding.changePassForm.setVisibility(View.GONE);
//            binding.changePassword.setVisibility(View.VISIBLE);
//        });
//        binding.changePassSaveBtn.setOnClickListener(v -> {
//                binding.changePassSaveBtn.setVisibility(View.GONE);
//                binding.changePassCancelBtn.setVisibility(View.GONE);
//                binding.changePassProgBar.setVisibility(View.VISIBLE);
//                String pass1 = binding.txtPass.getText().toString();
//                String pass2 = binding.txtRePass.getText().toString();
//                if(isValidChangePasswordForm()){
//                    changPassword(pass1, pass2);
//                }
//        });
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
//                            binding.viewImage.setImageBitmap(bitmap);
                            Glide.with(getApplicationContext()).load(bitmap).into(binding.viewImage);
                            encodedImage = encodedImage(bitmap);
                            binding.btnProfileChange.setVisibility(View.GONE);
                            binding.imageView10.setVisibility(View.GONE);
                            binding.saveChanges.setVisibility(View.VISIBLE);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

    private String encodedImage(@NonNull Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void signOut(){
        showToast("Signing out...");
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));

    }
}