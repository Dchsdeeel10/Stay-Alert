package com.stp.stay_alert.adapater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stp.stay_alert.databinding.ItemContainerRecentConversionBinding;
import com.stp.stay_alert.listeners.ConversionListener;
import com.stp.stay_alert.models.ChatMessage;
import com.stp.stay_alert.models.User;
import com.stp.stay_alert.utilities.Constants;

import java.util.List;
import java.util.Objects;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>{

    private final List<ChatMessage> chatMessage;
    private final ConversionListener conversionListener;


    public RecentConversationsAdapter(List<ChatMessage> chatMessage, ConversionListener conversionListener) {
        this.chatMessage = chatMessage;
        this.conversionListener = conversionListener;
    }


    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,false
                )
        );
    }


    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessage.get(position));

    }

    @Override
    public int getItemCount() {
        return chatMessage.size();
    }

    class  ConversationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversationViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;

        }
        void setData(ChatMessage chatMessage){
            if(chatMessage.isActive != null){
                if(chatMessage.isActive.equals("1")){
                    binding.onlineImg.setVisibility(View.VISIBLE);
                }else{
                    binding.onlineImg.setVisibility((View.GONE));
                }

            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Constants.KEY_COLLECTION_USERS).document(chatMessage.conversionId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            binding.imageProfile.setImageBitmap(getConversationImage(document.getString(Constants.KEY_IMAGE)));
                            String message_name = document.getString(Constants.KEY_FULL_NAME);
                            char[] charArray = Objects.requireNonNull(message_name).toCharArray();
                            boolean foundSpace = true;
                            for(int i = 0; i < charArray.length; i++) {
                                if(Character.isLetter(charArray[i])) {
                                    if(foundSpace) {
                                        charArray[i] = Character.toUpperCase(charArray[i]);
                                        foundSpace = false;
                                    }
                                }
                                else {
                                    foundSpace = true;
                                }
                            }
                            message_name = String.valueOf(charArray);
                            binding.name.setText(message_name);
                            binding.textRecentMessage.setText(chatMessage.message);
                            binding.messageDate.setText(chatMessage.dateObject.toLocaleString());
                            String finalMessage_name = message_name;
                            binding.getRoot().setOnClickListener(v -> {
                                User user = new User();
                                user.id = chatMessage.conversionId;
                                user.name = finalMessage_name;
                                user.image = chatMessage.conversionImage;
                                user.token = chatMessage.receiverToken;
                                user.address = document.getString(Constants.KEY_ADDRESS);
                                user.contact = document.getString(Constants.KEY_CONTACT);
                                conversionListener.onConversionListener(user);
                            });
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);

    }
}
