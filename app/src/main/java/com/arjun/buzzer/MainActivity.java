package com.arjun.buzzer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText getPhoneNumber;
    android.widget.Button sendOtp;
    CountryCodePicker countryCodePicker;
    String countryCode;
    String phoneNo;

    FirebaseAuth firebaseAuth;
    ProgressBar progressBarOfMain;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        getPhoneNumber = findViewById(R.id.getphonenumber);
        sendOtp = findViewById(R.id.sendotpbutton);
        countryCodePicker = findViewById(R.id.countrycodepicker);
        progressBarOfMain = findViewById(R.id.progressbarofmain);

        firebaseAuth = FirebaseAuth.getInstance();
        countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();

        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
            }
        });

        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                number = getPhoneNumber.getText().toString();
                if (number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter your Number", Toast.LENGTH_LONG).show();

                } else if (number.length() < 10) {
                    Toast.makeText(getApplicationContext(), "Please Enter Valid Number", Toast.LENGTH_LONG).show();
                } else {
                    progressBarOfMain.setVisibility(View.VISIBLE);
                    phoneNo = countryCode + number;
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(phoneNo)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });


        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(), "Verifying OTP automatically", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), "OTP SEND FAILED", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(getApplicationContext(), "OTP has been sent", Toast.LENGTH_SHORT).show();
                progressBarOfMain.setVisibility(View.INVISIBLE);
                codeSent = s;
                Intent intent = new Intent(MainActivity.this, Authentication.class);
                intent.putExtra("otp", codeSent);
                startActivity(intent);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
