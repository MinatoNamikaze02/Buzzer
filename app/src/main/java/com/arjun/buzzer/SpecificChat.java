package com.arjun.buzzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {
    EditText getMessage;
    ImageButton sendMessageButton;
    CardView sendMessage;
    androidx.appcompat.widget.Toolbar toolbarOfSpecificChat;
    ImageView imageViewOfSpecificUser;
    TextView nameOfSpecificUser;
    private String enteredMessage;
    Intent intent;
    String recieverName, senderName, recieverUID, senderUID;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String senderRoom, recieverRoom;
    ImageButton backButtonOfSpecificChat;
    RecyclerView messageRecyclerView;
    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);
        getMessage = findViewById(R.id.getmessage);
        sendMessage = findViewById(R.id.cardviewofsendmessage);
        sendMessageButton = findViewById(R.id.imageviewofsendmessage);
        toolbarOfSpecificChat = findViewById(R.id.toolbarofspecificchat);
        nameOfSpecificUser = findViewById(R.id.nameofspecificuser);
        imageViewOfSpecificUser = findViewById(R.id.specificuserimageinimageview);
        backButtonOfSpecificChat = findViewById(R.id.backbuttonofspecificchat);
        messagesArrayList = new ArrayList<>();
        messageRecyclerView = findViewById(R.id.recyclerviewofspecificchat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        messageRecyclerView.setAdapter(messagesAdapter);

        intent = getIntent();

        setSupportActionBar(toolbarOfSpecificChat);
        toolbarOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Toolbar clicked", Toast.LENGTH_SHORT).show();

            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");
        senderUID = firebaseAuth.getUid();
        recieverUID = intent.getStringExtra("recieverUID");
        recieverName = intent.getStringExtra("name");
        senderRoom = senderUID + recieverUID;
        recieverRoom = recieverUID + senderUID;
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Chats").child(senderRoom).child("Messages");
        messagesAdapter = new MessagesAdapter(SpecificChat.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);

                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        backButtonOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        nameOfSpecificUser.setText(recieverName);
        String uri = intent.getStringExtra("ImageURI");
        if(uri.isEmpty()){
            Toast.makeText(getApplicationContext(), "Null is recieved", Toast.LENGTH_SHORT).show();

        }else{
            Picasso.get().load(uri).into(imageViewOfSpecificUser);
        }


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredMessage = getMessage.getText().toString();
                if(enteredMessage.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Message is empty", Toast.LENGTH_SHORT).show();
                }else{
                    Date date = new Date();
                    currentTime = simpleDateFormat.format(calendar.getTime());
                    Messages messages = new Messages(enteredMessage, firebaseAuth.getUid(), date.getTime(), currentTime);
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("Chats").child(senderRoom).child("Messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            firebaseDatabase.getReference().child("Chats").child(recieverRoom).child("Messages").push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(), "Messages sent to Database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    getMessage.setText(null);
                }
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!=null){
            messagesAdapter.notifyDataSetChanged();
        }
    }
}