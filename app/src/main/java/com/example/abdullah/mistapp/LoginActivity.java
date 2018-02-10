package com.example.abdullah.mistapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    Button needtoRegisterButton;

    private EditText LoginEmailField;
    private EditText LoginPasswordField;
    private Button LoginButton;

    private FirebaseAuth mAuth;

    // DatabaseReference to check if account is legit or not
    private DatabaseReference databaseReferenceUsers;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceUsers.keepSynced(true);

        progressDialog = new ProgressDialog(this);

        LoginButton = (Button) findViewById(R.id.loginButton);
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }


        });


        needtoRegisterButton = (Button) findViewById(R.id.needtoRegisterButton);
        needtoRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });

        LoginEmailField = (EditText) findViewById(R.id.loginEmailField);
        LoginPasswordField = (EditText) findViewById(R.id.loginPasswordField);



    }

    private void checkLogin() {
        String email = LoginEmailField.getText().toString().trim();
        String password = LoginPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            progressDialog.setMessage("Checking Login . . .");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {
                        progressDialog.dismiss();
                        checkUserExist();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Email or Password is Incorrect", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

    private void checkUserExist() {
        final String user_id = mAuth.getCurrentUser().getUid();
        databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id))
                {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // user won't be able to go back to Feed without logging in
                    startActivity(i);
                } else {
                    Intent i = new Intent(LoginActivity.this, SetupActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



}
}
