package com.sudhirtheindian4.whatsappnewchattingapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sudhirtheindian4.whatsappnewchattingapp.Activities.ChatsActivity;
import com.sudhirtheindian4.whatsappnewchattingapp.R;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.User;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.RowConversationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    Context context;
    ArrayList<User> users;

    public  UsersAdapter(Context context,ArrayList<User> users){
        this.context = context;
        this.users = users;


    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId+user.getUid();


        // yah code tabhi kam karega jab dusra use bhi login ho
        //  tabhi last message and last message time show hoga






        ////*****************************************************************////////////////////////////
//
        FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                                .child(senderRoom)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if(snapshot.exists()){
                                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                                    long time= snapshot.child("lastMsgTime").getValue(Long.class);
                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                                    holder.binding.lastMsg.setText(lastMsg);

                                                }
                                                else {
                                                    holder.binding.lastMsg.setText("Tap to Chat");
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


        ////*****************************************************************////////////////////////////













        holder.binding.userName.setText(user.getName());  // for gettng the user name

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.profile) // if user has not upload image then by default this image will show
                .into(holder.binding.imageProfile);   // for getting the image from  firebae


      // when every item click then we can go chat activity
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatsActivity.class);
                    intent.putExtra("name",user.getName());
                    intent.putExtra("image",user.getProfileImage());
                    intent.putExtra("uid",user.getUid()); // for user id and name  will show on title
                    intent.putExtra("token",user.getToken()); // for token generate and send the notification device to device
                    context.startActivity(intent);
                }
            });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        // use here binding
        RowConversationBinding binding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);

        }
    }
}
