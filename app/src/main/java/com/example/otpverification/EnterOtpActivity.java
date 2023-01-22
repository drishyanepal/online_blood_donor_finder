package com.example.otpverification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.databinding.ActivityEnterOtpBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class EnterOtpActivity extends AppCompatActivity {
    ActivityEnterOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnterOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();


        binding.otpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = binding.phoneNumberEt.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    if (phoneNumber.length() == 10) {
                        binding.progressBarGet.setVisibility(View.VISIBLE);
                        binding.otpButton.setVisibility(View.GONE);
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+977" + phoneNumber, 60,
                                TimeUnit.SECONDS, EnterOtpActivity.this,
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    @Override
                                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                        binding.progressBarGet.setVisibility(View.GONE);
                                        binding.otpButton.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onVerificationFailed(@NonNull FirebaseException e) {
                                        binding.progressBarGet.setVisibility(View.GONE);
                                        binding.otpButton.setVisibility(View.VISIBLE);
                                        Toast.makeText(EnterOtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onCodeSent(@NonNull String otpSent, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                        binding.progressBarGet.setVisibility(View.GONE);
                                        binding.otpButton.setVisibility(View.VISIBLE);
                                        Intent intent = new Intent(EnterOtpActivity.this, VerifyActivity.class);
                                        intent.putExtra("phoneNumber", phoneNumber);
                                        intent.putExtra("otp", otpSent);
                                        startActivity(intent);
                                    }
                                }
                        );


                    } else {
                        Toast.makeText(EnterOtpActivity.this, "Incorrect phone number", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(EnterOtpActivity.this, "Enter mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(EnterOtpActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}