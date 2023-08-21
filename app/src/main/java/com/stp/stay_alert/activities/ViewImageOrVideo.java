package com.stp.stay_alert.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stp.stay_alert.databinding.ActivitySendImageBinding;
import com.stp.stay_alert.databinding.ActivityViewImageOrVideoBinding;

public class ViewImageOrVideo extends AppCompatActivity {
    private ActivityViewImageOrVideoBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewImageOrVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.imageButton4.setOnClickListener(v -> onBackPressed());

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getString("type").equals("image")){
                Glide.with(this)
                        .load(extras.getString("img_url"))
                        .into(binding.imageView5);
                binding.imageView5.setVisibility(View.VISIBLE);
            }else if (extras.getString("type").equals("video")){
                setVideoToVideoView(Uri.parse(extras.getString("video_url")));
                binding.frameLayout.setVisibility(View.VISIBLE);
            }
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
        MediaController mediaController = new MediaController(binding.videoView2.getContext());
        mediaController.setAnchorView(binding.videoView2);
        binding.videoView2.setMediaController(mediaController);
        binding.videoView2.setVisibility(View.VISIBLE);
        binding.videoView2.setVideoURI(videoUri);
        binding.videoView2.requestFocus();
        binding.videoView2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                binding.videoView2.start();
            }
        });
    }
}