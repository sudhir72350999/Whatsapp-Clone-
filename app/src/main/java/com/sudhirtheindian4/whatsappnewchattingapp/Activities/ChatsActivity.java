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
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sudhirtheindian4.whatsappnewchattingapp.Adapters.MessagesAdapter;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.Message;
import com.sudhirtheindian4.whatsappnewchattingapp.R;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ActivityChatsBinding;

import org.json.JSONObject;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatsActivity extends AppCompatActivity {

    ActivityChatsBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    String receiverUid;
    String senderUid;
    String token;
    String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar); //for show toolbar


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image.....");
        progressDialog.setCancelable(false);    // isse dialog close nahi hota hai kahi bhi side me click krne par pura hon ese pahle

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messages = new ArrayList<>();


        String name = getIntent().getStringExtra("name");   // for every  user name show on title
        String profile = getIntent().getStringExtra("image");   // for every  user name show on title
        String token = getIntent().getStringExtra("token");   // for every  user generate the token


        //// token not generated properlu
//        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();

        ///now set the name and show the name of user and image
        binding.name.setText(name);
        Glide.with(ChatsActivity.this).load(profile).placeholder(R.drawable.avatar).into(binding.profile);

        /// back button par press  krte hi back ho jeyenge
        binding.imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();


        /// find reciever is online or not  and show it in chatactivity
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if (!status.isEmpty()) {

                        // yadi user offline hoga to offline text show naho hoga balki empty show hoga
                        if (status.equals("Offline")) {
                            binding.status.setVisibility(View.GONE);
                        } else {
                            binding.status.setText(status);
                            binding.status.setVisibility(View.VISIBLE);

                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this, messages, receiverRoom, senderRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // for showing the message in display
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();

                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String messageText = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageText, senderUid, date.getTime());

                /// message send hone ke bad editetext ko empty karne ke liye
                binding.messageBox.setText("");

                // for uniqe id to sent the feeling other
                //we use unique key
                String randomKey = database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
//                        .push() // here push make unique node
                        .setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                sendNotification(name, message.getMessage(), token);
//                                Toast.makeText(ChatsActivity.this, "message has been sent", Toast.LENGTH_SHORT).show();

                                // for reciver
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
//                                        .push()  /// do not use push
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                sendNotification(name, message.getMessage(), token);
                                            }
                                        });

                            }
                        });

            }
        });


        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); // isse sara image galary se access kar sakte hai
                // for video intent.setType("video/*")
                // for video and photo all the type   intent.setType("*/*")
                startActivityForResult(intent, 25);   // here 25 is temporary number we can take any number
            }
        });


        /// user jab bhi typeing karna start kare use show karna hai


        // jab user typing karna chhod de to use typing nahi online show karna hai
       final Handler handler = new Handler();

        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("presence").child(senderUid).setValue("Typing....");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTypeing, 1000);
                // yadi user 1 secoond yani 1000 milisecond tak typeing nahi karta hai to use online dikhayenge


            }
            /// here we are using runnable thread for multitasking  online and typing ek sath hai isliye

            Runnable userStoppedTypeing = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");

                }
            };
        });


        /// remove  the title of the
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        getSupportActionBar().setTitle(name);

        // for back in previous activtiy

        // yah automatic back icon generate karta hai
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }


    /// token means kise message send karna hai ?

    void sendNotification(String name, String message, String token) {

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
//            String url = "https://fcm.googleeapis.com/fcm/send";
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("name", message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);


            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    /// if api called then success
                    Toast.makeText(ChatsActivity.this, "success", Toast.LENGTH_SHORT).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(ChatsActivity.this,error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(ChatsActivity.this, "error in api calling", Toast.LENGTH_SHORT).show();
                }
                /// here we create getheader for firbase
            }) {
                /// by header we send token and notification in api
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    HashMap<String, String> map = new HashMap<>();
                    ///without server key we can not  send the notification to user
                    ///befor  key we should write Key=
                    /// for key we shoold go to firebase setting and mesaaging then find server ky
                    String key = "Key=AAAAXC5jX8U:APA91bEg9p1u1L-JYQhFGPX-sgzrtyZhQwdxxuCcI3yxBaWtpVhxoAT-YSyXWwpwbAVEwlzVL5lYDQSP4GAG2T8a17Cz1cRef-SZAaaCEXNvNuU2x6-tSwSuY4U762A7YLofAwGxEa6S";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);


                    return map;

                }
            };

            queue.add(request);

        } catch (Exception e) {

        }


    }

    // for attchment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    progressDialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filepath = uri.toString();


                                        String messageText = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageText, senderUid, date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filepath);   // give the path of sending message

                                        /// message send hone ke bad editetext ko empty karne ke liye
                                        binding.messageBox.setText("");

                                        // for uniqe id to sent the feeling other
                                        //we use unique key
                                        String randomKey = database.getReference().push().getKey();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
//                        .push() // here push make unique node
                                                .setValue(message)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        // for reciver
                                                        database.getReference().child("chats")
                                                                .child(receiverRoom)
                                                                .child("messages")
                                                                .child(randomKey)
//                                        .push()
                                                                .setValue(message)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {

                                                                    }
                                                                });

                                                    }
                                                });
//                                        Toast.makeText(ChatsActivity.this, filepath, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                }
            }
        }
    }


    // for back in previous activtiy
    // yah automatic back icon generate karta hai

    // this is the method for back
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call:
                Toast.makeText(this, "call", Toast.LENGTH_SHORT).show();
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