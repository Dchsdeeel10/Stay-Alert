package com.stp.stay_alert.adapater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stp.stay_alert.activities.ChatActivity;
import com.stp.stay_alert.activities.Reports;
import com.stp.stay_alert.activities.ViewImageOrVideo;
import com.stp.stay_alert.databinding.ItemContainerReceivedMessageBinding;
import com.stp.stay_alert.databinding.ItemContainerSentMessageBinding;
import com.stp.stay_alert.databinding.ItemContainerUserBinding;
import com.stp.stay_alert.models.ChatMessage;
import com.stp.stay_alert.network.ApiClient;
import com.stp.stay_alert.network.ApiService;
import com.stp.stay_alert.utilities.Constants;
import com.stp.stay_alert.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private String senderId;
    private String receiverID;
    private String receiverToken;
    private String FCM_TOKEN;
    private String user_type;
//    private String incidentReportID;
    private int status;
    public static final int VIEW_TYPE_SEND = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, String receiverID,
                       String receiverToken,  String FCM_TOKEN, String user_type) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.receiverID = receiverID;
        this.receiverToken = receiverToken;
        this.FCM_TOKEN = FCM_TOKEN;
        this.user_type = user_type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEND){
            return new SendMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                                    parent, false)
            );
        }else {
            return new ReceivedMessageHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false)
            );
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND){
            ((SendMessageViewHolder)holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageHolder)holder).setData(chatMessages.get(position),receiverProfileImage, user_type);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SEND;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SendMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;
        private String img_url;
        private String video_url;
        private String type;
        SendMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
            binding.viewSendImages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!type.equals("text")){
                        Intent intent = new Intent(view.getContext(),ViewImageOrVideo.class);
                        intent.putExtra("img_url", img_url);
                        intent.putExtra("type", type);
                        intent.putExtra("video_url", video_url);
                        SendMessageViewHolder.this.itemView.getContext().startActivity(intent);
                    }
                }
            });
        }

        void setData(ChatMessage chatMessage){
            if(chatMessage.msg_type.equals("text")){
                type = chatMessage.msg_type;
                binding.textMessage.setText(chatMessage.message);
                binding.textDateTime.setText(chatMessage.dateTime);
                binding.viewSendImages.setVisibility(View.GONE);
                binding.textImageDateTime.setVisibility(View.GONE);
                binding.playImage.setVisibility(View.GONE);
            }else if (chatMessage.msg_type.equals("image")){
                type = chatMessage.msg_type;
                img_url = chatMessage.media_path;
                Glide.with(itemView.getContext())
                        .load(chatMessage.media_path)
                        .into(binding.viewSendImages);
                binding.textImageDateTime.setText(chatMessage.dateTime);
                binding.textDateTime.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
                binding.playImage.setVisibility(View.GONE);
            }else if (chatMessage.msg_type.equals("video")){
                type = chatMessage.msg_type;
                video_url = chatMessage.media_path;
                Glide.with(itemView.getContext())
                        .load(chatMessage.media_path)
                        .into(binding.viewSendImages);
                binding.textImageDateTime.setText(chatMessage.dateTime);
                binding.textDateTime.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
                binding.playImage.setVisibility(View.VISIBLE);
            }
        }

    }
    class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;
        private String img_url;
        private String video_url;
        private String type;

        ReceivedMessageHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());

            binding = itemContainerReceivedMessageBinding;
            binding.receiveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!type.equals("text")){
                        Intent intent = new Intent(view.getContext(),ViewImageOrVideo.class);
                        intent.putExtra("img_url", img_url);
                        intent.putExtra("type", type);
                        intent.putExtra("video_url", video_url);
                        ReceivedMessageHolder.this.itemView.getContext().startActivity(intent);
                    }
                }
            });
            binding.closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.modal.setVisibility(View.GONE);
                }
            });
        }

        void showDialog(String incidentReportID, TextView textview, int status){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.itemView.getContext());
            dialogBuilder.setTitle("Confirmations");
            dialogBuilder.setMessage("Accept this and indicate when the MDRRMO-Paracale will be going to rescue the incident..");
            if(status != 1){
                dialogBuilder.setPositiveButton("RESCUE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateReport(true, "We got your reported incident!, Please wait Until na team arrive on area.", 1, incidentReportID, textview);
                        dialogInterface.dismiss();
                        binding.receivedImg.setVisibility(View.VISIBLE);
                        binding.writeReport.setVisibility(View.VISIBLE);
                    }
                });
            }
            dialogBuilder.setNeutralButton("INVALID", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    updateReport(true, "INVALID REPORT!", 4, incidentReportID, textview);
                    binding.receivedImg.setVisibility(View.VISIBLE);
                    binding.writeReport.setVisibility(View.GONE);
                }
            });
            dialogBuilder.setNegativeButton("Unattended", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateReport(true, "Unattended Report", 2, incidentReportID, textview);
                    binding.receivedImg.setVisibility(View.VISIBLE);
                    binding.writeReport.setVisibility(View.GONE);
                }
            });
            dialogBuilder.show();
        }
        void updateReport(Boolean parm, String meesage, int status, String incidentReportID, TextView textview){
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    database.collection("reported_incident").document(incidentReportID);
            documentReference.update(
                    "isReceived", parm,
                    "receivedAt", new Date(),
                    "status", status
            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("LOG", "SUCCESS");
                    if(status != 1){
                        textview.setOnLongClickListener(null);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("LOG", "FAILED");
                }
            });
                              ;
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverToken);
                JSONObject data = new JSONObject();
                data.put("userID", senderId);
                data.put(Constants.KEY_FCM_TOKEN, receiverToken);
                data.put(Constants.KEY_MESSAGE,  meesage);

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast("Invalid while sending notification " + exception.getMessage());
            }
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

        void showToast(String message) {
            Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
        }

        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage, String curr_user_type){
//            if(curr_user_type.equals("3")){
//                binding.receiveImage.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View view) {
//                        showDialog(chatMessage.incidentReportID, );
//                        return true;
//                    }
//                });
//            }

            binding.writeReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(),Reports.class);
                    intent.putExtra("incidentID", chatMessage.incidentReportID);
                    ReceivedMessageHolder.this.itemView.getContext().startActivity(intent);
                }
            });
            FirebaseFirestore database1 = FirebaseFirestore.getInstance();
            if(!chatMessage.incidentReportID.isEmpty()){
                DocumentReference docRef = database1.collection("reported_incident").document(chatMessage.incidentReportID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("LOG", "DocumentSnapshot data: " + document.getData());
                                if(Boolean.TRUE.equals(document.getBoolean("isReceived"))){
                                    binding.receivedImg.setVisibility(View.VISIBLE);
                                }else{
                                    binding.receivedImg.setVisibility(View.GONE);
                                }
                                int status = Objects.requireNonNull(document.getLong("status")).intValue();
                                if(status == 1){
                                    binding.writeReport.setVisibility(View.VISIBLE);
                                }else{
                                    binding.writeReport.setVisibility(View.GONE);
                                }

                                if(curr_user_type.equals("3")) {
                                    binding.textMessage.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View view) {
                                            showDialog(chatMessage.incidentReportID, binding.textMessage, status);
                                            return true;
                                        }
                                    });
                                }

                                if(status == 3 || status == 2 || status == 4){
                                    binding.textMessage.setOnLongClickListener(null);
                                }
                            } else {
                                Log.d("LOG", "No such document");
                            }
                        } else {
                            Log.d("LOG", "get failed with ", task.getException());
                        }
                    }
                });
            }

            if (chatMessage.msg_type.equals("text")){
                type = chatMessage.msg_type;
                binding.textMessage.setText(chatMessage.message);
                binding.textDateTime.setText(chatMessage.dateTime);
                binding.receiveImage.setVisibility(View.GONE);
                binding.textDateTime2.setVisibility(View.GONE);
                binding.playImageRec.setVisibility(View.GONE);
            }else if (chatMessage.msg_type.equals("image")){
                type = chatMessage.msg_type;
                img_url = chatMessage.media_path;
                Glide.with(itemView.getContext())
                        .load(chatMessage.media_path)
                        .into(binding.receiveImage);
                binding.receiveImage.setVisibility(View.VISIBLE);
                binding.textDateTime2.setText(chatMessage.dateTime);
                binding.textDateTime.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
                binding.playImageRec.setVisibility(View.GONE);
            }else if (chatMessage.msg_type.equals("video")){
                type = chatMessage.msg_type;
                video_url = chatMessage.media_path;
                Glide.with(itemView.getContext())
                        .load(chatMessage.media_path)
                        .into(binding.receiveImage);
                binding.textDateTime2.setText(chatMessage.dateTime);
                binding.textDateTime.setVisibility(View.GONE);
                binding.textMessage.setVisibility(View.GONE);
                binding.playImageRec.setVisibility(View.VISIBLE);
                binding.receiveImage.setVisibility(View.GONE);
            }
            if (receiverProfileImage != null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
