package com.acj.mobile.android.verifyfacial.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.size.Size;

import java.util.List;

public class FacialAnalizador {

    private Context contexto;

    private static final String TAG = "----" + FacialAnalizador.class.getSimpleName();

    public FacialAnalizador(Context contexto) {
        this.contexto = contexto;
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    public Task<List<FirebaseVisionFace>> correrFacialAnalizador(Size size, byte[] frame, int degrees) {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();
        Log.i(TAG, "correrFacialAnalizador: " + options.isTrackingEnabled());
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(size.getWidth())
                .setHeight(size.getHeight())
                .setRotation(degreesToFirebaseRotation(degrees))
                .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(frame, metadata);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        return detector.detectInImage(image);

    }

    public Task<List<FirebaseVisionFace>> imagenAnalizador(Bitmap bitmap){
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();
        // Formateando imagen para analizarla con Firebase
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        return detector.detectInImage(image);
    }
}
