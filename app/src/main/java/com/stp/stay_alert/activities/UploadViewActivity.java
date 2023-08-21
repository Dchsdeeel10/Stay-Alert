package com.stp.stay_alert.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stp.stay_alert.databinding.ActivityUploadViewBinding;
import com.stp.stay_alert.models.User;
import com.stp.stay_alert.network.ApiClient;
import com.stp.stay_alert.network.ApiService;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadViewActivity extends AppCompatActivity {
    private ActivityUploadViewBinding binding;
    private PreferenceManager preferenceManager;
    private User receiverUser;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private String send_img_url;
    StorageReference storageReference;
    private String send_type = null;
    FirebaseStorage storage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        binding.btnCancel.setOnClickListener(v -> onBackPressed());

        Bundle extras = getIntent().getExtras();
        conversionId = (String) extras.get("conversionId");
        isReceiverAvailable = (Boolean) extras.get("isReceiverAvailable");
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lat = String.valueOf(extras.get("latitude"));
                String longi = String.valueOf(extras.get("longitude"));
                String address = extras.getString("curr_address");

                receiverUser = (User) extras.get("receiverUser");

                if(send_type != null){
                    if(send_type == "video"){
                        Uri url = Uri.parse(extras.getString("video_url"));
                        uploadVideoFirebase(url, send_type, lat, longi, address);
                    }else{
                        Uri url;
                        receiverUser = (User) extras.get("receiverUser");
                        if(extras.getInt("type") == 1){
                            url = (Uri) extras.get("url");
                            Bitmap bitmap = (Bitmap) extras.get("bitmap");
                            uploadImage(url, send_type, lat, longi, address, "cam", bitmap);
                        }else if(extras.getInt("type") == 2) {
                            url = Uri.parse(extras.getString("image_url"));
                            uploadImage(url, send_type, lat, longi, address, "", null);
                        }
                    }
                }else{
                    showToast("Type not defined!");
                }
            }
        });


            if(extras.getInt("type") == 1){
                send_type = "image";
                binding.imageView3.setImageBitmap((Bitmap) extras.get("bitmap"));
//                Glide.with(getApplicationContext()).load((Bitmap) extras.get("bitmap")).into(binding.imageView3);
                binding.imageLayout.setVisibility(View.VISIBLE);
            }else if(extras.getInt("type") == 2){
                send_type = "image";
//                binding.imageView3.setImageURI(Uri.parse(extras.getString("image_url")));
                Glide.with(getApplicationContext()).load(Uri.parse(extras.getString("image_url"))).into(binding.imageView3);
                binding.imageLayout.setVisibility(View.VISIBLE);
            }else if (extras.getInt("type") == 3){
                send_type = "video";
                setVideoToVideoView(Uri.parse(extras.getString("video_url")));
                binding.videoLayout.setVisibility(View.VISIBLE);
            }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
    }

    private void setVideoToVideoView(Uri videoUri) {
        MediaController mediaController = new MediaController(binding.videoView3.getContext());
        mediaController.setAnchorView(binding.videoView3);
        binding.videoView3.setMediaController(mediaController);
        binding.videoView3.setVisibility(View.VISIBLE);
        binding.videoView3.setVideoURI(videoUri);
        binding.videoView3.requestFocus();
        binding.videoView3.seekTo(100);
        binding.videoView3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                binding.videoView3.pause();
            }
        });
    }

    private void sendMessage(String type, String url, String latitude, String longitude, String curr_address) {

        HashMap<String, Object> message1 = new HashMap<>();
        message1.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message1.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
        message1.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message1.put(Constants.KEY_LOC_LATITUDE, latitude);
        message1.put(Constants.KEY_LOC_LONGITUDE, longitude);
        message1.put(Constants.KEY_ADDRESS, curr_address);
        message1.put("isReceived", false);
        message1.put(Constants.KEY_TIMESTAMP, new Date());
        message1.put("receivedAt", new Date());
        message1.put("status", 0);
        database.collection("reported_incident").add(message1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String incidentId = documentReference.getId();
                FirebaseFirestore database1 = FirebaseFirestore.getInstance();
                HashMap<String, Object> message = new HashMap<>();
                message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                message.put(Constants.KEY_MESSAGE, "Send "+ type);
                message.put(Constants.KEY_LOC_LATITUDE, latitude);
                message.put(Constants.KEY_LOC_LONGITUDE, longitude);
                message.put(Constants.KEY_ADDRESS, curr_address);
                message.put(Constants.KEY_MSG_TYPE, type);
                message.put(Constants.KEY_MEDIA_PATH, url);
                message.put(Constants.KEY_TIMESTAMP, new Date());
                message.put("incidentId", incidentId);
                database1.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            }
        });

        if (conversionId != null) {
            updateConversation("Send "+ type);
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, "Send "+ type);
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, "Send "+ type);

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        onBackPressed();
//        binding.inputMessage.setText(null);
//        binding.imageView.setImageDrawable(null);
    }
    private void uploadImage(Uri uri, String type, String lat, String longi, String address, String img_type, Bitmap bitmap)
    {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            send_img_url = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("images/" + send_img_url);
            if(img_type.equals("cam")){
                binding.imageView3.setDrawingCacheEnabled(true);
                binding.imageView3.buildDrawingCache();
                Bitmap bitmap1 = ((BitmapDrawable) binding.imageView3.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                showToast("Send "+ send_type + " successfully");
                                sendMessage(type, uri.toString(), lat, longi, address);
                            }
                        });
                    }
                });
            }else {
                ref.putFile(uri).addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                progressDialog.dismiss();
                                                showToast("Send " + send_type + " successfully");
                                                sendMessage(type, uri.toString(), lat, longi, address);
                                            }
                                        });
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UploadViewActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(
                                    UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            }
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }
    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {

                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("results");

                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification send successfully");
                } else {
                    showToast("Error:" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void uploadVideoFirebase(Uri uri, String type, String lat, String longi, String address){
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();
        String filePathAndName = "Videos/" + "video_" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        sendMessage(type, downloadUri.toString(), lat, longi, address);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Failed while uploading!");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(
                            UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress = (100.0* taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage( "Uploaded " + (int)progress + "%");
                    }
                });
    }

}



