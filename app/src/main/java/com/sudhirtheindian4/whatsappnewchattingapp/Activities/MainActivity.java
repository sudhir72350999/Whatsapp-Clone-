package com.sudhirtheindian4.whatsappnewchattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sudhirtheindian4.whatsappnewchattingapp.Adapters.TopStatusAdapter;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.Status;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.UserStatus;
import com.sudhirtheindian4.whatsappnewchattingapp.R;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.User;
import com.sudhirtheindian4.whatsappnewchattingapp.Adapters.UsersAdapter;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ActivityMainBinding;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    FirebaseDatabase database;
    UsersAdapter usersAdapter;
    ArrayList<User> users;

    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;

    ProgressDialog dialog;

    User user;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*  note about remote config
     Remote config sabse pahle open hone wale activity me hi use karte hai
         we can control our appllication by backkend  very easily
         by remote config we can change color of toolbar or  background also
         */


        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(3600)
                .setMinimumFetchIntervalInSeconds(0)

                // ise debugging ya develop karte samay 0second rakhte hai kyoki3600 second yani 1 hour
                // wait nahi kar sakte update ke liye
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        /// by fetch activate method we change all thing
        remoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

                /*
                for background image changing by remote config
                 */
                String backgroundImage = remoteConfig.getString("backgroundImage");

                Glide.with(MainActivity.this)
                        .load(backgroundImage)
                        .into(binding.backgroundImage);







                /*  toolbar color and toolbar background image*/

                String toolbarColor = remoteConfig.getString("toolbarColor");

                String toolbarImage = remoteConfig.getString("toolbarImage");


                ///// kabhi toolbar image aur kabhi toolbar color ke liye boolean
                boolean isToolbarEnabled = remoteConfig.getBoolean("toolbarImageEnabled");

                if(isToolbarEnabled){
                    Glide.with(MainActivity.this).load(toolbarImage)
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    getSupportActionBar()
                                            .setBackgroundDrawable(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

//

                }
                else {
                    //                Toast.makeText(MainActivity.this, toolbarColor, Toast.LENGTH_SHORT).show();
                 getSupportActionBar().
                 setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));


                }

                /*  toolbar color and toolbar background image*/


            }
        });



        /// initialize the database users and user adapter for show the data from firebase
        database = FirebaseDatabase.getInstance();

        /// for specifice device to send  notification  create token by user and send the notification by token
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String token) {

                /// here string for  name of object and Object stores the value of string
                HashMap<String, Object> map = new HashMap<>();

                map.put("token",token);
                database.getReference().child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                        .updateChildren(map);


//                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);



        users = new ArrayList<>();
        userStatuses = new ArrayList<>();  // for status


        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        usersAdapter = new UsersAdapter(this,users);




        statusAdapter = new TopStatusAdapter(this, userStatuses); // for status
        binding.statusList.setAdapter(statusAdapter); // for status only
        LinearLayoutManager layoutManager= new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);

        // you can aleready use this in xml code so donot need this code here
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.recyclerView.setAdapter(usersAdapter);

        binding.recyclerView.showShimmerAdapter(); /// for showing shimeer
        binding.statusList.showShimmerAdapter();


        // get the realtime data from firebase

         database.getReference().child("users").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
//                 users.clear();
                 for(DataSnapshot snapshot2 : snapshot.getChildren()){
                     User user = snapshot2.getValue(User.class);

                     // yadi khud ke mobile me login hai to use nahi show karna hai
                     // kyoki vah khud ko message send nahi krega

                     if(!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                     {
                         users.add(user);
                     }else {
                         Toast.makeText(MainActivity.this, "You can not send message yourself", Toast.LENGTH_SHORT).show();
                     }



                     // yadi khud ke mobile me login hai to use nahi show karna hai
                     // kyoki vah khud ko message send nahi krega

                 }

                 binding.recyclerView.hideShimmerAdapter(); /// for hinding shimeer
                 usersAdapter.notifyDataSetChanged();

             }


             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });

         // database se value ko show ya screen par dikhane ke liye addvalueeventlistster ka use krete hai
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //user statuse clear krne se story update krne par ek jagah hi upada hogi
                userStatuses.clear();
                if(snapshot.exists()){
                    for(DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));



                        ArrayList<Status> statuses = new ArrayList<>();
                        for(DataSnapshot statusSnapshot :storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus  = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses); // set the status
                        userStatuses.add(status); // add kare status ko

                    }

                    binding.statusList.hideShimmerAdapter(); // after show status list end  the shimer
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


       // for status
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.status:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent,75);
                        break;

                }
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData()!=null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date  = new Date();
                StorageReference reference = storage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileImage());
                                    userStatus.setLastUpdated(date.getTime());


                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileImage",userStatus.getProfileImage());
                                    obj.put("lastUpdated",userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();
                                    Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                    database.getReference()
                                                    .child("stories")
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                                    .updateChildren(obj);

                                    database.getReference().child("stories")
                                                    .child(FirebaseAuth.getInstance().getUid())
                                                            .child("statuses")
                                                                    .push()
                                                                            .setValue(status);

                                    dialog.dismiss(); // for diualog show dismiiss

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group:
//                startActivity(new Intent(MainActivity.this,GroupChatActivity.class));
                Intent i = new Intent(MainActivity.this, GroupChatActivity.class);
                startActivity(i);
                finish();

                Toast.makeText(this, "group clicked ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "Toast", Toast.LENGTH_SHORT).show();
                break;

            case R.id.chats:
                Toast.makeText(this, "chats", Toast.LENGTH_SHORT).show();
                break;
            case  R.id.call:
                Toast.makeText(this, "call", Toast.LENGTH_SHORT).show();
                break;
            case R.id.invite:
                Toast.makeText(this, "invite", Toast.LENGTH_SHORT).show();
                break;
            case R.id.video_call:
                Toast.makeText(this, "video call", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }


    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }
}