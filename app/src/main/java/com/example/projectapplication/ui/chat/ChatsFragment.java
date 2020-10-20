package com.example.projectapplication.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectapplication.R;
import com.example.projectapplication.models.ChatMessage;
import com.example.projectapplication.ui.SharedViewModel;
import com.example.projectapplication.utils.ChatMessagesListAdapter;
import com.example.projectapplication.utils.DateAndTimeConversion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    SharedViewModel mSharedViewModel;
    EditText mMessageText;
    ImageView mCheckMark;
    RecyclerView mRecyclerView;
    ChatMessagesListAdapter mAdapter;
    String mCourseId;
    DatabaseReference mReference;
    private static final String TAG = "ChatsFragment";
    List<ChatMessage> mMessageList;


    public static ChatsFragment newInstance(){
        return new ChatsFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chats, container, false);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessageList = new ArrayList<>();
        mCheckMark = view.findViewById(R.id.check_mark);
        mMessageText = view.findViewById(R.id.message);
        mReference = FirebaseDatabase.getInstance().getReference();
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        mSharedViewModel.getCourseId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mCourseId = s;
                enableChatroomListener();
                getChatMessages();
            }
        });
        init();
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatMessage message = new ChatMessage();
                message.setMessage_timestamp(DateAndTimeConversion.newInstance().getTimestamp());
                message.setMessage_sender_id(FirebaseAuth.getInstance().getUid());
                message.setMessage(mMessageText.getText().toString());
                String messageId = mReference.child(getString(R.string.dbcourses_node)).child(mCourseId).child(getString(R.string.dbchats_node)).push().getKey();
                message.setMessage_id(messageId);
                mReference.child(getString(R.string.dbcourses_node))
                        .child(mCourseId)
                        .child(getString(R.string.dbchats_node))
                        .child(messageId)
                        .setValue(message);
                mMessageText.setText("");
            }
        });
    }

    private void init(){
        mRecyclerView = getView().findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.scrollToPosition(mMessageList.size()-1);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ChatMessagesListAdapter(mMessageList,getContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getChatMessages(){
        if(mMessageList.size() > 0){
            mMessageList.clear();
        }
        Query query = mReference.child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbchats_node))
                .orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singleSnapshot : snapshot.getChildren()){
                    ChatMessage chatMessage = singleSnapshot.getValue(ChatMessage.class);
                    mMessageList.add(chatMessage);
                }
                init();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: "+error.getMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReference.removeEventListener(mValueEventListener);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getChatMessages();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void enableChatroomListener(){
         /*
            ---------- Listener that will watch the 'chatroom_messages' node ----------
         */
        mReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbcourses_node))
                .child(mCourseId)
                .child(getString(R.string.dbchats_node));

        mReference.addValueEventListener(mValueEventListener);
    }

}