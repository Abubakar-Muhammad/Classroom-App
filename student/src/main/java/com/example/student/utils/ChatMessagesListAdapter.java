package com.example.student.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student.R;
import com.example.student.models.ChatMessage;
import com.example.student.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Map;

public class ChatMessagesListAdapter extends RecyclerView.Adapter<ChatMessagesListAdapter.ViewHolder> {

    private List<ChatMessage> mChatMessageList;
    LayoutInflater mInflater;
    Context mContext;
    private static final String TAG = "ChatMessagesListAdapter";

    public ChatMessagesListAdapter(List<ChatMessage> chatMessageList, Context context) {
        mChatMessageList = chatMessageList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.chat_message_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.message.setText(mChatMessageList.get(position).getMessage());
        String timestamp = DateAndTimeConversion.newInstance().get12hrFormattedTime(mChatMessageList.get(position).getMessage_timestamp());
        holder.timeStamp.setText(timestamp);
        holder.senderImage.setImageDrawable(null);
        holder.senderName.setText(null);
        if(mChatMessageList.get(position).getMessage_sender_id() != null){
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(mContext.getString(R.string.dbuser_node))
                    .orderByKey()
                    .equalTo(mChatMessageList.get(position).getMessage_sender_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String,Object> map = (Map<String,Object>)snapshot.getChildren().iterator().next().getValue();
                    String name = map.get(mContext.getString(R.string.user_name)).toString();
                    String image =map.get(mContext.getString(R.string.user_profile)).toString();
                    User user = new User();
                    user.setName(name);
                    user.setProfile_image(image);
                    holder.senderName.setText(user.getName());
                    ImageLoader.getInstance().displayImage(user.getProfile_image(),holder.senderImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: "+error.getMessage());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mChatMessageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView senderImage;
        TextView senderName;
        TextView message;
        TextView timeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senderImage = itemView.findViewById(R.id.profile_image);
            senderName = itemView.findViewById(R.id.user);
            message = itemView.findViewById(R.id.message);
            timeStamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
