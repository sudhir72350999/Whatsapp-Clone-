package com.sudhirtheindian4.whatsappnewchattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sudhirtheindian4.whatsappnewchattingapp.Adapters.GroupMessagesAdapter;
import com.sudhirtheindian4.whatsappnewchattingapp.Adapters.MessagesAdapter;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.Message;
import com.sudhirtheindian4.whatsappnewchattingapp.R;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ActivityChatsBinding;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class GroupChatActivity extends AppCompatActivity {

   ActivityGroupChatBinding binding;

    GroupMessagesAdapter adapter;
    ArrayList<Message> messages;



    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    String senderUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_group_chat);

        getSupportActionBar().setTitle("Group Chat");
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        senderUid = FirebaseAuth.getInstance().getUid();

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image.....");
        progressDialog.setCancelable(false);    // isse dialog close nahi hota hai kahi bhi side me click krne par pura hon ese pahle




        messages = new ArrayList<>();

        adapter = new GroupMessagesAdapter(this,messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);



        // for showing the message in display
        database.getReference().child("public")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();

                        for(DataSnapshot snapshot1:snapshot.getChildren()){
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
                Message message = new Message(messageText,senderUid,date.getTime());

                /// message send hone ke bad editetext ko empty karne ke liye
                binding.messageBox.setText("");


                database.getReference().child("public")
                        .push()
                        .setValue(message);
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
                startActivityForResult(intent,25);   // here 25 is temporary number we can take any number
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 25){
            if(data!=null){
                if(data.getData() !=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() +"");
                    progressDialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                progressDialog.dismiss();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String  filepath = uri.toString();


                                        String messageText = binding.messageBox.getText().toString();
                                        Date date = new Date();
                                        Message message = new Message(messageText,senderUid,date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filepath);   // give the path of sending message

                                        /// message send hone ke bad editetext ko empty karne ke liye
                                        binding.messageBox.setText("");

                                         database.getReference().child("public")
                                                 .push()
                                                 .setValue(message);





                                    }
                                });
                            }
                        }
                    });

                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}