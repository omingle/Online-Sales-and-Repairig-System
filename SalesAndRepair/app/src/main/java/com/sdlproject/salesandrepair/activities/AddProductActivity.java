package com.sdlproject.salesandrepair.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sdlproject.salesandrepair.Constants;
import com.sdlproject.salesandrepair.R;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {

    // ui views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private TextView categoryTv;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

    // permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    // image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    // permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    // image picked URL
    private Uri image_uri;

    // progress dialog
    private ProgressDialog progressDialog;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountedPriceEt = findViewById(R.id.discountedPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        categoryTv = findViewById(R.id.categoryTv);
        discountSwitch = findViewById(R.id.discountSwitch);
        addProductBtn = findViewById(R.id.addProductBtn);

        // unchecked, hide discountPriceEt, discountNoteEt
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        // init permission array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        // if discountSwitch is checked: show discountPriceEt, discountNoteEt
        // if discountSwitch is not checked: hide discountPriceEt, discountNoteEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    // checked, show discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);
                }
                else {
                    // unchecked, hide discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show dialog to pick image
                showImagePickDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pick category
                categoryDialog();
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
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

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        // 1) input Data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked(); // true or false

        // 2) validate data
        if(TextUtils.isEmpty(productTitle)) {
            Toast.makeText(this, "Enter Product Name", Toast.LENGTH_SHORT).show();
            return; // don't proceed further
        }
        if(TextUtils.isEmpty(productCategory)) {
            Toast.makeText(this, "Select Product Category", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(productQuantity)) {
            Toast.makeText(this, "Enter Product Quantity", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(this, "Enter Product Price", Toast.LENGTH_SHORT).show();
            return;
        }
        if(discountAvailable) {
            // if product has discount
            discountPrice = discountedPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();

            if(TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(this, "Enter Discount Price", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            // if product do not have discount
            discountPrice = "0";
            discountNote = "";
        }

        // 3) add Product
        addProduct();
    }

    private void addProduct() {
        progressDialog.setMessage("Adding Product...");
        progressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();

        if(image_uri == null) {
            // add info without image

            // setup data to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); // no image, set empty
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());

            // add to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // added to db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // failed to add in db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            // upload info with image

            // first upload image to storage

            // name and path of image
            String filePathAndName = "product_images/" + "" + timestamp;

            // upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // image uploaded
                            // get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()) {
                                // uri of image received, upload to db
                                // setup data to upload
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId", "" + timestamp);
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + firebaseAuth.getUid());

                                // add to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // added to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product Added", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // failed to add in db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        // clear data after uploading product
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountedPriceEt.setText("");
        discountedNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri = null;
    }

    private void categoryDialog() {
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get picked category
                        String category = Constants.productCategories[which];

                        // set picked category
                        categoryTv.setText(category);
                    }
                }).show();
    }

    private void showImagePickDialog() {
        // options to display dialog
        String[] options = {"Camera", "Gallery"};

        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // handle click
                        if(which == 0) {
                            // camera clicked
                            if(checkCameraPermission()) {
                                // camera permission allowed
                                pickFromCamera();
                            }
                            else {
                                // not allowed, request
                                requestCameraPermission();
                            }
                        }
                        else {
                            // gallery clicked
                            if(checkStoragePermission()) {
                                // storage permission allowed
                                pickFromGallery();
                            }
                            else {
                                // not allowed, request
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }

    private void pickFromGallery() {
        // intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        // intent to pick image from camera

        // using media store to pick high/original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    // handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted) {
                        // permission allowed
                        pickFromCamera();
                    }
                    else {
                        // permission denied
                        Toast.makeText(this, "Camera $ Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted) {
                        // permission allowed
                        pickFromGallery();
                    }
                    else {
                        // permission denied
                        Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // handle image pick result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {
                // get picked image from gallery
                image_uri = data.getData();

                // set to imageView
                productIconIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                // set to imageView
                productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}