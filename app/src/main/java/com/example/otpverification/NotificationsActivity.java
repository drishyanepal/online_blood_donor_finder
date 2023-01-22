package com.example.otpverification;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.otpverification.Models.LoadingDialog;
import com.example.otpverification.Models.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    TextView textView;
    FusedLocationProviderClient fusedLocationProviderClient;
    String localityClient = "";
    String userIdIntent = "";
    String intentTitle = "";
    String senderReceiverId = "";
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        textView = findViewById(R.id.textView);
        getSupportActionBar().hide();
        loadingDialog = new LoadingDialog(NotificationsActivity.this);
        loadingDialog.startLoadingDialog();
        getSenderInfo();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(NotificationsActivity.this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * 3);
        locationRequest.setFastestInterval(1000 * 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                updateLocation(location);
            }
        };
//        if (intentTitle.equals("Location request")) {
//            startLocationUpdates();
//        }
        if (intentTitle.equals("Blood Donation Request")) {
            startDonationRequestAction();
        }
        if (intentTitle.equals("Donation request accepted")) {
            gotoDonorDetails();
        }
        if (intentTitle.equals("Donation request rejected")) {
            finish();
        }

    }

    private void gotoDonorDetails() {
        Intent intent = new Intent(NotificationsActivity.this, HandleNotificationActivity.class);
        Log.e("check", "1111111111111111111111111111111");
        database.getReference().child("Users").child(userIdIntent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    UserModel model = snapshot.getValue(UserModel.class);
                    intent.putExtra("name", model.getName());
                    intent.putExtra("age", model.getAge());
                    intent.putExtra("gender", model.getGender());
                    intent.putExtra("bloodGroup", model.getBloodGroup());
                    intent.putExtra("phoneNumber", model.getPhoneNumber());
                    intent.putExtra("weight", model.getWeight());
                    intent.putExtra("userId", userIdIntent);
//                    intent.putExtra("profilePic",model.getProfilePic());
                    intent.putExtra("address", model.getAddress());
                } else {
                    Toast.makeText(NotificationsActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("Position", ".........Second");
                intent.putExtra("intentSource", "notificationActivitySecond");
                startActivity(intent);
                loadingDialog.dismissDialog();
                finish();
            }
        }, 2000);

    }

    private void startDonationRequestAction() {
        Intent intent = new Intent(this, HandleNotificationActivity.class);
        database.getReference().child("Users").child(userIdIntent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //request from receiver
                if (snapshot.getValue() == null) {
                    database.getReference().child("tempUsers").child(userIdIntent).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() == null) {
                                intent.putExtra("name","N/A");
                                intent.putExtra("age", "N/A");
                                intent.putExtra("gender", "N/A");
                                intent.putExtra("weight", "N/A");
                                intent.putExtra("bloodGroup", "N/A");
                                intent.putExtra("phoneNumber", "N/A");
                                intent.putExtra("userId", userIdIntent);
                                intent.putExtra("address", "N/A");
                            } else {
                                database.getReference().child("tempUsers").child(userIdIntent).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserModel userModel = snapshot.getValue(UserModel.class);
                                        try {
                                            intent.putExtra("name", userModel.getName());
                                            intent.putExtra("age", userModel.getAge());
                                            intent.putExtra("gender", userModel.getGender());
                                            intent.putExtra("weight", userModel.getWeight());
                                            intent.putExtra("bloodGroup", userModel.getBloodGroup());
                                            intent.putExtra("phoneNumber", userModel.getPhoneNumber());
                                            intent.putExtra("userId", userIdIntent);
                                            intent.putExtra("address", userModel.getAddress());
//                            intent.putExtra("imageUri", userModel.getProfilePic());
                                        } catch (Exception e) {
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
                    //request from donor
                } else {
                    database.getReference().child("Users").child(userIdIntent).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel model = snapshot.getValue(UserModel.class);
                            intent.putExtra("name", model.getName());
                            intent.putExtra("age", model.getAge());
                            intent.putExtra("gender", model.getGender());
                            intent.putExtra("bloodGroup", model.getBloodGroup());
                            intent.putExtra("phoneNumber", model.getPhoneNumber());
                            intent.putExtra("weight", model.getWeight());
                            intent.putExtra("userId", userIdIntent);
                            intent.putExtra("address", model.getAddress());
//                          intent.putExtra("imageUri", model.getProfilePic());
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("Position", ".........Second");
                intent.putExtra("intentSource", "notificationActivityFirst");
                startActivity(intent);
                loadingDialog.dismissDialog();
                finish();
            }
        }, 3000);


    }

    private void getSenderInfo() {
        Log.e("where", ".......................getSenderInfoCalled");
        intentTitle = getIntent().getStringExtra("title");
        String intentText = getIntent().getStringExtra("userId");
        Log.e("UserID", ".................." + intentText);
        String arr[] = intentText.split("ID:");
        userIdIntent = arr[1];
        Log.e("userIdIntent", "........." + userIdIntent);
    }

    private void startLocationUpdates() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(NotificationsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                updateLocation(location);
            }
        });
    }

    private void updateLocation(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(NotificationsActivity.this);
            try {
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                database.getReference().child("Users").child(auth.getUid()).child("latitude").setValue(String.valueOf(latitude));
                database.getReference().child("Users").child(auth.getUid()).child("longitude").setValue(String.valueOf(longitude));
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                localityClient = addresses.get(0).getLocality();
                database.getReference().child("Users").child(auth.getUid()).child("liveLocation").setValue(localityClient);
                Toast.makeText(NotificationsActivity.this, "Location fetched: " + localityClient, Toast.LENGTH_SHORT).show();
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                addInstantLocationToDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addInstantLocationToDatabase() {
        senderReceiverId = userIdIntent + auth.getUid();
        DatabaseReference reference = database.getReference().child("InstantLocation").child(senderReceiverId);
        reference.setValue(localityClient).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {
                Toast.makeText(NotificationsActivity.this, "Added to database", Toast.LENGTH_SHORT).show();
            }
        });
        loadingDialog.dismissDialog();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
}