package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdlproject.salesandrepair.R;
import com.sdlproject.salesandrepair.adapters.AdapterOrderUser;
import com.sdlproject.salesandrepair.adapters.AdapterRepairSeller;
import com.sdlproject.salesandrepair.adapters.AdapterRepairUser;
import com.sdlproject.salesandrepair.adapters.AdapterShop;
import com.sdlproject.salesandrepair.models.ModelOrderUser;
import com.sdlproject.salesandrepair.models.ModelRepairSeller;
import com.sdlproject.salesandrepair.models.ModelRepairUser;
import com.sdlproject.salesandrepair.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv, tabRepairTv;
    private RelativeLayout shopsRl, ordersRl, repairRl;
    private ImageButton logoutBtn, editProfileBtn, settingsBtn;
    private ImageView profileIv;
    private RecyclerView shopRv, ordersRv, repairRv;

    String email;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    private ArrayList<ModelRepairUser> repairUserArrayList;
    private AdapterRepairUser adapterRepairUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        tabShopsTv = findViewById(R.id.tabShopsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        tabRepairTv = findViewById(R.id.tabRepairTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        profileIv = findViewById(R.id.profileIv);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        repairRl = findViewById(R.id.repairRl);
        shopRv = findViewById(R.id.shopRv);
        ordersRv = findViewById(R.id.ordersRv);
        repairRv = findViewById(R.id.repairRv);
        settingsBtn = findViewById(R.id.settingsBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = firebaseAuth.getInstance();
        checkUser();

        //at start show shops ui
        showShopsUI();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make offline
                // logout
                // go to login activity
                makeMeOffline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open edit profile activity
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
            }
        });

        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show shops
                showShopsUI();
            }
        });
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show orders
                showOrdersUI();
            }
        });

        tabRepairTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load products to repair
                showRepairUI();
            }
        });

        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainUserActivity.this,SettingsActivity.class));
            }
        });
    }

    private void showShopsUI() {
        //show shops ui, hide orders ui and repair ui
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        repairRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabRepairTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabRepairTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui, hide shops ui and repair ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        repairRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabRepairTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabRepairTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showRepairUI() {
        // show repair ui, hide shop ui and orders ui
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.GONE);
        repairRl.setVisibility(View.VISIBLE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabRepairTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabRepairTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeMeOffline() {
        // after logging in, make user online
        progressDialog.setMessage("Logging Out...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        // update value in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null) {
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()) {
                            //get user data
                            String name = ""+ds.child("name").getValue();
                            email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            //set user data
                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            }
                            catch(Exception e) {
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }

                            //load only those shops that are in city of user
                            loadShops(city);
                            loadOrders();
                            loadRepairProducts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadRepairProducts() {
        //init order list
        repairUserArrayList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                repairUserArrayList.clear();

                for(DataSnapshot ds: snapshot.getChildren()) {
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Repair");
                    ref.orderByChild("customerEmail").equalTo(email)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()) {
                                        for(DataSnapshot ds: snapshot.getChildren()) {
                                            ModelRepairUser modelRepairUser = ds.getValue(ModelRepairUser.class);

                                            //add to list
                                            repairUserArrayList.add(modelRepairUser);
                                        }
                                        //setup adapter
                                        adapterRepairUser = new AdapterRepairUser(MainUserActivity.this, repairUserArrayList);
                                        //set to recylerview
                                        repairRv.setAdapter(adapterRepairUser);
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
    }

    private void loadOrders() {
        //init order list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                //dataSnapshot.getChildren()
                for(DataSnapshot ds: snapshot.getChildren()) {
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()) {
                                        for(DataSnapshot ds: snapshot.getChildren()) {
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        //set to recylerview
                                        ordersRv.setAdapter(adapterOrderUser);
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

    }

    private void loadShops(final String myCity) {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();

                            //show only user city shops
                            if(shopCity.equals(myCity)) {
                                shopsList.add(modelShop);
                            }

                            //if we want to display all shops, skip the if statement and add this
                            //shopsList.add(modelShop);
                        }
                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopsList);
                        //set adapter to recyclerview
                        shopRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}