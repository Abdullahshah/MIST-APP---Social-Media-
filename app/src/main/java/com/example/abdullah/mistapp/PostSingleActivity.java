package com.example.abdullah.mistapp;

import android.media.Image;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class PostSingleActivity extends AppCompatActivity {

    private String post_key;

    private DatabaseReference mDatabase;

    private ImageView singlePostImage;
    private TextView singlePostTitle;
    private TextView singlePostDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");

        post_key = getIntent().getExtras().getString("something");
       // Toast.makeText(PostSingleActivity.this, post_key, Toast.LENGTH_SHORT).show();

        singlePostImage = (ImageView) findViewById(R.id.singlePostImage);
        singlePostTitle = (TextView) findViewById(R.id.singlePostTitle);
        singlePostDescription = (TextView) findViewById(R.id.singlePostDescription);

         mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                String post_title = (String) dataSnapshot.child("Title").getValue();
                String post_description = (String) dataSnapshot.child("Description").getValue();
                String post_image = (String) dataSnapshot.child("Image").getValue();
                String post_uid = (String) dataSnapshot.child("uID").getValue();

                singlePostTitle.setText(post_title);
                singlePostDescription.setText(post_description);
                Picasso.with(PostSingleActivity.this).load(post_image).into(singlePostImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    }
