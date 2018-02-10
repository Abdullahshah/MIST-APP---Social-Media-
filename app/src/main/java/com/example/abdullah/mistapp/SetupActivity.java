package com.example.abdullah.mistapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupActivity extends AppCompatActivity {

    private ImageButton SetupImageButton;
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText phoneNumberField;
    private Button SubmitButton;
    private Uri imageUri;

    private static final int pickImageRequest = 1;

    private DatabaseReference databaseReferenceUsers;
    private FirebaseAuth mAuth;
    private StorageReference storageImage;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SetupImageButton = (ImageButton) findViewById(R.id.profileImageButton);
        firstNameField = (EditText) findViewById(R.id.setupFirstNameField);
        lastNameField = (EditText) findViewById(R.id.setupLastNameField);
        phoneNumberField = (EditText) findViewById(R.id.setupPhoneField);
        SubmitButton = (Button) findViewById(R.id.submitButton);

        SetupImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, pickImageRequest);
            }
        });

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        storageImage = FirebaseStorage.getInstance().getReference().child("Profile_Images"); // All profile images are in their own separate branch
        progressDialog = new ProgressDialog(this);

    }

    public void buttonOnClick(View v) {
        startSetupAccount();
        
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void startSetupAccount() {
        final String fname = firstNameField.getText().toString().trim();
        final String lastname = lastNameField.getText().toString().trim();
        final String user_id = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(fname) && !TextUtils.isEmpty(lastname) && SetupImageButton != null)
        {
            progressDialog.setMessage("Finalizing Profile");
            progressDialog.show();
            StorageReference filepath = storageImage.child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    try {
                        @SuppressWarnings("VisibleForTests") String downloadUri = taskSnapshot.getDownloadUrl().toString();
                        databaseReferenceUsers.child(user_id).child("Name").setValue(fname + " " + lastname);
                        databaseReferenceUsers.child(user_id).child("Image").setValue(downloadUri);
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SetupActivity.this, "Please Choose a Profile Picture", Toast.LENGTH_SHORT);
                    }

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            SetupImageButton.setImageURI(imageUri);
        }
    }
}
