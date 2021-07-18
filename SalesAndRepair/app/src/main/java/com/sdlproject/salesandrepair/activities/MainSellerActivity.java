package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.sdlproject.salesandrepair.adapters.AdapterOrderShop;
import com.sdlproject.salesandrepair.adapters.AdapterProductSeller;
import com.sdlproject.salesandrepair.Constants;
import com.sdlproject.salesandrepair.adapters.AdapterRepairSeller;
import com.sdlproject.salesandrepair.models.ModelOrderShop;
import com.sdlproject.salesandrepair.models.ModelProduct;
import com.sdlproject.salesandrepair.R;
import com.sdlproject.salesandrepair.models.ModelRepairSeller;
import com.sdlproject.salesandrepair.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private ImageView profileIv;
    private EditText searchProductEt;
    private TextView nameTv, shopNameTv, emailTv, tabProductsTv, tabOrdersTv, tabRepairTv, filteredProductsTv, filteredOrdersTv;
    private ImageButton logoutBtn, editProfileBtn, addProductBtn, filterProductBtn, filterOrderBtn, addRepairBtn, settingsBtn;
    private RelativeLayout productsRl, ordersRl, repairRl;
    private RecyclerView productsRv, ordersRv, repairRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    private ArrayList<ModelRepairSeller> repairSellerArrayList;
    private AdapterRepairSeller adapterRepairSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        emailTv = findViewById(R.id.emailTv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        tabRepairTv = findViewById(R.id.tabRepairTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        repairRl = findViewById(R.id.repairRl);
        productsRv = findViewById(R.id.productsRv);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        ordersRv = findViewById(R.id.ordersRv);
        addRepairBtn = findViewById(R.id.addRepairBtn);
        repairRv = findViewById(R.id.repairRv);
        settingsBtn = findViewById(R.id.settingsBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = firebaseAuth.getInstance();
        checkUser();

        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        loadRepairProducts();

        // search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open add product activity
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load products
                showProductsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load orders
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

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category : ")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if (selected.equals("All")){
                                    // load all
                                    loadAllProducts();
                                }
                                else {
                                    // load filtered
                                    loadFilteredProducts(selected);
                                }
                            }
                        }).show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                final String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders : ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if(which == 0) {
                                    // All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter("");    //show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing " + optionClicked + " Orders");   //eg. Showing Completed Orders
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();
            }
        });

        addRepairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open add product product to repair activity
                startActivity(new Intent(MainSellerActivity.this, AddRepairProductActivity.class));
            }
        });

        //start settings screen
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this,SettingsActivity.class));
            }
        });
    }

    private void loadAllOrders() {
        // init array list
        orderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear list before adding new data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            orderShopArrayList.add(modelOrderShop);
                        }

                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(final String selected) {
        productList = new ArrayList<>();

        // get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {

                            String productCategory = ""+ds.child("productCategory").getValue();

                            // if selected category matches product category then add in list
                            if (selected.equals(productCategory)) {
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }

                        // setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        // set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        // get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // before getting reset list
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }

                        // setup adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);

                        // set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadRepairProducts() {
        // init array list
        repairSellerArrayList = new ArrayList<>();

        //load products to be repair
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Repair")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear list before adding new data in it
                        repairSellerArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelRepairSeller modelRepairSeller = ds.getValue(ModelRepairSeller.class);
                            //add to list
                            repairSellerArrayList.add(modelRepairSeller);
                        }

                        //setup adapter
                        adapterRepairSeller = new AdapterRepairSeller(MainSellerActivity.this, repairSellerArrayList);
                        //set adapter to recyclerview
                        repairRv.setAdapter(adapterRepairSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProductsUI() {
        // show products ui and hide orders ui, repair ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        repairRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabRepairTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabRepairTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        // show orders ui and hide products ui, repair ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);
        repairRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabRepairTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabRepairTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showRepairUI() {
        // show repair ui and hide products ui, orders ui
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.GONE);
        repairRl.setVisibility(View.VISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

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
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user == null) {
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
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
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            }
                            catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}