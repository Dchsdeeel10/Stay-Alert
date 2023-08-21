package com.stp.stay_alert.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.stp.stay_alert.R;
import com.stp.stay_alert.adapater.ChatAdapter;
import com.stp.stay_alert.databinding.ActivityEducationalTipsBinding;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;


public class EducationalTips extends AppCompatActivity {

    private ActivityEducationalTipsBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationalTipsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        binding.imageButton6.setOnClickListener(view -> onBackPressed());

        Glide.with(EducationalTips.this)
                .load(Uri.parse("android.resource://"+getPackageName()+ "/"+ R.raw.earthquakeads))
                .into(binding.imageView9);
        binding.imageButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EducationalTips.this, ViewImageOrVideo.class);
                intent.putExtra("img_url", "");
                intent.putExtra("type", "video");
                intent.putExtra("video_url","android.resource://"+getPackageName()+ "/"+ R.raw.earthquakeads);
                startActivity(intent);
            }
        });

        Glide.with(EducationalTips.this)
                .load(Uri.parse("android.resource://"+getPackageName()+ "/"+ R.raw.fireads))
                .into(binding.imageView12);
        binding.imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EducationalTips.this, ViewImageOrVideo.class);
                intent.putExtra("img_url", "");
                intent.putExtra("type", "video");
                intent.putExtra("video_url","android.resource://"+getPackageName()+ "/"+ R.raw.fireads);
                startActivity(intent);
            }
        });
        Glide.with(EducationalTips.this)
                .load(Uri.parse("android.resource://"+getPackageName()+ "/"+ R.raw.landslideads))
                .into(binding.imageView13);
        binding.imageButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EducationalTips.this, ViewImageOrVideo.class);
                intent.putExtra("img_url", "");
                intent.putExtra("type", "video");
                intent.putExtra("video_url","android.resource://"+getPackageName()+ "/"+ R.raw.landslideads);
                startActivity(intent);
            }
        });
        Glide.with(EducationalTips.this)
                .load(Uri.parse("android.resource://"+getPackageName()+ "/"+ R.raw.vehicleaccidentads))
                .into(binding.imageView14);
        binding.imageButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EducationalTips.this, ViewImageOrVideo.class);
                intent.putExtra("img_url", "");
                intent.putExtra("type", "video");
                intent.putExtra("video_url","android.resource://"+getPackageName()+ "/"+ R.raw.vehicleaccidentads);
                startActivity(intent);
            }
        });
    }
}