package com.example.otpverification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.otpverification.Models.LoadingDialog;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.databinding.ActivityDonorProfileBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DonorProfileActivity extends AppCompatActivity {
    ActivityDonorProfileBinding binding;
    RadioButton radioButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String photoUrl;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        bloodGroupList();

        loadingDialog = new LoadingDialog(DonorProfileActivity.this);
        loadingDialog.startLoadingDialog();

        DatabaseReference rootRef = database.getReference().child("Users").child(auth.getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(DonorProfileActivity.this, "You are already registered as donor. Please check out user profile", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAllFieldsFilled();
            }
        });


        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData() != null) {
            Uri imageUri = data.getData();
            binding.profilePicture.setImageURI(imageUri);
            final StorageReference reference = storage.getReference().child("profilePictures").child(auth.getUid());
            reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(DonorProfileActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(@NonNull Uri uri) {
                            database.getReference().child("Users").child(auth.getUid()).child("profilePic")
                                    .setValue(uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DonorProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startVerification() {
        String phoneNumber = binding.phone.getText().toString().trim();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+977" + phoneNumber, 60,
                TimeUnit.SECONDS, DonorProfileActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        loadingDialog.dismissDialog();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(DonorProfileActivity.this, "Verification failed", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }

                    @Override
                    public void onCodeSent(@NonNull String otpSent, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        loadingDialog.dismissDialog();
                        Intent intent = new Intent(DonorProfileActivity.this, VerifyActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("otp", otpSent);

                        int radioId = binding.radioGroup.getCheckedRadioButtonId();
                        radioButton = findViewById(radioId);
                        //Data in String form
                        String name = binding.name.getText().toString();
                        String age = binding.age.getText().toString();
                        String weight = binding.weight.getText().toString();
                        String bloodGroup = binding.autoComplete.getText().toString();
                        String gender = radioButton.getText().toString();
                        String address = binding.address.getText().toString();

                        intent.putExtra("name", name);
                        intent.putExtra("age", age);
                        intent.putExtra("weight", weight);
                        intent.putExtra("bloodGroup", bloodGroup);
                        intent.putExtra("gender", gender);
                        intent.putExtra("radioId", String.valueOf(radioId));
                        intent.putExtra("address", address);
                        loadingDialog.dismissDialog();
                        startActivity(intent);

                    }
                }
        );
    }

    private void checkAllFieldsFilled() {
        if (!binding.name.getText().toString().isEmpty() &&
                !binding.age.getText().toString().isEmpty() &&
                !binding.weight.getText().toString().isEmpty() &&
                !binding.autoComplete.getText().toString().isEmpty() &&
                !binding.phone.getText().toString().isEmpty() &&
                !binding.address.getText().toString().isEmpty() &&
                (binding.maleButton.isChecked() || binding.femaleButton.isChecked() || binding.otherButton.isChecked())) {
            int age = Integer.parseInt(binding.age.getText().toString().trim());
            if (age < 60 && age > 17) {
                int weight = Integer.parseInt(binding.weight.getText().toString().trim());
                if (weight > 44 && weight < 150) {
                    if (binding.phone.getText().toString().length() != 10) {
                        Toast.makeText(DonorProfileActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        checkDonorEligibility();
                    }
                } else {
                    Toast.makeText(this, "Weight restriction: Sorry, you are not eligible for blood donation", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Age restriction: Sorry, you are not eligible for blood donation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DonorProfileActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDonorEligibility() {
        if ((binding.yes.isChecked() || binding.no.isChecked()) && (binding.yesD.isChecked() || binding.noD.isChecked())) {
            if (binding.yes.isChecked() ||
                    binding.yesD.isChecked() ||
                    binding.cancer.isChecked() ||
                    binding.heartDisease.isChecked() ||
                    binding.hiv.isChecked() ||
                    binding.hepatitis.isChecked() ||
                    binding.hemophilia.isChecked() ||
                    binding.diabetes.isChecked() ||
                    binding.liver.isChecked() ||
                    binding.polycythemia.isChecked() ||
                    binding.asthma.isChecked() ||
                    binding.endocrine.isChecked() ||
                    binding.pregnant.isChecked()) {
                Toast.makeText(DonorProfileActivity.this, "Sorry, your are not eligible for donation", Toast.LENGTH_SHORT).show();
            } else {
                loadingDialog.startLoadingDialog();
                submitAction();
                //         startVerification();
            }
        } else {
            Toast.makeText(DonorProfileActivity.this, "Yes/No fields can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAction() {
        //send to database
        int radioId = binding.radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);
        String name = binding.name.getText().toString();
        String age = binding.age.getText().toString();
        String weight = binding.weight.getText().toString();
        String bloodGroup = binding.autoComplete.getText().toString();
        String phoneNumber = binding.phone.getText().toString();
        String gender = radioButton.getText().toString();
        String address = binding.address.getText().toString();

        UserModel model = new UserModel();
        model.setAvailableStatus("1");
        model.setRadioId(String.valueOf(radioId));
        model.setName(name);
        model.setAge(age);
        //model.setProfilePic(photoUrl);
        model.setWeight(weight);
        model.setBloodGroup(bloodGroup);
        model.setGender(gender);
        model.setUserId(auth.getUid());
        model.setAddress(address);
        model.setPhoneNumber(phoneNumber);
        DatabaseReference reference = database.getReference().child("Users").child(auth.getUid());
        reference.setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DonorProfileActivity.this, "You're are successfully registered as Donor", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DonorProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(DonorProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bloodGroupList() {
        ArrayList<String> bloodGroup = new ArrayList<>();
        bloodGroup.add("A-positive (A+)");
        bloodGroup.add("A-negative (A-)");
        bloodGroup.add("B-positive (B+)");
        bloodGroup.add("B-negative (B-)");
        bloodGroup.add("AB-positive (AB+)");
        bloodGroup.add("AB-negative (AB-)");
        bloodGroup.add("O-positive (O+)");
        bloodGroup.add("O-negative (O-)");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dropdown_list, bloodGroup);
        binding.autoComplete.setAdapter(adapter);
    }

}