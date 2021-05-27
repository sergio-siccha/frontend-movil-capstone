package com.acj.mobile.android.verifyfacial.utils;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BlinkTracker {
    private final static double OPEN_THRESHOLD = 0.75;
    private final static double CLOSE_THRESHOLD = 0.15;

    private static final BlinkTracker BLINK_TRACKER = new BlinkTracker();

    private static final String TAG = "----" + BlinkTracker.class.getSimpleName();

    private int state = 0;

    public static BlinkTracker getInstance() {
        return BLINK_TRACKER;
    }

    public boolean onUpdate(FirebaseVisionFace face) {
        float left = face.getLeftEyeOpenProbability();
        float right = face.getRightEyeOpenProbability();
        if ((left == FirebaseVisionFace.UNCOMPUTED_PROBABILITY) ||
                (right == FirebaseVisionFace.UNCOMPUTED_PROBABILITY)) {
            // At least one of the eyes was not detected.
//            Log.i(TAG, "UNCOMPUTED_PROBILITY");
            return false;
        }

        switch (state) {
            case 0:
//                Log.d(TAG, "Caso 0");
                if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
//                    Log.i(TAG, "onUpdate: ABIERTO");
                    // Both eyes are initially open
                    state = 1;
                    return false;
                }
                break;

            case 1:
//                Log.d(TAG, "Caso 1");
                if ((left < CLOSE_THRESHOLD) && (right < CLOSE_THRESHOLD)) {
//                    Log.i(TAG, "onUpdate: CERRADO");
                    // Both eyes become closed
                    state = 2;
                    return false;
                }
                break;

            case 2:
//                Log.d(TAG, "Caso 2");
                if ((left > OPEN_THRESHOLD) && (right > OPEN_THRESHOLD)) {
                    // Both eyes are open again
//                    Log.d(TAG, "PESTAÑEO");
                    state = 0;
                    return true;
                }
                break;
        }
        return false;
    }
}
