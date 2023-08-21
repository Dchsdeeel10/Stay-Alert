package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stp.stay_alert.databinding.ActivityLoginBinding;
import com.stp.stay_alert.databinding.ActivitySendImageBinding;

import java.io.IOException;
import java.util.UUID;

public class SendImage extends AppCompatActivity {

    private ActivitySendImageBinding binding;
    private String url, receiverName, senderUid, receiverUid;
    private Uri uri;
    Uri imageUrl;
    private UploadTask uploadTask;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference rootRef1, rootRef2;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private ImageView send_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }
    private void init(){
        Intent iin = getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            uri = (Uri) b.get("u");
            Log.d("image", uri.toString());
        }
        try {
            // Setting image on image view using Bitmap
            Bitmap bitmap = MediaStore
                    .Images
                    .Media
                    .getBitmap(
                            getContentResolver(),
                            uri);
//            binding.sendImage.setImageBitmap(bitmap);
        }

        catch (IOException e) {
            // Log the exception
            e.printStackTrace();
        }
    }
    private void uploadImage()
    {
        if (uri != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(uri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(SendImage.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(SendImage.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }
}