package com.stp.stay_alert.activities;

//import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
//import android.content.pm.PackageManager;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


//import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//import androidx.core.app.ActivityCompat;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
//import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.stp.stay_alert.R;
import com.stp.stay_alert.adapater.BaseActivity;
import com.stp.stay_alert.adapater.RecentConversationsAdapter;
import com.stp.stay_alert.databinding.ActivityMainBinding;
import com.stp.stay_alert.listeners.ConversionListener;
import com.stp.stay_alert.models.ChatMessage;
import com.stp.stay_alert.models.User;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;
import com.stp.stay_alert.network.NetworkChecker;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private String cm_receiver_token;
    private String cm_is_active;
    private BroadcastReceiver broadcastReceiver;
    DatabaseReference mDatabase;
    ChatMessage chatMessage;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }else{

                String uuid = currentUser.getUid();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                HashMap<String, Object> user1 = new HashMap<>();
                user1.put(Constants.KEY_AVAILABILITY, 1);
                user1.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                mDatabase.child("usersAvailability").child(preferenceManager.getString(Constants.KEY_USER_ID)).setValue(user1);
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (int i = 0; i < conversations.size(); i++){
                            for ( DataSnapshot datasnapshot: snapshot.getChildren()){
                                String userStatus = Objects.requireNonNull(datasnapshot.child(conversations.get(i).receiverId).child("availability").getValue()).toString();
                                conversations.get(i).isActive = userStatus;
                            }
                        }
                        Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
                        conversationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_USERS).document(uuid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                preferenceManager.putString(Constants.KEY_USER_ID, uuid);
                                preferenceManager.putString(Constants.KEY_FULL_NAME, document.getString(Constants.KEY_FULL_NAME));
                                preferenceManager.putString(Constants.KEY_IMAGE, document.getString(Constants.KEY_IMAGE));
                                preferenceManager.putString(Constants.KEY_ADDRESS, document.getString(Constants.KEY_ADDRESS));
                                preferenceManager.putString(Constants.KEY_CONTACT, document.getString(Constants.KEY_CONTACT));
                                preferenceManager.putString(Constants.KEY_USER_TYPE, document.getString(Constants.KEY_USER_TYPE));
//                                String u_type = document.getString(Constants.KEY_USER_TYPE);
//                                if(u_type!= null && u_type.equals("1")){
//                                    binding.addAccount.setVisibility(View.VISIBLE);
//                                }
//                                if(u_type != null && u_type.equals("3")){
//                                    binding.startChat.setVisibility(View.GONE);
//                                }
                            } else {
                                showToast("No such document");
                            }
                        } else {
                            showToast("get failed with " + task.getException());
                        }
                    }
                });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        broadcastReceiver = new NetworkChecker();
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        getToken();
        setListener();
        init();
        listerConversations();
        refresh();

    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecycler.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListener(){
        binding.info.setOnClickListener(v -> {
            Intent intent = new Intent(this, EducationalTips.class);
            intent.putExtra("re-authenticated", false);
            startActivity(intent);
        });
//        binding.btnReport.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Reports.class)));
        binding.webServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browseLink = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stay-alert-admin-web.vercel.app/"));
                startActivity(browseLink);
            }
        });
        binding.startChat.setOnClickListener(v -> {
            binding.startChat.setVisibility(View.GONE);
            binding.floatingActionButton2.setImageResource(R.drawable.ic_baseline_add_24);
            startActivity(new Intent(MainActivity.this, UsersActivity.class));
        });
        binding.addAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),RegAdminActivity.class)));
        binding.floatingActionButton2.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String u_type = preferenceManager.getString(Constants.KEY_USER_TYPE);
                if(u_type!= null && u_type.equals("1")){
                    if(binding.addAccount.getVisibility() == View.VISIBLE){
                        binding.addAccount.setVisibility(View.GONE);
                        binding.webServer.setVisibility(View.GONE);
                        binding.startChat.setVisibility(View.GONE);
                        binding.floatingActionButton2.setImageResource(R.drawable.ic_baseline_add_24);
                    }else{
                        binding.addAccount.setVisibility(View.VISIBLE);
                        binding.webServer.setVisibility(View.VISIBLE);
                        binding.startChat.setVisibility(View.VISIBLE);
                        binding.floatingActionButton2.setImageResource(R.drawable.baseline_horizontal_rule_24);
                    }
                }
                if(u_type != null && u_type.equals("3")){
                    if(binding.startChat.getVisibility() == View.VISIBLE){
                        binding.startChat.setVisibility(View.GONE);
                        binding.floatingActionButton2.setImageResource(R.drawable.ic_baseline_add_24);
                    }else{
                        binding.startChat.setVisibility(View.VISIBLE);
                        binding.floatingActionButton2.setImageResource(R.drawable.baseline_horizontal_rule_24);
                    }
                }
                if(u_type != null && u_type.equals("2")){
                    if(binding.startChat.getVisibility() == View.VISIBLE){
                        binding.startChat.setVisibility(View.GONE);
                        binding.floatingActionButton2.setImageResource(R.drawable.ic_baseline_add_24);
                    }else{
                        binding.startChat.setVisibility(View.VISIBLE);
                        binding.floatingActionButton2.setImageResource(R.drawable.baseline_horizontal_rule_24);
                    }
                }
            }
        }));
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("re-authenticated", false);
            startActivity(intent);
        });
    }

    private void loadUserDetails(){
        binding.textEmail.setText(preferenceManager.getString(Constants.KEY_FULL_NAME));
        if(preferenceManager.getString(Constants.KEY_IMAGE) != null){
            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
        }

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void listerConversations(){
        if(preferenceManager.getString(Constants.KEY_USER_TYPE).equals("2")){
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }else{
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{

        if (error != null){
            return;
        }
        if(value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    String u_type = documentChange.getDocument().getString(Constants.KEY_USER_TYPE);
                    String condi_token;
                    if( u_type != null && u_type.equals("3")){
                        condi_token = receiverId;
                    }else{
                        condi_token = senderId;
                    }

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("usersAvailability").child(condi_token).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            }
                            else {
                                cm_receiver_token = String.valueOf(task.getResult().child(Constants.KEY_FCM_TOKEN).getValue());
                                cm_is_active = String.valueOf(task.getResult().child("availability").getValue());
                                chatMessage = new ChatMessage();
                                chatMessage.senderId = senderId;
                                chatMessage.receiverId = receiverId;

                                if(preferenceManager.getString(Constants.KEY_USER_TYPE).equals("2")){
                                    chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                                    chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                                    chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                                }else{
                                    chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                                    chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                                    chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                                }
                                chatMessage.isActive = cm_is_active;
                                chatMessage.receiverToken = cm_receiver_token;
                                chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                                chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                conversations.add(chatMessage);
                                conversationsAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecycler.smoothScrollToPosition(0);
            binding.conversationsRecycler.setVisibility(View.VISIBLE);
            binding.progressbar.setVisibility(View.GONE);
        }
    };

    private void  getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    @Override
    public void onConversionListener(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
    @SuppressLint("ResourceAsColor")
    private void refresh(){

    }
}