package com.arjun.buzzer;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetProfile extends AppCompatActivity {
    private static final String TAG = SetProfile.class.getSimpleName();
    private CardView getUserImage;
    private ImageView getUserImageInImageView;
    private Uri imagepath;

    private EditText getUsername;
    private android.widget.Button saveProfile;

    private FirebaseAuth firebaseAuth;
    private String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private String ImageUriAccessToken;
    private FirebaseFirestore firebaseFirestore;

    ProgressBar progressBarOfSetProfile;
    ActivityResultLauncher <String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            imagepath = result;
            getUserImageInImageView.setImageURI(imagepath);

        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getUsername = findViewById(R.id.getusername);
        getUserImage = findViewById(R.id.getuserimage);
        getUserImageInImageView = findViewById(R.id.getuserimageinimageview);
        saveProfile = findViewById(R.id.saveProfile);
        progressBarOfSetProfile = findViewById(R.id.progressbarofsetProfile);

        getUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = getUsername.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter a Name", Toast.LENGTH_SHORT).show();
                }else if(imagepath==null){
                    Toast.makeText(getApplicationContext(), "Please Select an Image", Toast.LENGTH_SHORT).show();
                }else{
                    progressBarOfSetProfile.setVisibility(View.VISIBLE);
                    uploadNewUserData();
                    progressBarOfSetProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(SetProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    private void uploadNewUserData() {
        sendDataToRealTimeDatabase();

    }

    private void sendDataToRealTimeDatabase() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(Objects.requireNonNull(firebaseAuth.getUid()));

        UserProfile userProfile = new UserProfile(name);
        databaseReference.child("users").child(firebaseAuth.getUid()).setValue(userProfile);
        Toast.makeText(getApplicationContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
        sendImageToStorage();

    }

    private void sendImageToStorage() {
        StorageReference imageref = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Picture");
        Bitmap bitmap = null;
        //optimizing image

        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        //putting image to storage

        UploadTask uploadTask = imageref.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI has been recieved successfully", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFireStore();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI was not recieved", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image was not uploaded", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendDataToCloudFireStore() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("Image", ImageUriAccessToken);
        userData.put("UID", firebaseAuth.getUid());
        userData.put("Status", "Online");

        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data sent to Cloud Firestore successfully", Toast.LENGTH_SHORT).show();
                
            }
        });

    }
}