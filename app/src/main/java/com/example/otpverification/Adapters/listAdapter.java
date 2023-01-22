package com.example.otpverification.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otpverification.HandleNotificationActivity;
import com.example.otpverification.Models.UserModel;
import com.example.otpverification.NotificationsActivity;
import com.example.otpverification.R;
import com.example.otpverification.SearchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class listAdapter extends RecyclerView.Adapter<listAdapter.myViewHolder> {
    Double lat, lon;
    Context context;
    ArrayList<UserModel> list;
    String location;
    SearchActivity searchActivity = new SearchActivity();

    public listAdapter(Context context, ArrayList<UserModel> list, String location, Double lat, Double lon) {
        this.context = context;
        this.list = list;
        this.location = location;
        this.lat = lat;
        this.lon = lon;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_row, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        UserModel model = list.get(position);
        Double latD = Double.parseDouble(model.getLatitude());
        Double lonD = Double.parseDouble(model.getLongitude());
        try {
            Double distance = searchActivity.distanceCalculateHaversine(lat, lon, latD, lonD);
            String formattedDistance = String.format("%.2f", distance);
            holder.distance.setText(formattedDistance);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        holder.location.setText(model.getLiveLocation());
//
        holder.name.setText(model.getName());
        holder.bloodGroup.setText(model.getBloodGroup());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HandleNotificationActivity.class);
                intent.putExtra("name", model.getName());
                intent.putExtra("age", model.getAge());
                intent.putExtra("gender", model.getGender());
                intent.putExtra("bloodGroup", model.getBloodGroup());
                intent.putExtra("phoneNumber", model.getPhoneNumber());
                intent.putExtra("weight", model.getWeight());
                intent.putExtra("userId", model.getUserId());
                intent.putExtra("address", location);
//                intent.putExtra("profilePic", model.getProfilePic());
                intent.putExtra("intentSource", "listAdapter");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView location, name, bloodGroup, distance;
        ImageView profilePic;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.locationSample);
            name = itemView.findViewById(R.id.nameSample);
            bloodGroup = itemView.findViewById(R.id.bloodGroupSample);
            distance = itemView.findViewById(R.id.distanceSample);
            profilePic = itemView.findViewById(R.id.profile_image);
        }
    }
}
