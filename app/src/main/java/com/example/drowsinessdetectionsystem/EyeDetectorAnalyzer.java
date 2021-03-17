package com.example.drowsinessdetectionsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EyeDetectorAnalyzer implements ImageAnalysis.Analyzer {
    TextView textview1;
    volatile boolean stopThread = false;
    boolean startThread = true;
    boolean startSMSThread = true;
    boolean showEyesClose = false;
    boolean messageSent = false;
    MediaPlayer buzzer;
    String user_emergency_contact;
    boolean setFirebase = true;
    double latitude,longitude;

    FusedLocationProviderClient fusedLocationProviderClient;

    public EyeDetectorAnalyzer(TextView textview1, MediaPlayer buzzer) {
        this.textview1 = textview1;
        this.buzzer = buzzer;
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        if (setFirebase) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(textview1.getContext());
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            DatabaseReference reff = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            reff.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        user_emergency_contact = snapshot.child("emergency_contact").getValue(String.class);
                        setFirebase = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .build();
        @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);
            @SuppressLint("SetTextI18n") Task<List<Face>> result =
                    detector.process(image)
                            .addOnSuccessListener(
                                    faces -> {
                                        for (Face face : faces) {
                                            if (face.getRightEyeOpenProbability() != null || face.getLeftEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                if (rightEyeOpenProb >= 0.99 || leftEyeOpenProb >= 0.99) {
                                                    textview1.setText("Eyes Open");
                                                    stopThread = true;
                                                } else {
                                                    startThread();
                                                    if (showEyesClose) {
                                                        textview1.setText("Eyes Close");
                                                        if (!messageSent) {
                                                            startSmsThread();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        imageProxy.close();
                                    })
                            .addOnFailureListener(
                                    e -> imageProxy.close());
        }
    }


    public void startThread() {
        WaitForEyesThread runnable1 = new WaitForEyesThread();
        Thread waitThread = new Thread(runnable1);
        stopThread = false;
        if (startThread) {
            waitThread.start();
            startThread = false;
        }
    }

    //Thread for Buzzer
    class WaitForEyesThread implements Runnable {
        int count = 4;

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                if (stopThread) {
                    startThread = true;
                    showEyesClose = false;
                    return;
                }
                if (i == count - 1) {
                    startThread = true;
                    showEyesClose = true;
                    buzzer.start();
                    return;
                }
                Log.i("Wait thread Count", "Count:" + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startSmsThread() {
        stopThread = false;
        sendSmsThread runnable2 = new sendSmsThread();
        Thread waitThread2 = new Thread(runnable2);
        if (startSMSThread) {
            waitThread2.start();
            startSMSThread = false;
        }
    }

    //Thread for sending message
    class sendSmsThread implements Runnable {
        int count = 2;

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                if (stopThread) {
                    startSMSThread = true;
                    showEyesClose = false;
                    return;
                }
                if (i == count - 1) {
                    try {
                        SmsManager manager = SmsManager.getDefault();
                        if (ActivityCompat.checkSelfPermission(textview1.getContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        Geocoder geocoder = new Geocoder(textview1.getContext(),Locale.getDefault());
                                            List<Address> addresses = null;
                                            try {
                                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (addresses.size() >= 0) {
                                                Barcode.GeoPoint p = new Barcode.GeoPoint(
                                                        (int) (addresses.get(0).getLatitude() * 1E6),
                                                        (int) (addresses.get(0).getLongitude() * 1E6));
                                            latitude = p.lat;
                                            longitude = p.lng;
                                            String message="I need assistance at http://www.google.com/maps/place/"+String.valueOf(latitude)+","+String.valueOf(longitude);
                                            manager.sendTextMessage(user_emergency_contact,null,message,null,null);
                                        }
                                    }
                                }
                            });
                        }

                        Log.i("Eye Detector Activity","Message sent");
                        messageSent=true;
                        startSMSThread=false;
                        return;
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                return;
                }

                Log.i("Sleep thread Count","Count:"+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
