package com.stp.stay_alert.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stp.stay_alert.R;
import com.stp.stay_alert.adapater.BaseActivity;
import com.stp.stay_alert.adapater.ChatAdapter;
import com.stp.stay_alert.databinding.ActivityChatBinding;
import com.stp.stay_alert.models.ChatMessage;
import com.stp.stay_alert.models.User;
import com.stp.stay_alert.network.ApiClient;
import com.stp.stay_alert.network.ApiService;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;
    private static final int PICK_IMAGE = 1;
    private Uri uri;
    private double latitude;
    private double longitude;
    private String curr_address;
    private FusedLocationProviderClient fusedLocationClient;
    LocationManager locationManager;
    private ImageView imageView;
    private String send_img_url;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final int CAMERA_REQ_CODE = 1001;
    private final int CAMERA_REQ_CODE_CAM = 1005;
    private final int VIDEO_PICK_GALLERY_CODE = 1002;
    private final int VIDEO_PICK_CAMERA_CODE = 1003;
    private String[] cameraPermissions;
    private Uri videoUri;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        preferenceManager = new PreferenceManager(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        getLocation();
        setListener();
        loadReceiverDetails();
        init();
        listenMessages();

        binding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE);
            }
        });
        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkPermission()){
                    requestCameraPermission(CAMERA_REQ_CODE_CAM);
                }else{
                    openCamera();
                }
            }
        });
        binding.btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPickDialog();
            }
        });

        binding.chatProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, chatProfile.class);
                intent.putExtra("receiverID", receiverUser.id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }else{

                HashMap<String, Object> user1 = new HashMap<>();
                user1.put(Constants.KEY_AVAILABILITY, 1);
                user1.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for ( DataSnapshot datasnapshot: snapshot.getChildren()){
                            userStatus = Objects.requireNonNull(datasnapshot.child(receiverUser.id).child("availability").getValue()).toString();
                            receiverUser.token = Objects.requireNonNull(datasnapshot.child(receiverUser.id).child("fcmToken").getValue()).toString();
                            Log.d("firebase" ,userStatus );

                        }
                        if(!userStatus.isEmpty()){
                            isReceiverAvailable = userStatus.equals("1");
                        }

                        if (isReceiverAvailable) {
                            binding.textAvailability.setVisibility(View.VISIBLE);
                        } else {
                            binding.textAvailability.setVisibility(View.GONE);
                        }

                        if(!preferenceManager.getString(Constants.KEY_USER_TYPE).equals("2")){
                            binding.linearLayout3.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("firebase", "load:onCancelled", error.toException());
                    }
                });
        }
    }

    private void videoPickDialog(){
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            if(!checkPermission()){
                                requestCameraPermission(CAMERA_REQ_CODE);
                            }else{
                                videoPickCamera();
                            }
                        } else if(i == 1){
                            // Gallery Pick
                            videoPickGallery();
                        }
                    }
                }).show();
    }

    private void requestCameraPermission(int code){
        ActivityCompat.requestPermissions(this, cameraPermissions, code);
    }
    private boolean checkPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQ_CODE:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if( cameraAccepted && storageAccepted){
                        videoPickCamera();
                    }else{
                        Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
            case CAMERA_REQ_CODE_CAM:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if( cameraAccepted && storageAccepted){
                        openCamera();
                    }else{
                        Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void videoPickGallery(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void openCamera(){
        Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(iCamera, CAMERA_REQ_CODE_CAM);
    }

    private void videoPickCamera(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                            }
                            Boolean coarseLocationGranted = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            }
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object

                                Geocoder geocoder = new Geocoder(ChatActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    String address = addresses.get(0).getAddressLine(0);
                                    curr_address = address;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Permission Denied, Location in turn OFF", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled. Would you like to enable it?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
                            getLocation();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            showToast("Location is required when using this featured.");
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && data != null)
        {
            if(data.getData() != null) {
                uri = data.getData();
                Intent intent = new Intent(this, UploadViewActivity.class);
                intent.putExtra("image_url", uri.toString());
                intent.putExtra("type", 2);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("curr_address", curr_address);
                intent.putExtra("receiverUser", receiverUser);
                intent.putExtra("conversionId", conversionId);
                intent.putExtra("isReceiverAvailable", isReceiverAvailable);
                startActivity(intent);
            }

        }else if(requestCode == CAMERA_REQ_CODE_CAM && data != null)
        {
                uri = data.getData();
                Bitmap bitmap = (Bitmap)(data.getExtras()).get("data");
                Intent intent = new Intent(this, UploadViewActivity.class);
                intent.putExtra("bitmap", bitmap);
                intent.putExtra("url", uri);
                intent.putExtra("type", 1);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("curr_address", curr_address);
                intent.putExtra("receiverUser", receiverUser);
                intent.putExtra("conversionId", conversionId);
                intent.putExtra("isReceiverAvailable", isReceiverAvailable);
                startActivity(intent);

        } else if(requestCode == VIDEO_PICK_CAMERA_CODE && data != null){
            videoUri = data.getData();
            Intent intent = new Intent(this, UploadViewActivity.class);
            intent.putExtra("video_url", videoUri.toString());
            intent.putExtra("type", 3);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("curr_address", curr_address);
            intent.putExtra("receiverUser", receiverUser);
            intent.putExtra("receiverUser", receiverUser);
            intent.putExtra("conversionId", conversionId);
            intent.putExtra("isReceiverAvailable", isReceiverAvailable);startActivity(intent);

        } else if (requestCode == VIDEO_PICK_GALLERY_CODE && data != null){
            videoUri = data.getData();
            Intent intent = new Intent(this, UploadViewActivity.class);
            intent.putExtra("video_url", videoUri.toString());
            intent.putExtra("type", 3);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("curr_address", curr_address);
            intent.putExtra("receiverUser", receiverUser);
            intent.putExtra("conversionId", conversionId);
            intent.putExtra("isReceiverAvailable", isReceiverAvailable);
            startActivity(intent);
        }else {
            showToast("No File Selected");
        }
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages, getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id,
                receiverUser.token, preferenceManager.getString(Constants.KEY_FCM_TOKEN), preferenceManager.getString(Constants.KEY_USER_TYPE)
        );
        binding.chatRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message1 = new HashMap<>();
        message1.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message1.put(Constants.KEY_FULL_NAME, preferenceManager.getString(Constants.KEY_FULL_NAME));
        message1.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message1.put(Constants.KEY_LOC_LATITUDE, latitude);
        message1.put(Constants.KEY_LOC_LONGITUDE, longitude);
        message1.put(Constants.KEY_ADDRESS, curr_address);
        message1.put("isReceived", false);
        message1.put(Constants.KEY_TIMESTAMP, new Date());
        message1.put("receivedAt", new Date());
        message1.put("status", 0);
        message1.put("isEvaluated", 0);
        message1.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        // add to report incident when common user send to the admin
        if(preferenceManager.getString(Constants.KEY_USER_TYPE).equals("2")){
            database.collection("reported_incident").add(message1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    String incidentId = documentReference.getId();
                    FirebaseFirestore database1 = FirebaseFirestore.getInstance();
                    HashMap<String, Object> message = new HashMap<>();
                    message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
                    message.put(Constants.KEY_LOC_LATITUDE, latitude);
                    message.put(Constants.KEY_LOC_LONGITUDE, longitude);
                    message.put(Constants.KEY_ADDRESS, curr_address);
                    message.put(Constants.KEY_MSG_TYPE, "text");
                    message.put(Constants.KEY_TIMESTAMP, new Date());
                    message.put("incidentId", incidentId);
                    database1.collection(Constants.KEY_COLLECTION_CHAT).add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            binding.inputMessage.setText(null);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("FINDING ERROR");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showToast("FINDING ERROR");
                }
            });
        }else{
            FirebaseFirestore database1 = FirebaseFirestore.getInstance();
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            message.put(Constants.KEY_LOC_LATITUDE, latitude);
            message.put(Constants.KEY_LOC_LONGITUDE, longitude);
            message.put(Constants.KEY_ADDRESS, curr_address);
            message.put(Constants.KEY_MSG_TYPE, "text");
            message.put(Constants.KEY_TIMESTAMP, new Date());
            message.put("incidentId", "");
            database1.collection(Constants.KEY_COLLECTION_CHAT).add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    binding.inputMessage.setText(null);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showToast("FINDING ERROR");
                }
            });
        }


        if (conversionId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_FULL_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }

            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_FULL_NAME, preferenceManager.getString(Constants.KEY_FULL_NAME));
                data.put(Constants.KEY_FCM_TOKEN, receiverUser.token);
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast("Invalid while sending notification " + exception.getMessage());
            }

//        binding.inputMessage.setText(null);
        binding.imageView.setImageDrawable(null);
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
                            Log.d("error", responseJson.toString());
                            JSONArray result = responseJson.getJSONArray("results");

                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) result.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast("ERRORRRRRRRR");
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

    private void listenAvailabilityOfReceiver() {
//        database.collection(Constants.KEY_COLLECTION_USERS).document(
//                receiverUser.id
//        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
//            if (error != null) {
//                return;
//            }
//            if (value != null) {
//                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
//                    int availability = Objects.requireNonNull(
//                            value.getLong(Constants.KEY_AVAILABILITY)
//                    ).intValue();
//                    isReceiverAvailable = availability == 1;
//                }
//                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
//                if (receiverUser.image == null) {
//                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
//                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
//                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
//                }
//            }
//            if (isReceiverAvailable) {
//                binding.textAvailability.setVisibility(View.VISIBLE);
//            } else {
//                binding.textAvailability.setVisibility(View.GONE);
//            }
//
//
//        });
    }

    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.media_path = documentChange.getDocument().getString(Constants.KEY_MEDIA_PATH);
                    chatMessage.msg_type = documentChange.getDocument().getString(Constants.KEY_MSG_TYPE);
                    chatMessage.incidentReportID = documentChange.getDocument().getString("incidentId");
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressbar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversation();
        }
    });

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.btnSend.setOnClickListener(v -> {
            if(!binding.inputMessage.getText().toString().equals("")){
                sendMessage();
            }else{
                showToast("Message is required!");
            }
        });
        binding.quickChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.inputMessage.setText("What: Anong uri ng aksidente?:\nWhen: Kailan nangyari ang aksidente?:\nWhere: Saan nangyari ang aksidente?:\nWho: Sino ang sangkot sa aksidente?:\n\nNote: Fill out this Form.");
                sendMessage();
            }
        });
        binding.quickChat2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.inputMessage.setText("Thank you! You're Report has been Received.\nOur Team will get into you're location as soon as possible.");
                sendMessage();
            }
        });
    }

    @NonNull
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
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

    private void checkForConversation() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void checkForConversionRemotely(String senderId, String receivedId) {

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();

        }
    };

//    @Override
//    protected void onResume() {
//        super.onResume();
////        listenAvailabilityOfReceiver();
//    }

}