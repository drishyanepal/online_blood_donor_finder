package com.example.otpverification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.Models.DetailsModel;
import com.example.otpverification.databinding.ActivitySignupPageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupPageActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivitySignupPageBinding binding;

    EditText email, password, rePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        email = findViewById(R.id.emailSignupPage);
        password = findViewById(R.id.passwordSignupPage);
        rePassword = findViewById(R.id.rePasswordSignupPage);


        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().isEmpty() || rePassword.getText().toString().isEmpty() || email.getText().toString().isEmpty()) {
                    Toast.makeText(SignupPageActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                String emailCheck = binding.emailSignupPage.getText().toString().trim();
                try {
                    String arr[] = emailCheck.split("@");
                    String emailProvider = arr[1];
                    if (emailProvider.equals("gmail.com") ||
                            emailProvider.equals("hotmail.com") ||
                            emailProvider.equals("yahoo.com") ||
                            emailProvider.equals("outlook.com") ||
                            emailProvider.equals("zoho.com") ||
                            emailProvider.equals("protonmail.com") ||
                            emailProvider.equals("yandex.com") ||
                            emailProvider.equals("live.com") ||
                            emailProvider.equals("inbox.com") ||
                            emailProvider.equals("aim.com") ||
                            emailProvider.equals("mail.com") ||
                            emailProvider.equals("walla.com") ||
                            emailProvider.equals("netzero.net") ||
                            emailProvider.equals("aol.com") ||
                            emailProvider.equals("msn.com")) {

                        if (password.getText().toString().equals(rePassword.getText().toString())) {
                            if (password.getText().toString().length() > 5) {
                                String passwordText = password.getText().toString();
                                String regex = "^(?=.*[0-9])"
                                        + "(?=.*[a-z])(?=.*[A-Z])"
                                        + "(?=.*[@!#$%^&+*/=])"
                                        + "(?=\\S+$).{5,20}$";

                                // Compile the ReGex
                                Pattern p = Pattern.compile(regex);
                                Matcher m = p.matcher(passwordText);
                                if (m.matches() == true) {
                                    binding.progressBarGet.setVisibility(View.VISIBLE);
                                    binding.signUpButton.setVisibility(View.GONE);
                                    auth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                                @Override
                                                public void onSuccess(@NonNull AuthResult authResult) {
                                                    binding.progressBarGet.setVisibility(View.GONE);
                                                    binding.signUpButton.setVisibility(View.VISIBLE);
                                                    Toast.makeText(SignupPageActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                                    addToDatabase();
                                                    Intent intent = new Intent(SignupPageActivity.this, LoginPageActivity.class);
                                                    startActivity(intent);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignupPageActivity.this, "SignUp failed", Toast.LENGTH_SHORT).show();
                                            binding.progressBarGet.setVisibility(View.GONE);
                                            binding.signUpButton.setVisibility(View.VISIBLE);
                                        }
                                    });
                                } else {
                                    Toast.makeText(SignupPageActivity.this, "Invalid password alert: " +
                                            "Password must be combination of uppercase, lowercase, digits and special characters without any whitespaces", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(SignupPageActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignupPageActivity.this, "passwords not matching", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignupPageActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(SignupPageActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupPageActivity.this, LoginPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void addToDatabase() {
        DetailsModel model = new DetailsModel(binding.emailSignupPage.getText().toString().trim(),
                binding.passwordSignupPage.getText().toString().trim(),
                auth.getUid());
        DatabaseReference reference = database.getReference().child("Details").child(auth.getUid());
        reference.setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }
}