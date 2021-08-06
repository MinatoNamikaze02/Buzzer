package com.arjun.buzzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

public class ProfileActivity extends AppCompatActivity {


    private static final String TAG = ProfileActivity.class.getSimpleName();
    EditText viewUsername;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    TextView updateProfile;
    FirebaseFirestore firebaseFirestore;
    ImageView viewUserImageInImageView;
    ImageButton backButtonOfViewProfile;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private String URIAccessToken;
    androidx.appcompat.widget.Toolbar toolBarOfViewProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        viewUserImageInImageView = findViewById(R.id.viewuserimageinimageview);
        viewUsername = findViewById(R.id.viewusername);
        updateProfile = findViewById(R.id.movetoupdateprofile);
        firebaseFirestore = FirebaseFirestore.getInstance();
        toolBarOfViewProfile = findViewById(R.id.toolbarofviewprofile);
        backButtonOfViewProfile = findViewById(R.id.backbuttonofviewprofile);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();


        setSupportActionBar(toolBarOfViewProfile);

        backButtonOfViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Picture").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                URIAccessToken = uri.toString();
                Picasso.get().load(uri).into(viewUserImageInImageView);

            }
        });

        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Task failed"+task.getException(), Toast.LENGTH_SHORT).show();
                }else{
                    DocumentSnapshot documentSnapshot = task.getResult();
                    viewUsername.setText(String.valueOf(documentSnapshot.get("name")));
                    Log.d(TAG, task.getResult().toString());
                    Log.d(TAG, firebaseAuth.getUid());
                    Toast.makeText(getApplicationContext(), "Username loaded!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ProfileActivity.this, UpdateProfile.class);
                intent.putExtra("username", viewUsername.getText().toString());
                startActivity(intent);

            }
        });






    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.onStop();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        documentReference.update("Status", "Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Offline", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        documentReference.update("Status", "Online").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "User is Online", Toast.LENGTH_SHORT).show();
            }
        });
    }
}