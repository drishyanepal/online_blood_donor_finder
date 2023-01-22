package com.example.otpverification;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otpverification.databinding.ActivityHandleNotificationBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HandleNotificationActivity extends AppCompatActivity {
    ActivityHandleNotificationBinding binding;
    String userId;
    String userToken = "";
    String name, bloodGroup, gender, weight, phoneNumber, location, age, imageUri;
    String title = "Blood Donation Request";
    String acceptedMessage, rejectedMessage;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandleNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        binding.phoneLayout.setVisibility(View.GONE);
        binding.acceptImage.setVisibility(View.GONE);
        binding.rejectImage.setVisibility(View.GONE);
        binding.acceptRejectTv.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
        binding.phoneImage.setVisibility(View.GONE);
        binding.messageImage.setVisibility(View.GONE);

        userId = getIntent().getStringExtra("userId");
        name = getIntent().getStringExtra("name");
        bloodGroup = getIntent().getStringExtra("bloodGroup");

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        location = getIntent().getStringExtra("address");
        try {
            age = getIntent().getStringExtra("age");
            gender = getIntent().getStringExtra("gender");
            weight = getIntent().getStringExtra("weight");
//            imageUri = getIntent().getStringExtra("profilePic");
        } catch (Exception e) {
        }

        fillDetails();
        checkIntentSource();
        Log.e("userID", ".........." + userId);

//

        binding.notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.notifyButton.setVisibility(View.GONE);
                getUserToken();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendRequestNotification();
                    }
                }, 2000);
            }
        });
        binding.phoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });
        binding.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", phoneNumber);
                smsIntent.putExtra("sms_body", "नमस्ते, OBDFA यापमा तपाइले मेरो रिकोइस्ट याक्सेप्ट " +
                        "गर्नुभएको थियो। यसै सिलसिलामा कुरा गर्न चाहन्छु। धन्यबाद!  ");
                try {
                    startActivity(smsIntent);
                } catch (Exception e) {
                    Toast.makeText(HandleNotificationActivity.this, "No application found to handle messaging", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void getUserToken() {
        database.getReference().child("Token").child(userId).child("deviceToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
                Log.e("TOKEN", ".........." + userToken);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fillDetails() {
        binding.name.setText(name);
        binding.bloodGroup.setText(bloodGroup);
        try {
            binding.gender.setText(gender);
            binding.weight.setText(weight);
            binding.age.setText(age);
        } catch (Exception e) {
        }
        binding.phoneNumber.setText(phoneNumber);
        binding.location.setText(location);
    }


    private void checkIntentSource() {
        if (getIntent().getStringExtra("intentSource").equals("listAdapter")) {
            binding.userDetailsTv.setVisibility(View.VISIBLE);
            binding.notifyButton.setVisibility(View.VISIBLE);
            binding.acceptImage.setVisibility(View.GONE);
            binding.rejectImage.setVisibility(View.GONE);
            binding.acceptRejectTv.setVisibility(View.GONE);
            binding.phoneLayout.setVisibility(View.GONE);
            binding.phoneImage.setVisibility(View.GONE);
            binding.messageImage.setVisibility(View.GONE);
            donationRequestHandler();

        }
        if (getIntent().getStringExtra("intentSource").equals("notificationActivityFirst")) {
            binding.acceptImage.setVisibility(View.VISIBLE);
            binding.rejectImage.setVisibility(View.VISIBLE);
            binding.acceptRejectTv.setVisibility(View.VISIBLE);
            binding.phoneLayout.setVisibility(View.VISIBLE);
            binding.userDetailsTv.setVisibility(View.GONE);
            binding.notifyButton.setVisibility(View.GONE);
            binding.phoneImage.setVisibility(View.GONE);
            binding.messageImage.setVisibility(View.GONE);
            donationRequestHandler();

        }
        if (getIntent().getStringExtra("intentSource").equals("notificationActivitySecond")) {
            binding.phoneImage.setVisibility(View.VISIBLE);
            binding.messageImage.setVisibility(View.VISIBLE);
            binding.userDetailsTv.setVisibility(View.GONE);
            binding.phoneLayout.setVisibility(View.VISIBLE);
            binding.acceptImage.setVisibility(View.GONE);
            binding.rejectImage.setVisibility(View.GONE);
            binding.notifyButton.setVisibility(View.GONE);
        }
    }

    private void donationRequestHandler() {
        getUserToken();
        final String[] name = {""};
        database.getReference().child("Users").child(auth.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name[0] = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.acceptImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.acceptImage.setClickable(false);
                binding.rejectImage.setClickable(false);
                title = "Donation request accepted";
                acceptedMessage = name[0] + " has accepted your donation request. User ID:";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendAcceptedNotification();
                    }
                }, 1000);

            }
        });

        binding.rejectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.acceptImage.setClickable(false);
                binding.rejectImage.setClickable(false);
                title = "Donation request rejected";
                rejectedMessage = name[0] + " has rejected your donation request. User ID:";
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendRejectedNotification();
                    }
                }, 1000);
            }
        });
    }

    private void sendRequestNotification() {
        String sendMessage = "You have received a blood donation request. Click here for details. Sender ID:" + auth.getUid();
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userToken,
                title, sendMessage, getApplicationContext(), HandleNotificationActivity.this);
        notificationsSender.SendNotifications();
        Toast.makeText(HandleNotificationActivity.this, "Request notification sent", Toast.LENGTH_SHORT).show();
        binding.notifyButton.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void sendAcceptedNotification() {
        acceptedMessage = acceptedMessage + auth.getUid();
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userToken,
                title, acceptedMessage, getApplicationContext(), HandleNotificationActivity.this);
        notificationsSender.SendNotifications();
        Toast.makeText(HandleNotificationActivity.this, "Acceptance notification sent", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void sendRejectedNotification() {
        rejectedMessage = rejectedMessage + auth.getUid();
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userToken,
                title, rejectedMessage, getApplicationContext(), HandleNotificationActivity.this);
        notificationsSender.SendNotifications();
        Toast.makeText(HandleNotificationActivity.this, "Rejection notification sent", Toast.LENGTH_SHORT).show();
        finish();
    }
}