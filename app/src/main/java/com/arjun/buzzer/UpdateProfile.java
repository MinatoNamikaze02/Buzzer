package com.arjun.buzzer;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    private android.widget.Button buttonOfUpdateProfile;
    private EditText newUsername;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private ImageView getNewUserImageInImageView;
    private ImageButton backButtonOfUpdateProfile;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String URIAccessToken;
    private androidx.appcompat.widget.Toolbar toolBarOfUpdateProfile;
    private ProgressBar progressBarOfUpdateProfile;

    private Uri imagePath;
    Intent intent;
    String newName;
    ActivityResultLauncher <String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            imagePath = result;
            getNewUserImageInImageView.setImageURI(imagePath);

        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        toolBarOfUpdateProfile = findViewById(R.id.toolbarofupdateprofile);
        backButtonOfUpdateProfile = findViewById(R.id.backbuttonofupdateprofile);
        getNewUserImageInImageView = findViewById(R.id.getnewuserimageinimageview);
        progressBarOfUpdateProfile = findViewById(R.id.progressbarofupdateprofile);
        newUsername = findViewById(R.id.getnewusername);
        buttonOfUpdateProfile = findViewById(R.id.updateprofilebutton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        intent = getIntent();
        newName = intent.getStringExtra("username");
        newUsername.setText(newName);
        setSupportActionBar(toolBarOfUpdateProfile);
        backButtonOfUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getNewUserImageInImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContent.launch("image/*");
            }
        });
        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());
        buttonOfUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newName = newUsername.getText().toString();
                if(newName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Name is Empty", Toast.LENGTH_SHORT).show();

                }else if(imagePath!=null){
                    progressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName);
                    databaseReference.setValue(userProfile);
                    updateImageToStorage();
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
                    progressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    progressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    UserProfile userProfile = new UserProfile(newName);
                    databaseReference.setValue(userProfile);
                    updateNameOnCloudFireStore();
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    progressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(UpdateProfile.this, ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        storageReference = firebaseStorage.getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Picture").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                URIAccessToken = uri.toString();
                Picasso.get().load(uri).into(getNewUserImageInImageView);
            }
        });




    }

    private void updateNameOnCloudFireStore() {
        DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newName);
        userData.put("Image", URIAccessToken);
        userData.put("UID", firebaseAuth.getUid());
        userData.put("Status", "Online");
        Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
        documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Data sent to Cloud Firestore successfully", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void updateImageToStorage() {
        StorageReference imageref = storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Picture");
        Bitmap bitmap = null;
        //optimizing image

        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
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
                        URIAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "URI has been recieved successfully", Toast.LENGTH_SHORT).show();
                        updateNameOnCloudFireStore();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "URI was not recieved", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(getApplicationContext(), "Image is updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image was not updated" , Toast.LENGTH_SHORT).show();
            }
        });


    }
    @Override
    protected void onStop() {
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