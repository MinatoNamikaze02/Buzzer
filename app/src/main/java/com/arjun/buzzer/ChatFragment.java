package com.arjun.buzzer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.zip.Inflater;

public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();
    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;

    ImageView imageViewOfUser;

    FirestoreRecyclerAdapter<Model, NoteViewHolder> chatAdapter;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerview);
        //Query query = firebaseFirestore.collection("users");
        Query query = firebaseFirestore.collection("users").whereNotEqualTo("UID", firebaseAuth.getUid());
        FirestoreRecyclerOptions<Model> users = new FirestoreRecyclerOptions.Builder<Model>().setQuery(query, Model.class).build();

        chatAdapter = new FirestoreRecyclerAdapter<Model, NoteViewHolder>(users) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull NoteViewHolder holder, int position, @NonNull @NotNull Model model) {
                holder.particularUsername.setText(model.getName());
                String uri = model.getImage();
                Picasso.get().load(uri).into(imageViewOfUser);
                Log.d(TAG, uri.toString());
                if(model.getStatus().equals("Online")){
                    Toast.makeText(getActivity(), "User is Online", Toast.LENGTH_LONG);
                    holder.statusOfUser.setText(model.getStatus());
                    holder.statusOfUser.setTextColor(Color.GREEN);
                }else{
                    holder.statusOfUser.setText(model.getStatus());
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SpecificChat.class);
                        intent.putExtra("name", model.getName());
                        intent.putExtra("recieverUID", model.getUid());
                        intent.putExtra("ImageURI", model.getImage());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout, parent, false);
                return  new NoteViewHolder(view);
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(chatAdapter);

        return view;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{

        private TextView particularUsername;
        private TextView statusOfUser;
        public NoteViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            particularUsername = itemView.findViewById(R.id.nameofuser);
            statusOfUser = itemView.findViewById(R.id.statusofuser);
            imageViewOfUser = itemView.findViewById(R.id.imageviewofuser);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter!=null){
            chatAdapter.stopListening();
        }
    }
    public class LinearLayoutManagerWrapper extends LinearLayoutManager{


        public LinearLayoutManagerWrapper(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
                Log.e(TAG, "No exceptions!!!");
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Recycler View Exception" + e.getMessage());
            }

        }
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }
}