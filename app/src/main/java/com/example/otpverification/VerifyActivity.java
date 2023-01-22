package com.example.otpverification;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.Models.DetailsModel;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.databinding.ActivityVerifyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {
    ActivityVerifyBinding binding;
    String otpObtained;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String userId = "";
    String email, password, intentSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneNumberGet.setText("+977 " + phoneNumber);
//        otpObtained = getIntent().getStringExtra("otp");
        moveToNext();

        binding.verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.progressBarVerify.setVisibility(View.VISIBLE);
                binding.verifyButton.setVisibility(View.GONE);

                String enteredOtp = binding.box1.getText().toString() +
                        binding.box2.getText().toString() +
                        binding.box3.getText().toString() +
                        binding.box4.getText().toString() +
                        binding.box5.getText().toString() +
                        binding.box6.getText().toString();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpObtained, enteredOtp);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete()) {
                            if(task.isSuccessful()){
                                String name = getIntent().getStringExtra("name");
                                String age = getIntent().getStringExtra("age");
                                String gender = getIntent().getStringExtra("gender");
                                String weight = getIntent().getStringExtra("weight");
                                String bloodGroup = getIntent().getStringExtra("bloodGroup");
                                String radioId = getIntent().getStringExtra("radioId");
                                String address = getIntent().getStringExtra("address");

                                UserModel model = new UserModel();
                                model.setName(name);
                                model.setAge(age);
                                model.setWeight(weight);
                                model.setBloodGroup(bloodGroup);
                                model.setRadioId(radioId);
                                model.setGender(gender);
                                model.setAddress(address);
                                model.setAvailableStatus("1");
                                model.setUserId(auth.getUid());
                                model.setPhoneNumber(phoneNumber);
                                database.getReference().child("Users").child(auth.getUid()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(VerifyActivity.this, "Verification successful, donor profile created", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(VerifyActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                finish();
                            }
                            else {
                                Toast.makeText(VerifyActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
        });

        binding.sendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.progressBarVerify.setVisibility(View.VISIBLE);
                binding.verifyButton.setVisibility(View.GONE);
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+977" + phoneNumber, 60,
                        TimeUnit.SECONDS, VerifyActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                binding.progressBarVerify.setVisibility(View.GONE);
                                binding.verifyButton.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                binding.progressBarVerify.setVisibility(View.GONE);
                                binding.verifyButton.setVisibility(View.VISIBLE);
                                Toast.makeText(VerifyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String newOtpSent, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                binding.progressBarVerify.setVisibility(View.GONE);
                                binding.verifyButton.setVisibility(View.VISIBLE);
                                otpObtained = newOtpSent;
                            }
                        }
                );
            }
        });
    }

    //editText boxes switching
    private void moveToNext() {
        binding.box1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    binding.box2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.box2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    binding.box3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.box3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    binding.box4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.box4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    binding.box5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.box5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty()) {
                    binding.box6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}