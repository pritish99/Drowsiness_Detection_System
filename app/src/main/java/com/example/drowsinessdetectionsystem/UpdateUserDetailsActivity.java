package com.example.drowsinessdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class UpdateUserDetailsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    User users;
    DatabaseReference reff;
    EditText name,age,emergency_contact;
    AlertDialog mProgressDialog;
    Button update_details;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_details);
        mAuth=FirebaseAuth.getInstance();
        users=new User();
        name=findViewById(R.id.name_edit_holder);
        age=findViewById(R.id.age_edit_holder);
        back=findViewById(R.id.left_arrow_user_details_update);
        update_details=findViewById(R.id.update_edit_details);
        emergency_contact=findViewById(R.id.emergency_contact_edit_holder);
        FirebaseUser user = mAuth.getCurrentUser();
        reff= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mProgressDialog = new SpotsDialog(this, R.style.Custom);
        mProgressDialog.show();
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    String user_name = snapshot.child("name").getValue(String.class);
                    String user_age = snapshot.child("age").getValue(String.class);
                    String user_emergency_contact = snapshot.child("emergency_contact").getValue(String.class);
                    name.setText(user_name);
                    if(user_age==null || user_emergency_contact==null ){
                        Toast.makeText(getApplicationContext(),"Kindly update details",Toast.LENGTH_SHORT).show();
                        if((user_age==null)){
                            age.setText("Yet to update");
                        }
                        else {
                            age.setText(user_age);
                        }
                        if(user_emergency_contact==null){
                            emergency_contact.setText("Yet to update");
                        }
                        else {
                            emergency_contact.setText(user_emergency_contact);
                        }
                    }
                    else {
                        emergency_contact.setText(user_emergency_contact);
                        age.setText(user_age);


                    }

                    mProgressDialog.dismiss();
                }
                else {
                    mProgressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        update_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                String input_name=name.getText().toString();
                String input_age=age.getText().toString();
                String input_emergency_contact=emergency_contact.getText().toString();
                if(input_name.equals("") || input_age.equals("Yet to update")|| input_emergency_contact.equals("Yet to update") || input_age.equals("") || input_emergency_contact.equals("")){
                    Toast.makeText(getApplicationContext(),"Kindly fill all fields",Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();

                }
                else if(input_age.length()>2 || input_emergency_contact.length()!=10){
                    Toast.makeText(getApplicationContext(),"Kindly fill all fields correctly",Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
                }
                else {
                    users.setName(input_name);
                    users.setAge(input_age);
                    users.setEmergency_contact(input_emergency_contact);
                    reff=FirebaseDatabase.getInstance().getReference().child("Users");
                    reff.child(user.getUid()).setValue(users);
                    Toast.makeText(getApplicationContext(), "Updated",
                            Toast.LENGTH_SHORT).show();
                    finish();

                    Intent intent=new Intent(getApplicationContext(),UserDetailsActivity.class);
                    startActivity(intent);
                    mProgressDialog.dismiss();
                }


            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),UserDetailsActivity.class));
                finish();
            }
        });
    }
}