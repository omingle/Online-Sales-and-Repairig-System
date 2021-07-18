package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.Calendar;
import java.util.HashMap;

public class RepairDetailsSellerActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn, editBtn;
    private TextView repairIdTv, productIdTv, descriptionTv, dateTv, repairStatusTv, emailTv, repairingChargesTv;

    private String repairId, customerEmail;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        repairIdTv = findViewById(R.id.repairIdTv);
        productIdTv = findViewById(R.id.productIdTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        dateTv = findViewById(R.id.dateTv);
        repairStatusTv = findViewById(R.id.repairStatusTv);
        emailTv = findViewById(R.id.emailTv);
        repairingChargesTv = findViewById(R.id.repairingChargesTv);

        //get data from intent
        repairId = getIntent().getStringExtra("repairId");
        customerEmail = getIntent().getStringExtra("custEmail");

        firebaseAuth = FirebaseAuth.getInstance();

        loadRepairDetails();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit repair status: In Progress, Completed or Cancelled
                editRepairStatusDialog();
            }
        });

    }

    private void editRepairStatusDialog() {
        //options to be displayed in dialog
        final String[] options = {"In Progress", "Completed", "Cancelled"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Repairing Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        String selectedOption = options[which];
                        editRepairStatus(selectedOption);
                    }
                })
                .show();
    }

    private void editRepairStatus(final String selectedOption) {
        //setup data to put in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("repairStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Repair").child(repairId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //status updated
                        Toast.makeText(RepairDetailsSellerActivity.this, "Product Repairing is : " + selectedOption, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update status
                        Toast.makeText(RepairDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRepairDetails() {
        //load repair details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Repair").child(repairId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String repairId = ""+dataSnapshot.child("repairId").getValue();
                        String productId = ""+dataSnapshot.child("productId").getValue();
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
                        emailTv.setText(customerEmail);

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