package com.example.otpverification;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.Models.DetailsModel;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.databinding.ActivityLoginPageBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class LoginPageActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivityLoginPageBinding binding;
    String userId = "";
    String phoneNumber = "";
    int emailValidation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.emailLoginPage.getText().toString().isEmpty() || binding.passwordLoginPage.getText().toString().isEmpty()) {
                    Toast.makeText(LoginPageActivity.this, "No field can be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.progressBarGet.setVisibility(View.VISIBLE);
                binding.loginButton.setVisibility(View.GONE);
                auth.signInWithEmailAndPassword(binding.emailLoginPage.getText().toString().trim(),
                        binding.passwordLoginPage.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(@NonNull AuthResult authResult) {
                                binding.progressBarGet.setVisibility(View.GONE);
                                binding.loginButton.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
                                Toast.makeText(LoginPageActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBarGet.setVisibility(View.GONE);
                        binding.loginButton.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginPageActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPageActivity.this, SignupPageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.forgetPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailLoginPage.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(LoginPageActivity.this, "Please enter your email first", Toast.LENGTH_SHORT).show();
                } else {
                    auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void unused) {
                            Toast.makeText(LoginPageActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginPageActivity.this, "Verification email sending failed", Toast.LENGTH_SHORT).show();
                        }
                    });

//                    binding.forgetPasswordTV.setVisibility(View.GONE);
//                    binding.progressBarForget.setVisibility(View.VISIBLE);
//                    database.getReference().child("Details").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                                if (emailValidation == 0) {
//                                    DetailsModel model = dataSnapshot.getValue(DetailsModel.class);
//                                    if (model.getEmail().equals(email)) {
//                                        userId = model.getUserId();
//                                        emailValidation = 1;
//                                    }
//                                }
//                            }
//                            if (emailValidation == 0) {
//                                Toast.makeText(LoginPageActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
//                                binding.forgetPasswordTV.setVisibility(View.VISIBLE);
//                                binding.progressBarForget.setVisibility(View.GONE);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (emailValidation == 1) {
//                                findUserPhoneAndVerify();
//                            }
//                        }
//                    }, 4000);
                }
            }
        });

    }

    private void findUserPhoneAndVerify() {
        database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //User ma xa
                    UserModel modelSecond = snapshot.getValue(UserModel.class);
                    phoneNumber = modelSecond.getPhoneNumber();
                } else {
                    //tempUser ma xa
                    database.getReference().child("tempUsers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel model = snapshot.getValue(UserModel.class);
                            phoneNumber = model.getPhoneNumber();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (phoneNumber != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startVerification();
                }
            }, 3000);
        }
    }

    private void startVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+977" + phoneNumber.trim(), 60,
                TimeUnit.SECONDS, LoginPageActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(LoginPageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String otpSent, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        Intent intent = new Intent(LoginPageActivity.this, VerifyActivity.class);
                        Toast.makeText(LoginPageActivity.this, "Code sent successfully", Toast.LENGTH_SHORT).show();
                        intent.putExtra("userId", userId);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("otp", otpSent);
                        intent.putExtra("intentSource", "LoginPageActivity");
                        startActivity(intent);
                        binding.forgetPasswordTV.setVisibility(View.VISIBLE);
                        binding.progressBarForget.setVisibility(View.GONE);
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginPageActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}