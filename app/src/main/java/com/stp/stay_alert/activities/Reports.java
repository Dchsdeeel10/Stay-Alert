package com.stp.stay_alert.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DateTime;
import com.stp.stay_alert.databinding.ActivityReportsBinding;
import com.stp.stay_alert.utilities.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Reports extends AppCompatActivity {
    private ActivityReportsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
//        getIntent().getStringExtra("incidentID");
    }

    public void setListener(){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        binding.timeDepartureFromOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Reports.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";
                                if (hourOfDay >= 12){
                                    am_pm = "PM";
                                    hourOfDay = hourOfDay-12;
                                }else if(hourOfDay == 12){
                                    am_pm = "PM";
                                }else {
                                    am_pm = "AM";
                                }
                                String new_time = hourOfDay + ":" + minute + am_pm;
                                binding.timeDepartureFromOffice.setText(new_time);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        binding.timeDepartureFromIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Reports.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";
                                if (hourOfDay >= 12){
                                    am_pm = "PM";
                                    hourOfDay = hourOfDay-12;
                                }else if(hourOfDay == 12){
                                    am_pm = "PM";
                                }else {
                                    am_pm = "AM";
                                }
                                String new_time = hourOfDay + ":" + minute + am_pm;
                                binding.timeDepartureFromIncident.setText(new_time);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        binding.timeArrivalIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Reports.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";
                                if (hourOfDay >= 12){
                                    am_pm = "PM";
                                    hourOfDay = hourOfDay-12;
                                }else if(hourOfDay == 12){
                                    am_pm = "PM";
                                }else {
                                    am_pm = "AM";
                                }
                                String new_time = hourOfDay + ":" + minute + am_pm;
                                binding.timeArrivalIncident.setText(new_time);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        binding.timeArrivalBackOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Reports.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String am_pm = "";
                                if (hourOfDay > 12){
                                    am_pm = "PM";
                                    hourOfDay = hourOfDay-12;
                                } else if(hourOfDay == 12){
                                    am_pm = "PM";
                                }else {
                                    am_pm = "AM";
                                }
                                String new_time = hourOfDay + ":" + minute + am_pm;
                                binding.timeArrivalBackOffice.setText(new_time);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
    }

    public void init(){
        binding.timeArrivalIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.imageButton2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.nxtButton.setOnClickListener(v -> {
            if(!binding.incident.getText().toString().isEmpty() && !binding.timeArrivalIncident.getText().toString().isEmpty() && !binding.timeArrivalBackOffice.getText().toString().isEmpty() && !binding.timeDepartureFromIncident.getText().toString().isEmpty() && !binding.timeDepartureFromOffice.getText().toString().isEmpty()){
                binding.nxtButton.setVisibility(View.GONE);
                binding.nxt2Button.setVisibility(View.VISIBLE);
                binding.incidentTxt.setVisibility(View.GONE);
                binding.incident.setVisibility(View.GONE);
                binding.timeDepartureFromOffice.setVisibility(View.GONE);
                binding.timeDepartureFromIncident.setVisibility(View.GONE);
                binding.timeArrivalIncident.setVisibility(View.GONE);
                binding.timeArrivalBackOffice.setVisibility(View.GONE);
                binding.patientTxt.setVisibility(View.VISIBLE);
                binding.name.setVisibility(View.VISIBLE);
                binding.address.setVisibility(View.VISIBLE);
                binding.age.setVisibility(View.VISIBLE);
                binding.gender.setVisibility(View.VISIBLE);
                binding.conditionTxt.setVisibility(View.VISIBLE);
                binding.condition.setVisibility(View.VISIBLE);
                binding.treatmentTxt.setVisibility(View.VISIBLE);
                binding.treatment.setVisibility(View.VISIBLE);
                binding.informant.setVisibility(View.VISIBLE);
                binding.contactPerson.setVisibility(View.VISIBLE);
                binding.contactNum.setVisibility(View.VISIBLE);
                binding.relation.setVisibility(View.VISIBLE);
                binding.location.setVisibility(View.VISIBLE);
            }else {
                showToast("All Fields are required!");
            }

        });
        binding.nxt2Button.setOnClickListener(v -> {
            if(!binding.name.getText().toString().isEmpty() && !binding.address.getText().toString().isEmpty() && !binding.age.getText().toString().isEmpty() &&
            !binding.gender.getText().toString().isEmpty() && !binding.condition.getText().toString().isEmpty() && !binding.treatment.getText().toString().isEmpty() &&
            !binding.contactPerson.getText().toString().isEmpty() && !binding.contactNum.getText().toString().isEmpty() && !binding.relation.getText().toString().isEmpty() &&
            !binding.location.getText().toString().isEmpty()){
                binding.submitBtn.setVisibility(View.VISIBLE);
                binding.nxtButton.setVisibility(View.GONE);
                binding.nxt2Button.setVisibility(View.GONE);
//            binding.Date.setVisibility(View.GONE);
                binding.incidentTxt.setVisibility(View.GONE);
                binding.incident.setVisibility(View.GONE);
                binding.timeDepartureFromOffice.setVisibility(View.GONE);
                binding.timeDepartureFromIncident.setVisibility(View.GONE);
                binding.timeArrivalIncident.setVisibility(View.GONE);
                binding.timeArrivalBackOffice.setVisibility(View.GONE);
                binding.patientTxt.setVisibility(View.GONE);
                binding.name.setVisibility(View.GONE);
                binding.address.setVisibility(View.GONE);
                binding.age.setVisibility(View.GONE);
                binding.gender.setVisibility(View.GONE);
                binding.conditionTxt.setVisibility(View.GONE);
                binding.condition.setVisibility(View.GONE);
                binding.treatmentTxt.setVisibility(View.GONE);
                binding.treatment.setVisibility(View.GONE);
                binding.informant.setVisibility(View.GONE);
                binding.contactPerson.setVisibility(View.GONE);
                binding.contactNum.setVisibility(View.GONE);
                binding.relation.setVisibility(View.GONE);
                binding.location.setVisibility(View.GONE);

                binding.respondentTxt.setVisibility(View.VISIBLE);
                binding.respondents.setVisibility(View.VISIBLE);
                binding.driver.setVisibility(View.VISIBLE);
                binding.referred.setVisibility(View.VISIBLE);
                binding.vehicle.setVisibility(View.VISIBLE);
            }else {
                showToast("All Fields are Required!");
            }
        });
        binding.submitBtn.setOnClickListener(v -> {
            if (!binding.respondents.getText().toString().isEmpty() && !binding.driver.getText().toString().isEmpty() && !binding.referred.getText().toString().isEmpty() && !binding.vehicle.getText().toString().isEmpty()){
                saveReports();
            }else {
                showToast("All Fields are Required!");
            }
        });
    }
    public void saveReports(){
        loading(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> reports = new HashMap<>();
        reports.put(Constants.KEY_INCIDENT_ID, getIntent().getStringExtra("incidentID"));
        reports.put(Constants.KEY_INCIDENT, binding.incident.getText().toString());
        reports.put(Constants.KEY_TIMESTAMP, new Date());
        reports.put(Constants.KEY_TIME_DEPARTURE_FROM_THE_OFFICE, binding.timeDepartureFromOffice.getText().toString());
        reports.put(Constants.KEY_TIME_DEPARTURE_FROM_THE_INCIDENT, binding.timeDepartureFromIncident.getText().toString());
        reports.put(Constants.KEY_TIME_ARRIVAL_TO_THE_INCIDENT, binding.timeArrivalIncident.getText().toString());
        reports.put(Constants.KEY_TIME_ARRIVAL_TO_THE_OFFICE, binding.timeArrivalBackOffice.getText().toString());
        reports.put(Constants.KEY_NAME,binding.name.getText().toString());
        reports.put(Constants.KEY_ADDRESS, binding.address.getText().toString());
        reports.put(Constants.KEY_AGE, binding.age.getText().toString());
        reports.put(Constants.KEY_GENDER, binding.gender.getText().toString());
        reports.put(Constants.KEY_CONDITION_OF_PATIENT, binding.condition.getText().toString());
        reports.put(Constants.KEY_TREATMENT_APPLIED, binding.treatment.getText().toString());
        reports.put(Constants.KEY_CONTACT_PERSON, binding.contactPerson.getText().toString());
        reports.put(Constants.KEY_CONTACT_NUMBER, binding.contactNum.getText().toString());
        reports.put(Constants.KEY_RELATIONSHIP, binding.relation.getText().toString());
        reports.put(Constants.KEY_LOCATION_OF_INCIDENT, binding.location.getText().toString());
        reports.put(Constants.KEY_RESPONDERS, binding.respondents.getText().toString());
        reports.put(Constants.KEY_DRIVER, binding.driver.getText().toString());
        reports.put(Constants.KEY_REFERRED_TO, binding.referred.getText().toString());
        reports.put(Constants.KEY_VEHICLE_USED, binding.vehicle.getText().toString());

        database.collection(Constants.KEY_COLLECTION_REPORTS).add(reports).addOnSuccessListener(documentReference -> {
            FirebaseFirestore database1 = FirebaseFirestore.getInstance();
            DocumentReference documentReference1 =
                    database1.collection("reported_incident").document(getIntent().getStringExtra("incidentID"));
            documentReference1.update(
                    "status", 3
            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("LOG", "SUCCESS");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("LOG", "FAILED");
                }
            });
            loading(false);
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            showToast("Report Successfully Saved!");
        }).addOnFailureListener(exception -> {
            loading(false);
            showToast(exception.getMessage());
        });
    }
    public void reports(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> reports = new HashMap<>();

//        reports.put(Constants.KEY_DATE, binding.Date.getText().toString());
        reports.put(Constants.KEY_INCIDENT, binding.incident.getText().toString());
        reports.put(Constants.KEY_TIME_DEPARTURE_FROM_THE_OFFICE, binding.timeDepartureFromOffice.getText().toString());
        reports.put(Constants.KEY_TIME_DEPARTURE_FROM_THE_INCIDENT, binding.timeDepartureFromIncident.getText().toString());
        reports.put(Constants.KEY_TIME_ARRIVAL_TO_THE_INCIDENT, binding.timeArrivalIncident.getText().toString());
        reports.put(Constants.KEY_TIME_ARRIVAL_TO_THE_OFFICE, binding.timeArrivalBackOffice.getText().toString());
        reports.put(Constants.KEY_NAME,binding.name.getText().toString());
        reports.put(Constants.KEY_ADDRESS, binding.address.getText().toString());
        reports.put(Constants.KEY_AGE, binding.age.getText().toString());
        reports.put(Constants.KEY_GENDER, binding.gender.getText().toString());
        reports.put(Constants.KEY_CONDITION_OF_PATIENT, binding.condition.getText().toString());
        reports.put(Constants.KEY_TREATMENT_APPLIED, binding.treatment.getText().toString());
        reports.put(Constants.KEY_CONTACT_PERSON, binding.contactPerson.getText().toString());
        reports.put(Constants.KEY_CONTACT_NUMBER, binding.contactNum.getText().toString());
        reports.put(Constants.KEY_RELATIONSHIP, binding.relation.getText().toString());
        reports.put(Constants.KEY_LOCATION_OF_INCIDENT, binding.location.getText().toString());
        reports.put(Constants.KEY_RESPONDERS, binding.respondents.getText().toString());
        reports.put(Constants.KEY_DRIVER, binding.driver.getText().toString());
        reports.put(Constants.KEY_REFERRED_TO, binding.referred.getText().toString());
        reports.put(Constants.KEY_VEHICLE_USED, binding.vehicle.getText().toString());

        database.collection(Constants.KEY_COLLECTION_REPORTS).add(reports).addOnSuccessListener(documentReference -> {
            loading(false);
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            showToast("Added Report");
        }).addOnFailureListener(exception -> {
            loading(false);
            showToast(exception.getMessage());
        });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.submitBtn.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
            binding.submitBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private  boolean validDateReport(){
        if(binding.incident.getText().toString().isEmpty() || binding.timeDepartureFromOffice.getText().toString().isEmpty() || binding.timeDepartureFromIncident.getText().toString().isEmpty() ||
        binding.timeArrivalIncident.getText().toString().isEmpty() || binding.timeArrivalBackOffice.getText().toString().isEmpty() || binding.name.getText().toString().isEmpty() || binding.address.getText().toString().isEmpty() ||
        binding.age.getText().toString().isEmpty() || binding.gender.getText().toString().isEmpty() || binding.condition.getText().toString().isEmpty() || binding.treatment.getText().toString().isEmpty() || binding.contactPerson.getText().toString().isEmpty() ||
        binding.contactNum.getText().toString().isEmpty() || binding.relation.getText().toString().isEmpty() || binding.location.getText().toString().isEmpty() ||
        binding.respondents.getText().toString().isEmpty() || binding.driver.getText().toString().isEmpty() || binding.referred.getText().toString().isEmpty() || binding.vehicle.getText().toString().isEmpty()){
            showToast("Please add report");
            return false;
        }else {
            return true;
        }

    }

}