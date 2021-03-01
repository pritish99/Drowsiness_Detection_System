package com.example.drowsinessdetectionsystem;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.media.Image;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import java.util.List;

public class EyeDetectorAnalyzer implements ImageAnalysis.Analyzer {
    TextView textview1;
    volatile boolean stopThread=false;
    boolean startThread=true;
    boolean showEyesClose=false;
    MediaPlayer buzzer;
    public EyeDetectorAnalyzer(TextView textview1,MediaPlayer buzzer){
        this.textview1=textview1;
        this.buzzer=buzzer;
    }
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
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
                                            /*Rect bounds = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees*/

                                            if (face.getRightEyeOpenProbability() != null || face.getLeftEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                if(rightEyeOpenProb >=0.99 || leftEyeOpenProb>=0.99){
                                                    textview1.setText("Eyes Open");
                                                    stopThread=true;
                                                }
                                                else{
                                                    //Log.i("Thread","Threat State"+waitThread.getState());
                                                    startThread();
                                                    if(showEyesClose){
                                                        textview1.setText("Eyes Close");

                                                    }
                                                }
                                            }
                                            // If face tracking was enabled:
                                            /*if (face.getTrackingId() != null) {
                                                int id = face.getTrackingId();
                                            }*/
                                        }
                                        imageProxy.close();
                                    })
                            .addOnFailureListener(
                                    e -> imageProxy.close());
        }
    }
    public void startThread(){

        WaitForEyesThread runnable1=new WaitForEyesThread();
        Thread waitThread=new Thread(runnable1);
        stopThread=false;
        if(startThread){
            waitThread.start();
            startThread=false;
        }
    }

    class WaitForEyesThread implements Runnable {

        int count=4;
        @Override
        public void run() {
            for(int i=0;i<count;i++){
                if(stopThread){
                    startThread=true;
                    showEyesClose=false;
                    return;
                }
                if(i==count-1){
                    startThread=true;
                    showEyesClose=true;
                    buzzer.start();
                    return;
                }
                Log.i("Thread Count","Count:"+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
