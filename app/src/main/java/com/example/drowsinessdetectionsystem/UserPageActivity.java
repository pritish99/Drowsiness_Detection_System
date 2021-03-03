package com.example.drowsinessdetectionsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
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
    Spinner sounds_list;
    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        user_name_holder=findViewById(R.id.user_name_holder);
        mAuth = FirebaseAuth.getInstance();
        sounds_list=findViewById(R.id.alert_settings);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        start_monitoring=findViewById(R.id.start_monitoring);
        signout_button=findViewById(R.id.signout_button);
        cameraSwitch=findViewById(R.id.camera_switch);
        user_details=findViewById(R.id.user_details);

        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sounds_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner*/


        String sounds[]=new String[]{"Buzzer","Alarm","Male Voice","Female Voice"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner,sounds);
        sounds_list.setPopupBackgroundResource(R.color.bright_orange);
        sounds_list.setAdapter(adapter);

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
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent1 = new Intent(getApplicationContext(), MonitoringActivity.class);
                    intent1.putExtra("Camera", useCamera);
                    startActivity(intent1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                }
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