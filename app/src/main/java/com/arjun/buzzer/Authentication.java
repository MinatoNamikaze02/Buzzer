package com.arjun.buzzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class Authentication extends AppCompatActivity {

    TextView changeNumber;
    EditText getOtp;
    android.widget.Button verifyOtP;
    String enteredOtp;

    FirebaseAuth firebaseAuth;
    ProgressBar progressBarOfOtpAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        changeNumber = findViewById(R.id.changenumber);
        verifyOtP = findViewById(R.id.verifyotp);
        getOtp = findViewById(R.id.getotp);
        progressBarOfOtpAuth = findViewById(R.id.progressbarofotpauth);
        firebaseAuth = FirebaseAuth.getInstance();

        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Authentication.this, MainActivity.class);
                startActivity(intent);
            }
        });
        verifyOtP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredOtp = getOtp.getText().toString();
                if(enteredOtp.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter the OTP", Toast.LENGTH_LONG).show();

                }else{
                    progressBarOfOtpAuth.setVisibility(View.VISIBLE);

                    String codeRecieved = getIntent().getStringExtra("otp");
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeRecieved, enteredOtp);
                    signInWithCredentials(credential);
                }
            }
        });

    }

    private void signInWithCredentials(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBarOfOtpAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Authentication.this, SetProfile.class);
                    startActivity(intent);
                }else{
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        progressBarOfOtpAuth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login Failure", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}