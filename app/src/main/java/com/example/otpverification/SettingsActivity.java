package com.example.otpverification;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.Models.DetailsModel;
import com.example.otpverification.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String newPassword, oldPassword, email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database.getReference().child("Details").child(auth.getUid()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.changePasswordCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideViews();
                auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        Toast.makeText(SettingsActivity.this, "confirmation email has been sent to your email account. Please proceed from there", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "password change failed", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        binding.changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword = binding.newPassword.getText().toString();
                String newPasswordAgain = binding.newPasswordAgain.getText().toString();
                if (newPassword.equals(newPasswordAgain)) {
                    if (newPassword.length() >= 6) {
                        changePassword();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SettingsActivity.this, "Passwords not matching", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showViews() {
        binding.newPassword.setVisibility(View.VISIBLE);
        binding.newPasswordAgain.setVisibility(View.VISIBLE);
        binding.changePasswordButton.setVisibility(View.VISIBLE);
    }

    private void hideViews() {
        binding.newPassword.setVisibility(View.GONE);
        binding.newPasswordAgain.setVisibility(View.GONE);
        binding.changePasswordButton.setVisibility(View.GONE);
    }

    private void changePassword() {
        binding.progressBarUpdate.setVisibility(View.VISIBLE);
        binding.changePasswordButton.setVisibility(View.GONE);
        database.getReference().child("Details").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DetailsModel model = snapshot.getValue(DetailsModel.class);
                oldPassword = model.getPassword();
                email = model.getEmail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.progressBarUpdate.setVisibility(View.GONE);
                        binding.changePasswordButton.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                        hideViews();
                                        binding.newPassword.setText("");
                                        binding.newPasswordAgain.setText("");
                                        database.getReference().child("Details").child(auth.getUid()).child("password").setValue(newPassword);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Changing password failed2", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, "Changing password failed1", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, 4000);
    }
}