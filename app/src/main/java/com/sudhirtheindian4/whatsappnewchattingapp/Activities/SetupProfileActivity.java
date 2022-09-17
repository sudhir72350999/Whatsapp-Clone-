package com.sudhirtheindian4.whatsappnewchattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.User;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ActivitySetupProfileBinding;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri selectedImage;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());

//        setContentView(R.layout.activity_setup_profile);

        setContentView(binding.getRoot());

        // for action bar hide
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile");
        progressDialog.setCancelable(false);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();




        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // here we select image from gallery
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);

            }
        });

        // update button tabhi active kare jab namebox aur image select ho empty na ho

        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // namebox check karne ke liye
                String name = binding.nameBox.getText().toString();
                if (name.isEmpty()) {
                    binding.nameBox.setError("Please Type Name");
                    // if name is empty then error will show please type name
                    // if name is not  empty then  return
                    return;
                }

                progressDialog.show();
                // for selected image store in database

                // if user selected image
                if (selectedImage != null) {
                    // selected image  ka profile folder me ek uique id bnega
                    StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // here uri is user profile uri
                                        String imageUrl = uri.toString();
                                        // all the data about user
                                        String uid = auth.getUid(); // user ki ek id banegi
                                        String phone = auth.getCurrentUser().getPhoneNumber();// current user ka phonenumber store hoga
                                        String name = binding.nameBox.getText().toString(); // user jo name update karega


                                        // this  4 constructor is created in user model class
                                        User user = new User(uid, name, phone, imageUrl);

                                        // firebase me is data ko add karna
                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                           progressDialog.dismiss();
                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });


                                    }
                                });
                            }

                        }
                    });

                }
                //if user not selcted image
                else {
                    String uid = auth.getUid(); // user ki ek id banegi
                    String phone = auth.getCurrentUser().getPhoneNumber();// current user ka phonenumber store hoga


                    // this  4 constructor is created in user model class
                    User user = new User(uid, name, phone, "No Image");

                    // firebase me is data ko add karna
                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });


                }


            }
        });

    }

    // here we set the selected image as profile
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // user has selected any image then if statement will be executed
        if (data != null) {
            if (data.getData() != null) {
                binding.imageView.setImageURI(data.getData()); // this will be set the image as profile
                selectedImage = data.getData(); // this will be store the selected image in firebase storage
            }
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }
}