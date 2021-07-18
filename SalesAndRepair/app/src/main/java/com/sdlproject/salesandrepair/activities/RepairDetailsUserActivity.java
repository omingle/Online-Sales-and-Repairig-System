package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdlproject.salesandrepair.R;
import com.sdlproject.salesandrepair.models.ModelRepairUser;

import java.util.Calendar;
import java.util.HashMap;

public class RepairDetailsUserActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private TextView repairIdTv, productIdTv, productNameTv, descriptionTv, dateTv, repairStatusTv, shopNameTv, repairingChargesTv;

    private String repairId, repairTo;

    private String productId;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_details_user);

        backBtn = findViewById(R.id.backBtn);
        repairIdTv = findViewById(R.id.repairIdTv);
        productIdTv = findViewById(R.id.productIdTv);
        productNameTv = findViewById(R.id.productNameTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        dateTv = findViewById(R.id.dateTv);
        repairStatusTv = findViewById(R.id.repairStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        repairingChargesTv = findViewById(R.id.repairingChargesTv);

        //get data from intent
        repairId = getIntent().getStringExtra("repairId");
        repairTo = getIntent().getStringExtra("repairTo");

        firebaseAuth = FirebaseAuth.getInstance();

        loadRepairDetails();
        loadShopInfo();
        loadProductInfo();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadShopInfo() {
        //get shop info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(repairTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = ""+dataSnapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadProductInfo() {
        //get product info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(repairTo).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {

                            for(DataSnapshot ds: snapshot.getChildren()) {
                                if(ds.child("productId").getValue().equals(productId)){
                                    String productName = ""+ds.child("productTitle").getValue();
                                    productNameTv.setText(productName);
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadRepairDetails() {
        //load repair details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(repairTo).child("Repair").child(repairId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String repairId = ""+dataSnapshot.child("repairId").getValue();
                        productId = ""+dataSnapshot.child("productId").getValue();
                        String repairDescription = ""+dataSnapshot.child("repairDescription").getValue();
                        String repairingCharges = ""+dataSnapshot.child("repairingCharges").getValue();
                        String inWarranty = ""+dataSnapshot.child("inWarranty").getValue();
                        String customerEmail = ""+dataSnapshot.child("customerEmail").getValue();
                        String repairStatus = ""+dataSnapshot.child("repairStatus").getValue();
                        String timestamp = ""+dataSnapshot.child("timestamp").getValue();
                        String repairTo = ""+dataSnapshot.child("repairTo").getValue();

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(timestamp));
                        String formattedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString(); //eg 23/11/2020 10:00 AM

                        if(repairStatus.equals("In Progress")) {
                            repairStatusTv.setTextColor(getResources().getColor(R.color.colorBlue));
                        }
                        else if(repairStatus.equals("Completed")) {
                            repairStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(repairStatus.equals("Cancelled")) {
                            repairStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data
                        repairIdTv.setText(repairId);
                        productIdTv.setText(productId);
                        descriptionTv.setText(repairDescription);
                        repairStatusTv.setText(repairStatus);

                        if (repairingCharges.equals("0")) {
                            repairingChargesTv.setText("No Repairing Charges (In Warranty Period");
                        }
                        else {
                            repairingChargesTv.setText("₹" + repairingCharges);
                        }
                        dateTv.setText(formattedDate);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}