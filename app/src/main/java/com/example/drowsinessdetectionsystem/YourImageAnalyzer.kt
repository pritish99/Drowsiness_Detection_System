package com.example.drowsinessdetectionsystem

import android.annotation.SuppressLint
import android.util.Log
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class YourImageAnalyzer(textView1: TextView) : ImageAnalysis.Analyzer {
    private val textView = textView1
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            val options = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .enableTracking()
                    .build()

            val detector = FaceDetection.getClient(options)



            // [START run_detector]
            val result = detector.process(image)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        // [START_EXCLUDE]
                        // [START get_face_info]
                        for (face in faces) {
                            val bounds = face.boundingBox
                            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
                            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                            // nose available):

                            if (face.rightEyeOpenProbability != null && face.leftEyeOpenProbability != null) {
                                val rightEyeOpenProb = face.rightEyeOpenProbability
                                val leftEyeOpenProb = face.leftEyeOpenProbability
                                if(rightEyeOpenProb >=0.99 || leftEyeOpenProb>=0.99){
                                    textView.setText("Eyes Open").toString()
                                }
                                else{
                                    textView.setText("Eyes Close").toString()
                                }

                            }



                            // If face tracking was enabled:
                            if (face.trackingId != null) {
                                val id = face.trackingId
                            }


                        }
                        // [END get_face_info]
                        // [END_EXCLUDE]
                        imageProxy.close()

                    }
                    .addOnFailureListener { e ->
                        Log.e("CameraXBasic", "Detection Error")
                        imageProxy.close()
                    }
            // [END run_detector]
            //System.out.println(result.result)
        }

    }

}
