package com.example.drowsinessdetectionsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;

public class UserPageActivity extends AppCompatActivity {
    TextView user_name_holder;
    AlertDialog progressDialog;
    private FirebaseAuth mAuth;
    ImageButton signout_button;
    Button user_details,start_monitoring;
    SwitchCompat cameraSwitch;
    String useCamera="Front Camera";;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        user_name_holder=findViewById(R.id.user_name_holder);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new SpotsDialog(this, R.style.Custom);
        start_monitoring=findViewById(R.id.start_monitoring);
        signout_button=findViewById(R.id.signout_button);
        cameraSwitch=findViewById(R.id.camera_switch);
        user_details=findViewById(R.id.user_details);

        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    cameraSwitch.setText("Front Camera");
                    useCamera="Front Camera";
                }
                else{
                    cameraSwitch.setText("Rear Camera");
                    useCamera="Rear Camera";

                }
            }
        });



        user_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),UserDetailsActivity.class));
            }
        });

        signout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                finish();
            }
        });

        start_monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(getApplicationContext(),MonitoringActivity.class);
                intent1.putExtra("Camera",useCamera);
                startActivity(intent1);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        progressDialog.show();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            user_name_holder.setText("Welcome "+currentUser.getDisplayName());
        }
        progressDialog.dismiss();
    }
}