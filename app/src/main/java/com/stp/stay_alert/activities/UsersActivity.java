package com.stp.stay_alert.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stp.stay_alert.adapater.BaseActivity;
import com.stp.stay_alert.adapater.UsersAdapter;
import com.stp.stay_alert.databinding.ActivityUsersBinding;
import com.stp.stay_alert.listeners.UserListener;
import com.stp.stay_alert.models.User;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private List<User> user_admin;
    private UsersAdapter usersAdapter;
    private String cm_is_active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        init();
        getAllAdmin();
        setListener();
        refresh();

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

    private void init(){
        user_admin = new ArrayList<>();
        usersAdapter = new UsersAdapter(user_admin, this);
        binding.usersRecycler.setAdapter(usersAdapter);
    }
    private void setListener(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getAllAdmin() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return ;
        }
        if(value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(currentUserId.equals(documentChange.getDocument().getId())){
                        continue;
                    }
                    if("3".equals(documentChange.getDocument().getString("user_type"))){
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("usersAvailability").child(documentChange.getDocument().getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                }
                                else {
                                    cm_is_active = String.valueOf(task.getResult().child("availability").getValue());
                                    User user = new User();
                                    user.name = documentChange.getDocument().getString(Constants.KEY_FULL_NAME);
                                    user.contact = documentChange.getDocument().getString(Constants.KEY_CONTACT);
                                    user.image = documentChange.getDocument().getString(Constants.KEY_IMAGE);
                                    user.token = documentChange.getDocument().getString(Constants.KEY_FCM_TOKEN);
                                    user.isActive = cm_is_active;
                                    user.id = documentChange.getDocument().getId();
                                    user_admin.add(user);
                                    usersAdapter.notifyDataSetChanged();
                                    binding.usersRecycler.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    // TODO: Make some logic here if needed
                }
            }
            usersAdapter.notifyDataSetChanged();
            binding.usersRecycler.smoothScrollToPosition(0);
            binding.usersRecycler.setVisibility(View.VISIBLE);
            loading(false);
        }
    };
    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            // user_type = '3' admin user
                            // user_type = '2' common user
                            // user_type = '1' super admin
                            if("3".equals(queryDocumentSnapshot.getString("user_type"))){
                                mDatabase = FirebaseDatabase.getInstance().getReference();
                                mDatabase.child("usersAvailability").child(queryDocumentSnapshot.getId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        }
                                        else {
                                            cm_is_active = String.valueOf(task.getResult().child("availability").getValue());
                                            User user = new User();
                                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                            user.isActive = cm_is_active;
                                            user.id = queryDocumentSnapshot.getId();
                                            users.add(user);

                                        }
                                    }
                                });
                            }
                        }
                        Log.d("LOGS",users.toString());
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecycler.setAdapter(usersAdapter);
                            binding.usersRecycler.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressbar.setVisibility(View.VISIBLE);
        }else{
            binding.progressbar.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void refresh(){
        binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                binding.swipeLayout.setRefreshing(false);
            }
        });
        binding.swipeLayout.setColorSchemeColors(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }


    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
        finish();
    }
}