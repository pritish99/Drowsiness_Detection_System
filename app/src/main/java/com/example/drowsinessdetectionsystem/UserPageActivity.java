package com.example.drowsinessdetectionsystem;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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
    String useCamera="Front Camera";
    @SuppressLint("SetTextI18n")
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

        cameraSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                cameraSwitch.setText("Front Camera");
                useCamera="Front Camera";
            }
            else{
                cameraSwitch.setText("Rear Camera");
                useCamera="Rear Camera";

            }
        });



        user_details.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UserDetailsActivity.class)));

        signout_button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            finish();
        });

        start_monitoring.setOnClickListener(v -> {
            Intent intent1=new Intent(getApplicationContext(),MonitoringActivity.class);
            intent1.putExtra("Camera",useCamera);
            startActivity(intent1);
        });
    }
    @SuppressLint("SetTextI18n")
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