package com.example.drowsinessdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dmax.dialog.SpotsDialog;

public class SignUpActivity extends AppCompatActivity {
    EditText signup_name,signup_email,signup_password;
    Button signup_button;
    private FirebaseAuth mAuth;
    User users;
    DatabaseReference reff;
    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        signup_name=findViewById(R.id.signup_name);
        signup_email=findViewById(R.id.signup_email);
        signup_password=findViewById(R.id.signup_password);
        signup_button=findViewById(R.id.signup_button);
        users=new User();
        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_email=signup_email.getText().toString();
                String input_password=signup_password.getText().toString();
                String input_name=signup_name.getText().toString();
                if(!input_email.isEmpty() && !input_password.isEmpty()){
                    userRegister(input_email,input_password,input_name);
                }
            }
        });
    }

    private void userRegister(String input_email, String input_password,String user_name) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(input_email, input_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign Up Successful", "createUserWithEmail:success");
                            Toast.makeText(getApplicationContext(), "Registeration successful",
                                    Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user_name).build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Updated", "User profile updated.");
                                            }
                                        }
                                    });

                            users.setName(user_name);
                            reff=FirebaseDatabase.getInstance().getReference().child("Users");
                            reff.child(user.getUid()).setValue(users);

                            Intent intent=new Intent(getApplicationContext(),UserPageActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign Up Failed", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Registration failed",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                        // ...
                    }
                });
    }
}