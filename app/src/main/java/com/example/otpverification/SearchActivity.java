package com.example.otpverification;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.otpverification.Adapters.listAdapter;
import com.example.otpverification.Models.LoadingDialog;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.databinding.ActivitySearchBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    public SearchActivity() {
    }

    ArrayList<UserModel> list;
    ActivitySearchBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String bloodGroup = "";
    String locality = "";
    String tokenString = "";
    String instantLocation = "";
    String latitude, longitude;
    //1 --> not on donor profile
    //2 ---> not on any user profile
    int userStat = 1;
    String title = "Location request";
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    String deviceId;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

//        LoadingDialog loadingDialog = new LoadingDialog(SearchActivity.this);
//        loadingDialog.startLoadingDialog();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        binding.nameTV.setVisibility(View.GONE);
        binding.phoneNumberTV.setVisibility(View.GONE);
        //check donor or receiver
//        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.getValue() == null) {
//                    userStat = 1;
//                    database.getReference().child("tempUsers").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.getValue() == null) {
//                                binding.nameTV.setVisibility(View.VISIBLE);
//                                binding.phoneNumberTV.setVisibility(View.VISIBLE);
//                                userStat = 2;
//                                loadingDialog.dismissDialog();
//                            } else {
//                                loadingDialog.dismissDialog();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//                } else {
//                    loadingDialog.dismissDialog();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        list = new ArrayList<>();

        binding.clickToSee.setVisibility(View.GONE);
        bloodGroupList();
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * 3);
        locationRequest.setFastestInterval(1000 * 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                updateUI(location);
            }
        };

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bloodGroup = binding.autoCompleteSearch.getText().toString();
                if (binding.locationSearch.getText().toString().isEmpty()) {
                    Toast.makeText(SearchActivity.this, "Please, fetch your location first..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userStat == 2) {
                    if (binding.nameTV.getText().toString().isEmpty() || binding.phoneNumberTV.getText().toString().isEmpty()) {
                        Toast.makeText(SearchActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (binding.phoneNumberTV.getText().toString().length() != 10) {
                        Toast.makeText(SearchActivity.this, "Incorrect phone number", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        //add to database (tempUsers)
                        UserModel modelInstance = new UserModel();
                        modelInstance.setName(binding.nameTV.getText().toString().trim());
                        modelInstance.setPhoneNumber(binding.phoneNumberTV.getText().toString().trim());
                        modelInstance.setAddress(locality);
                        modelInstance.setBloodGroup(bloodGroup);
                        modelInstance.setUserId(auth.getUid());
                        modelInstance.setLatitude(latitude);
                        modelInstance.setLongitude(longitude);
                        database.getReference().child("tempUsers").child(auth.getUid()).setValue(modelInstance);
                    }
                }
                list.clear();
                binding.searchButton.setVisibility(View.GONE);
                binding.progressBarSearch.setVisibility(View.VISIBLE);

                //findFromDatabase();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkLocationAndAddToList();
                    }
                }, 0000);

            }
        });

        binding.locationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
            }
        });
        binding.clickToSee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchButton.setVisibility(View.GONE);
                binding.clickToSee.setVisibility(View.GONE);
                checkLocationAndAddToList();
            }
        });
    }

    public double distanceCalculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radian
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        //d = 2 * r *arcsin(√(haversine(Δlat) ) + cos(lat1)cos(lat2)*haversine(Δlon) ) )
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLon / 2), 2);
        double c = Math.asin(Math.sqrt(a));
        return 2 * 6371 * c;
    }

    private void updateUI(Location location) {
        if (location != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Geocoder geocoder = new Geocoder(SearchActivity.this);
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                locality = addresses.get(0).getLocality();
                binding.locationSearch.setText(locality);
            } catch (IOException e) {
                e.printStackTrace();
            }
            database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        database.getReference().child("Users").child(auth.getUid()).child("latitude").setValue(latitude);
                        database.getReference().child("Users").child(auth.getUid()).child("longitude").setValue(longitude);
                        database.getReference().child("Users").child(auth.getUid()).child("liveLocation").setValue(locality);
                        Log.e("Location", "............." + locality);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(SearchActivity.this, "location fetching...", Toast.LENGTH_SHORT).show();
        }

    }

    private void startLocationUpdates() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(@NonNull Location location) {
                updateUI(location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SearchActivity.this, "location fetching failed", Toast.LENGTH_SHORT).show();
                binding.locationSearch.setText("Madhyapur Thimi");
                locality = "Madhyapur Thimi";
                latitude = "27.6757";
                longitude = "85.3665";
            }
        });
    }

    private void findFromDatabase() {
        //search for same bloodGroup users
        //send them locationRequest notification
        Log.e("check", "............FindFromDatabase()called ");
        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UserModel model = dataSnapshot.getValue(UserModel.class);
                    //condition: user not self
                    if (!auth.getUid().equals(model.getUserId())) {
                        //check bloodGroup match
                        if (model.getBloodGroup().equals(bloodGroup)) {
                            //check donor availability
                            if (model.getAvailableStatus().equals("1")) {
                                database.getReference().child("Token").child(model.getUserId()).child("deviceToken")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                try {
                                                    tokenString = snapshot.getValue(String.class);
                                                    sendLocationFetchSignal();
                                                    Log.e("Usertoken", "..........." + tokenString);//                                               sendLocationRequestNotification();
                                                } catch (Exception e) {
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkLocationAndAddToList() {
        Double lat = Double.parseDouble(latitude);
        Double lon = Double.parseDouble(longitude);
        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        UserModel model = dataSnapshot.getValue(UserModel.class);
                        deviceId = database.getReference().child("Token").child(model.getUserId()).child("deviceToken").toString();
                        String donorUserId = model.getUserId();
                        if (!donorUserId.equals(auth.getUid()) &&
                                model.getBloodGroup().equals(bloodGroup) &&
                                model.getAvailableStatus().equals("1")) {
                            fetchDonorLiveLocation(donorUserId);
                            Double latD = Double.parseDouble(model.getLatitude());
                            Double lonD = Double.parseDouble(model.getLongitude());
                            Double distance = distanceCalculateHaversine(lat, lon, latD, lonD);
                            if (distance < 10) {
                                model.setDistance(distance);
                                list.add(model);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Location fetch error", "failed to fetch donor's location");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("List count", ".....pachaadi......" + list.size());
                if (list.isEmpty()) {
                    String toastText = "Sorry, nobody of " + bloodGroup + " is available";
                    Toast.makeText(SearchActivity.this, toastText, Toast.LENGTH_SHORT).show();
                }
                binding.searchButton.setVisibility(View.VISIBLE);
                binding.progressBarSearch.setVisibility(View.GONE);

                if (!list.isEmpty()) {
                    Collections.sort(list);
                    listAdapter adapter = new listAdapter(SearchActivity.this, list, locality, lat, lon);
                    binding.recyclerView.setAdapter(adapter);
                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                    fadeOutViews();
                }
            }
        }, 3000);

    }

    private void fetchDonorLiveLocation(String donorUserId) {
//        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        boolean gpsEnabled = false;
//        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!gpsEnabled) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(intent);
//        }
        String donorDeviceToken = database.getReference().child("Token").child(donorUserId).child("deviceToken").toString();
        try {
            if (deviceId.equals(donorDeviceToken)) {
                if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
                    }
                }
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(@NonNull Location location) {
                        database.getReference().child("Users").child(donorUserId).setValue(location.getLatitude());
                        database.getReference().child("Users").child(donorUserId).setValue(location.getLatitude());
                    }
                });
            }
        } catch (Exception e) {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dropdown_list, bloodGroup);
        binding.autoCompleteSearch.setAdapter(adapter);
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

    private void sendLocationFetchSignal() {
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(tokenString,
                "", "", getApplicationContext(), SearchActivity.this);
        notificationsSender.SendNotifications();
    }

    private void fadeOutViews() {
        binding.nameTV.setVisibility(View.GONE);
        binding.phoneNumberTV.setVisibility(View.GONE);
        binding.dropDown.setVisibility(View.GONE);
        binding.locationSearch.setVisibility(View.GONE);
        binding.searchButton.setVisibility(View.GONE);
    }
}