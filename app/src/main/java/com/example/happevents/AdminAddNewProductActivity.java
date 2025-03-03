package com.example.happevents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, Description, Price, Pname, saveCurrentDate, saveCurrentTime;
    private Button AddNewProductButton;
    private ImageView InputProductImage;
    private EditText InputProductName, InputProductDescription, InputProductPrice;
    private static final int GalleryPick=1;
    private Uri ImageUri;
    private String ProductRandomKey, downloadImageUrl;
    private StorageReference ProductImagesRef;
    private DatabaseReference ProductsRef;
    private ProgressDialog loadingBar;   //while loading

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images"); //Product IMages is the folder wea images are
        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        // Toast.makeText(this,categoryName, Toast.LENGTH_SHORT).show();
        AddNewProductButton = (Button) findViewById(R.id.add_new_product);
        InputProductImage = (ImageView) findViewById(R.id.product_image);
        InputProductName = (EditText) findViewById(R.id.product_name);
        InputProductDescription = (EditText) findViewById(R.id.product_description);
        InputProductPrice = (EditText) findViewById(R.id.product_price);
        loadingBar = new ProgressDialog(this);

        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ValidateProductData();
            }
        });
    }

    private void ValidateProductData()
    {

        Description= InputProductDescription.getText().toString();
        Price= InputProductPrice.getText().toString();
        Pname= InputProductName.getText().toString();

        if (ImageUri==null){

            Toast.makeText(this, "Product Image Is Mandatory", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Description)){

            Toast.makeText(this, "Please Write Product Description", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Price)){

            Toast.makeText(this, "Please Write Product price", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Pname)){

            Toast.makeText(this, "Please Write Product Name", Toast.LENGTH_SHORT).show();
        }
        else {
            StoreProductInformation();
        }
    }

    private void StoreProductInformation()
    {
        //Information to display while loading

        loadingBar.setTitle("Add New Product");
        loadingBar.setMessage("Please Wait while we are adding the new product");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        //Stops here

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH :mm:ss a"); //in amm or pm
        saveCurrentTime = currentTime.format(calendar.getTime());

        //creating a random key by combining current time and Date
        ProductRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = ProductImagesRef.child(ImageUri.getLastPathSegment() + ProductRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();

                Toast.makeText(AdminAddNewProductActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast.makeText(AdminAddNewProductActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                //getting image uri so that it can be stored in database and then be displayed to a user
                Task<Uri> uriTask= uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if (!task.isSuccessful()){
                            throw task.getException();

                        }

                        downloadImageUrl =filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Got the product Url successfully" , Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }
                    }
                });
            }
        });
    }

    private void SaveProductInfoToDatabase()
    {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", ProductRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", Description);
        productMap.put("image", downloadImageUrl);
        productMap.put("category", categoryName);
        productMap.put("price", Price);
        productMap.put("pname", Pname);

        //to database
        ProductsRef.child(ProductRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                            startActivity(intent);

                            loadingBar.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Product Has been Added Successfully" , Toast.LENGTH_SHORT).show();
                        }
                        else {

                            loadingBar.dismiss(); //dismiss loadin bar in case of any error
                            String message = task.getException().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message , Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, GalleryPick);

        /*
         Intent intent = new Intent();
        intent.setType("image/*");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), GalleryPick);
        */
    }








    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick && resultCode== RESULT_OK && data!=null){
            ImageUri = data.getData();
            InputProductImage.setImageURI(ImageUri);
        }
    }
}

