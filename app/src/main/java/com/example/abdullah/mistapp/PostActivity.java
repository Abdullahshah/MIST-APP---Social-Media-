package com.example.abdullah.mistapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class PostActivity extends AppCompatActivity {

    private ImageButton selectImage;
    private static final int pickImageRequest = 1;
    private Uri imageUri;

    private EditText postTitle;
    private EditText postDescription;

    private Button submitButton;

    //Storage variables
    private StorageReference storageReference;

    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    //New stuff
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        //Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        //Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Feed");

        //New stuff
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid().toString());

        progressDialog = new ProgressDialog(this);

        // Image Switcher Button
        selectImage = (ImageButton) findViewById(R.id.imageButton);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, pickImageRequest);
            }
        });

        postTitle = (EditText) findViewById(R.id.titleField);
        postDescription = (EditText) findViewById(R.id.descriptionField);

        submitButton = (Button) findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }

    // Post Process to Database
    private void startPosting() {
        progressDialog.setMessage("Posting to Feed");

        final String title_val = postTitle.getText().toString().trim();
        final String description_val = postDescription.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(description_val) && imageUri != null) {
            progressDialog.show();
            StorageReference filepath = storageReference.child("Feed_Images").child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uploads to Storage
                    @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    //Uploads to Database
                    final DatabaseReference newPost = databaseReference.push();



                    mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("Title").setValue(title_val);
                            newPost.child("Description").setValue(description_val);
                            newPost.child("Image").setValue(downloadUrl.toString());
                            newPost.child("uID").setValue(mCurrentUser.getUid());
                            newPost.child("Username").setValue(dataSnapshot.child("Name").getValue());
                            newPost.child("Comments").setValue(Integer.toString(0));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    progressDialog.dismiss();
                    startActivity(new Intent(PostActivity.this, MainActivity.class));

                }
            });
        }

    }



    // Sets image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            selectImage.setImageURI(imageUri);
        }
    }

            }