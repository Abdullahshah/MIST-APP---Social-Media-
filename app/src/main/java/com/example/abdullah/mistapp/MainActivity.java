package com.example.abdullah.mistapp;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView feedList;

    private DatabaseReference databaseReference;

    private DatabaseReference databaseReferenceUsers;

    private DatabaseReference databaseLike; // For the like button

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean ProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth Setup
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    Intent logIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user won't be able to go back to Feed without logging in
                    startActivity(logIntent);
                }

            }
        };

        //database for users
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceUsers.keepSynced(true); // Offline User storage


        //database for feed
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Feed");
        databaseReference.keepSynced(true);

        databaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        databaseLike.keepSynced(true);

        feedList = (RecyclerView) findViewById(R.id.feed_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        feedList.setHasFixedSize(true);
        feedList.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // checkUserExist();
        // Firebase Auth
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Feed, FeedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Feed, FeedViewHolder>(
                // Passing these values in
                Feed.class, R.layout.feed_row, FeedViewHolder.class, databaseReference

        ) {
            @Override
            protected void populateViewHolder(FeedViewHolder viewHolder, Feed model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplication(), model.getImage());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setComments(model.getComments());

                viewHolder.setLikeButton(post_key);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // Toast.makeText(MainActivity.this, post_key, Toast.LENGTH_SHORT).show();
                        Intent singlePostIntent = new Intent(MainActivity.this, PostSingleActivity.class);
                        singlePostIntent.putExtra("something", post_key);
                        startActivity(singlePostIntent);
                    }
                });

                viewHolder.LikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProcessLike = true;

                        databaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (ProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        databaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        ProcessLike = false;
                                    } else {
                                        databaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("Random Value");
                                        ProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }
        };
        feedList.setAdapter(firebaseRecyclerAdapter);
    }

    private void checkUserExist() {
        final String user_id = mAuth.getCurrentUser().getUid();
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user_id)) {
                    Intent i = new Intent(MainActivity.this, SetupActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user won't be able to go back to Feed without logging in
                    startActivity(i);
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageButton LikeButton;

        DatabaseReference databaseLike;
        FirebaseAuth mAuth;

        public FeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            LikeButton = (ImageButton) view.findViewById(R.id.LikeButton);

            databaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            databaseLike.keepSynced(true);


        }

        public void setLikeButton(final String post_key)
        {
            databaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid()))
                    {
                        LikeButton.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    }
                    else
                        LikeButton.setImageResource(R.mipmap.ic_thumb_up_white_24dp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setTitle(String title) {
            TextView post_title = (TextView) view.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDescription(String description) {
            TextView post_desc = (TextView) view.findViewById(R.id.post_description);
            post_desc.setText(description);
        }

        public void setUsername(String username) {
            TextView post_username = (TextView) view.findViewById(R.id.post_username);
            post_username.setText(username);
        }
        public void setComments(String comments)
        {
            TextView post_comments = (TextView) view.findViewById(R.id.commentText);
            post_comments.setText(comments);
        }


        public void setImage(Context ctx, String image) {
            ImageView post_image = (ImageView) view.findViewById(R.id.post_Image);
            Picasso.with(ctx).load(image).into(post_image);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu); // Creates the Menu bar and links it to the xml sheet
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add)
            startActivity(new Intent(MainActivity.this, PostActivity.class)); //Play Button to New Post
        if (item.getItemId() == R.id.action_logout)
            logout();
        if (item.getItemId() == R.id.action_setting)
            startActivity(new Intent(MainActivity.this, SetupActivity.class));
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }
}
