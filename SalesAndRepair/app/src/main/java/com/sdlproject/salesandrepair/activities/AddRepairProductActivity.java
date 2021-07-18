package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sdlproject.salesandrepair.R;

import java.util.HashMap;

public class AddRepairProductActivity extends AppCompatActivity {

    // ui views
    private ImageButton backBtn;
    private EditText emailEt, productIdEt, descriptionEt, repairingChargesEt;
    private SwitchCompat warrantySwitch;
    private Button addRepairProductBtn;

    // progress dialog
    private ProgressDialog progressDialog;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_repair_product);

        // init ui views
        backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.emailEt);
        productIdEt = findViewById(R.id.productIdEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        repairingChargesEt = findViewById(R.id.repairingChargesEt);
        warrantySwitch = findViewById(R.id.warrantySwitch);
        addRepairProductBtn = findViewById(R.id.addRepairProductBtn);

        repairingChargesEt.setVisibility(View.VISIBLE);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();


        // if warrantySwitch is checked: hide repairingChargesEt
        // if warrantySwitch is not checked: show repairingChargesEt
        warrantySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    // checked, hide repairingChargesEt
                    repairingChargesEt.setVisibility(View.GONE);
                }
                else {
                    // unchecked, show repairingChargesEt
                    repairingChargesEt.setVisibility(View.VISIBLE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addRepairProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow:
                //1) Input data
                //2) Validate data
                //3) Add data to db

                inputData();
            }
        });
    }

    private String email, productID, repairDescription, repairingCharges;
    private boolean inWarranty = false;

    private void inputData() {
        // 1) input Data
        email = emailEt.getText().toString().trim();
        productID = productIdEt.getText().toString().trim();
        repairDescription = descriptionEt.getText().toString().trim();
        repairingCharges = repairingChargesEt.getText().toString().trim();
        inWarranty = warrantySwitch.isChecked(); // true or false

        // 2) validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(productID)) {
            Toast.makeText(this, "Enter Product ID", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if(TextUtils.isEmpty(repairDescription)) {
            Toast.makeText(this, "Enter Description about what to repair", Toast.LENGTH_SHORT).show();
            return;
        }
        if(inWarranty) {
            // if product is in warranty
            repairingCharges = "0";
        }
        else {
            // if product is not in warranty
            repairingCharges = repairingChargesEt.getText().toString().trim();

            if(TextUtils.isEmpty(repairingCharges)) {
                Toast.makeText(this, "Enter Repairing Charges", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 3) add Product
        addProductToRepair();
    }

    private void addProductToRepair() {
        progressDialog.setMessage("Adding Product to Repair...");
        progressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();

        // setup data to add
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("repairId", "" + timestamp);
        hashMap.put("productId", "" + productID);
        hashMap.put("repairDescription", "" + repairDescription);
        hashMap.put("repairingCharges", "" + repairingCharges);
        hashMap.put("inWarranty", "" + inWarranty);
        hashMap.put("customerEmail", "" + email);
        hashMap.put("repairStatus", "In Progress");   // In Progress/Completed/Cancelled
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("repairTo", "" + firebaseAuth.getUid());

        // add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Repair").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // added to db
                        progressDialog.dismiss();
                        Toast.makeText(AddRepairProductActivity.this, "Product Added to Repair", Toast.LENGTH_SHORT).show();
                        clearData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to add in db
                        progressDialog.dismiss();
                        Toast.makeText(AddRepairProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void clearData() {
        // clear data after uploading product
        emailEt.setText("");
        productIdEt.setText("");
        descriptionEt.setText("");
        warrantySwitch.setChecked(false);
        repairingChargesEt.setText("");
    }
}