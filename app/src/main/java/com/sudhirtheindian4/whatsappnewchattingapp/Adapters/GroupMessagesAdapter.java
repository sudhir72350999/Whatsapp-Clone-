package com.sudhirtheindian4.whatsappnewchattingapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.Message;
import com.sudhirtheindian4.whatsappnewchattingapp.Models.User;
import com.sudhirtheindian4.whatsappnewchattingapp.R;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ItemRecieveBinding;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ItemRecieveGroupBinding;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ItemSentBinding;
import com.sudhirtheindian4.whatsappnewchattingapp.databinding.ItemSentGroupBinding;

import java.util.ArrayList;

// yaha dono me se kisi viewholer se extend nahi karenge
public class GroupMessagesAdapter extends RecyclerView.Adapter {

    // final karne se ise kabhi bhi change na hi kr skte
    final int ITEM_SENT = 1;
    final int ITEM_RECIEVE = 2;


    Context context;
    ArrayList<Message> messages;


    public GroupMessagesAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_group, parent, false);
            return new SendViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve_group, parent, false);
            return new RecieveViewHolder(view);
        }

    }


    /// this will be uses muliple view holer using case
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // ydi sender ki id current user se match kregi to messge send hoga
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }
        else {
            return ITEM_RECIEVE;
        }

    }




    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {



        Message message = messages.get(position);

        ///**************** yha se code tabhi kam  karega jab dusra user bhi login ho use hi message send hoga


        // for feeling


        int reaction[] =new int[]{     // comment
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reaction)
                .build();


        // comment
        // for pop up
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            // feeling for sender and reciever

            if(pos < 0)
                return  false;
// comment
            if(holder.getClass()==SendViewHolder.class){         // safe
                SendViewHolder viewHolder = (SendViewHolder)holder;  // safe

                viewHolder.binding.feeling.setImageResource(reaction[pos]);   // comment
                viewHolder.binding.feeling.setVisibility(View.VISIBLE); // feeling show
            }
            else {
                RecieveViewHolder viewHolder = (RecieveViewHolder) holder;     // safe

                viewHolder.binding.feeling.setImageResource(reaction[pos]);  // comment
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);     // comment

            }

//             for setting of feeling
            message.setFeeling(pos);     // comment


          //  ***************************************   //safe
            // for sender room
            FirebaseDatabase.getInstance().getReference()     //safe
                    .child("public")       // change the chats this occur error
                    .child(message.getMessageId()).setValue(message);


            //  **************************************    //safe


            return true; // true is closing popup, false is requesting a new selection
        });




        ///**************** yha tak  code tabhi kam  karega jab dusra user bhi login ho use hi message send hoga





        //safe
        if(holder.getClass()==SendViewHolder.class){
            SendViewHolder viewHolder = (SendViewHolder)holder; // here typecasting is necessory

            // for image
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder_image).into(viewHolder.binding.image);

            }
            // for image


                    //// for sender user name get

            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(message.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText("@"+user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




            viewHolder.binding.message.setText(message.getMessage());
            //safe

            // for show feeling
            ///comment
            if(message.getFeeling()>=0){
                viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]); // message is long so tupecast in int
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }
            //comment

            // when user touch any message
            //comment


//            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });


            // for image
//            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });

            // comment
        }
        else{
            //   //safe
            RecieveViewHolder viewHolder = (RecieveViewHolder) holder; // here typecasting is necessory

            // for image
            if(message.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder_image).into(viewHolder.binding.image);


//                placeholder(R.drawable.placeholder_image)  place holder vah image hota hai jo image load nahi hone tak show hota hai
                // image load hone ke bad show hona  band kar deta hai
            }
            // for image

            //// for sender user name get

            FirebaseDatabase.getInstance().getReference().child("users")
                          .child(message.getSenderId())
                                  .addListenerForSingleValueEvent(new ValueEventListener() {
                                      @Override
                                      public void onDataChange(@NonNull DataSnapshot snapshot) {
                                          if(snapshot.exists()){
                                              User user = snapshot.getValue(User.class);

                                              viewHolder.binding.name.setText("@"+user.getName());
                                          }
                                      }

                                      @Override
                                      public void onCancelled(@NonNull DatabaseError error) {

                                      }
                                  });



            viewHolder.binding.message.setText(message.getMessage());
            //safe


            // comment
            // for show feeling
            if(message.getFeeling()>=0){
             viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]); // message is long so tupecast in int
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }


//            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });


//            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    popup.onTouch(view,motionEvent);
//                    return false;
//                }
//            });
            // comment


        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    // here we have to create 2 view holder 1 for send  layout and 2 for receive layout
    public  class  SendViewHolder extends RecyclerView.ViewHolder{
        ItemSentGroupBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentGroupBinding.bind(itemView);
        }
    }

    public  class RecieveViewHolder extends  RecyclerView.ViewHolder{
        ItemRecieveGroupBinding binding;
        public RecieveViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemRecieveGroupBinding.bind(itemView);

        }
    }
}
