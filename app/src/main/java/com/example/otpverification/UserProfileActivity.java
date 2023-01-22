package com.example.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.otpverification.Models.LoadingDialog;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {
    ActivityUserProfileBinding binding;
    RadioButton radioButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String bloodGroupForEdit, phoneNumberForVerification;
    String latitude;
    String longitude;
    String liveLocation;
    int userType;
    //1->donor,0->non-donor,2-> new user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.userName.setFocusable(false);
        binding.age.setFocusable(false);
        binding.weight.setFocusable(false);
//        binding.autoComplete.setFocusable(false);
        binding.phoneNumber.setFocusable(false);
        binding.maleButton.setClickable(false);
        binding.femaleButton.setClickable(false);
        binding.otherButton.setClickable(false);
        binding.location.setFocusable(false);

        final LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.startLoadingDialog();

        DatabaseReference rootRef = database.getReference().child("Users").child(auth.getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    int radioId;
                    userType = 1;
                    UserModel model = snapshot.getValue(UserModel.class);
//                    photoUrl = model.getProfilePic();
                    binding.userName.setText(model.getName());
                    binding.age.setText(model.getAge());
                    binding.weight.setText(model.getWeight());
                    binding.phoneNumber.setText(model.getPhoneNumber());
                    phoneNumberForVerification = model.getPhoneNumber();
                    try {
                        latitude = model.getLatitude();
                        longitude = model.getLongitude();
                        liveLocation = model.getLiveLocation();
                    } catch (Exception e) {
                    }
                    binding.location.setText(model.getAddress());
//                    binding.bloodGroupsAdapter.setVisibility(View.GONE);
                    binding.bloodGroupTV.setVisibility(View.VISIBLE);
                    binding.bloodGroupTV.setText(model.getBloodGroup());
                    bloodGroupForEdit = model.getBloodGroup();
                    try {
                        radioId = Integer.parseInt(model.getRadioId());
                        RadioButton radioButton = findViewById(radioId);
                        radioButton.setChecked(true);
                    } catch (Exception e) {
                    }
                    if (model.getAvailableStatus().equals("1")) {
                        binding.activeSwitch.setChecked(true);
                    }
                    loadingDialog.dismissDialog();
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadingDialog.dismissDialog();
//                        }
//                    }, 3000);

                } else {
                    database.getReference().child("tempUsers").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                loadingDialog.dismissDialog();
                                userType = 0;
                                int radioId;
                                UserModel model = snapshot.getValue(UserModel.class);
                                try {
                                    radioId = Integer.parseInt(model.getRadioId());
                                    RadioButton radioButton = findViewById(radioId);
                                    radioButton.setChecked(true);
                                } catch (Exception e) {
                                }

//                    photoUrl = model.getProfilePic();
                                binding.userName.setText(model.getName());
                                binding.age.setText(model.getAge());
                                binding.weight.setText(model.getWeight());
                                binding.phoneNumber.setText(model.getPhoneNumber());
                                phoneNumberForVerification = model.getPhoneNumber();
                                binding.location.setText(model.getAddress());
//                                binding.bloodGroupsAdapter.setVisibility(View.GONE);
                                binding.bloodGroupTV.setVisibility(View.VISIBLE);
                                binding.bloodGroupTV.setText(model.getBloodGroup());
                                bloodGroupForEdit = model.getBloodGroup();

                            } else {
                                userType = 2;
                                loadingDialog.dismissDialog();
                                binding.editImage.setVisibility(View.GONE);
                                binding.bloodGroupTV.setVisibility(View.GONE);
//                                binding.bloodGroupsAdapter.setVisibility(View.VISIBLE);
                                binding.submitButton.setVisibility(View.VISIBLE);
                                bloodGroupList();

                                binding.userName.setFocusableInTouchMode(true);
                                binding.age.setFocusableInTouchMode(true);
                                binding.weight.setFocusableInTouchMode(true);
                                binding.phoneNumber.setFocusableInTouchMode(true);
                                binding.location.setFocusableInTouchMode(true);
                                binding.maleButton.setClickable(true);
                                binding.femaleButton.setClickable(true);
                                binding.otherButton.setClickable(true);
                            }
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

        binding.activeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.activeSwitch.isChecked()) {
                    binding.available.setVisibility(View.VISIBLE);
                    binding.notAvailable.setVisibility(View.GONE);
                } else {
                    binding.available.setVisibility(View.GONE);
                    binding.notAvailable.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.submitButton.setVisibility(View.VISIBLE);
                binding.editImage.setVisibility(View.GONE);
                if (userType == 1) {
                    binding.activeSwitch.setVisibility(View.VISIBLE);
                    if (binding.activeSwitch.isChecked()) {
                        binding.available.setVisibility(View.VISIBLE);
                    } else {
                        binding.notAvailable.setVisibility(View.VISIBLE);
                    }
                }
                binding.bloodGroupTV.setVisibility(View.GONE);
//                binding.bloodGroupsAdapter.setVisibility(View.VISIBLE);
                bloodGroupList();

                binding.userName.setFocusableInTouchMode(true);
                binding.age.setFocusableInTouchMode(true);
                binding.weight.setFocusableInTouchMode(true);
//                binding.phoneNumber.setFocusableInTouchMode(true);
                binding.location.setFocusableInTouchMode(true);
                binding.maleButton.setClickable(true);
                binding.femaleButton.setClickable(true);
                binding.otherButton.setClickable(true);

            }
        });

        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAllFieldsFilled();
            }
        });
    }

    private void submitAction() {
//        if(!binding.phoneNumber.getText().toString().trim().equals(phoneNumberForVerification)){
//            Toast.makeText(this, "Please verify phone number first", Toast.LENGTH_SHORT).show();
//            binding.verifyPhone.setVisibility(View.VISIBLE);
//            return;
//        }
        int radioId = binding.radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);
        //Data in String form
        String name = binding.userName.getText().toString();
        String age = binding.age.getText().toString();
        String weight = binding.weight.getText().toString();
        String bloodGroup = binding.autoComplete.getText().toString();
        String gender = radioButton.getText().toString();
        String phoneNumber = binding.phoneNumber.getText().toString();
        String address = binding.location.getText().toString();

        //send to database
        UserModel model = new UserModel();
        if (userType == 1) {
            if (binding.activeSwitch.isChecked()) {
                model.setAvailableStatus("1");
            } else {
                model.setAvailableStatus("0");
            }
            model.setLatitude(latitude);
            model.setLongitude(longitude);
            model.setLiveLocation(liveLocation);
        }
        model.setRadioId(String.valueOf(radioId));
        model.setName(name);
        model.setAge(age);
//        model.setProfilePic(photoUrl);
        model.setWeight(weight);
        model.setBloodGroup(bloodGroup);
        model.setGender(gender);
        model.setUserId(auth.getUid());
        model.setAddress(address);
        model.setPhoneNumber(phoneNumber);
        DatabaseReference reference;
        if (userType == 1) {
            reference = database.getReference().child("Users").child(auth.getUid());
        } else {
            reference = database.getReference().child("tempUsers").child(auth.getUid());
        }
        reference.setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UserProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(UserProfileActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkAllFieldsFilled() {
        if (!binding.userName.getText().toString().isEmpty() &&
                !binding.age.getText().toString().isEmpty() &&
                !binding.weight.getText().toString().isEmpty() &&
                !binding.autoComplete.getText().toString().isEmpty() &&
                !binding.phoneNumber.getText().toString().isEmpty() &&
                !binding.location.getText().toString().isEmpty() &&
                (binding.maleButton.isChecked() || binding.femaleButton.isChecked() || binding.otherButton.isChecked())) {
            int age = Integer.parseInt(binding.age.getText().toString().trim());
            if (age < 60 && age > 17) {
                int weight = Integer.parseInt(binding.weight.getText().toString().trim());
                if (weight > 44 && weight < 150) {
                    if (binding.phoneNumber.getText().toString().length() != 10) {
                        Toast.makeText(UserProfileActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    } else {
                        submitAction();
                    }
                } else {
                    Toast.makeText(this, "Weight restriction: Sorry, you are not eligible for blood donation", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Age restriction: Sorry, you are not eligible for blood donation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(UserProfileActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
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
        if (userType != 2) {
            for (int i = 0; i < bloodGroup.size(); i++) {
                if (bloodGroupForEdit.equals(bloodGroup.get(i))) {
                    binding.autoComplete.setText(bloodGroupForEdit);
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dropdown_list, bloodGroup);
        binding.autoComplete.setAdapter(adapter);
    }
}