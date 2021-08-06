package com.arjun.buzzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Messages> messagesArrayList;
    int ITEM_SEND = 1;
    int ITEM_RECIEVE = 2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SEND){
            View view = LayoutInflater.from(context).inflate(R.layout.sendchatlayout, parent, false);
            return new SenderViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.recieverchatlayout, parent, false);
            return  new RecieverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Messages messages = messagesArrayList.get(position);
        if(holder.getClass()==SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.timeOfMessage.setText(messages.getCurrentTime());
        }else{
            RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
            viewHolder.textViewMessage.setText(messages.getMessage());
            viewHolder.timeOfMessage.setText(messages.getCurrentTime());
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderID())){
            return ITEM_SEND;
        }else{
            return ITEM_RECIEVE;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;
        TextView timeOfMessage;

        public SenderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendmessage);
            timeOfMessage = itemView.findViewById(R.id.timeofmessage);

        }
    }
    class RecieverViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;
        TextView timeOfMessage;

        public RecieverViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendmessage);
            timeOfMessage = itemView.findViewById(R.id.timeofmessage);

        }
    }


}
