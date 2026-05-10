package com.example.capteur.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.Queue;

public class ActionDetectorFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorService;
    private Sensor accelSensor;

    private TextView detectionResultView;

    private final float[] gravityFilter = new float[3];
    private final Queue<Float> motionBuffer = new LinkedList<>();

    private static final int BUFFER_CAPACITY = 30;
    private static final float SMOOTHING_FACTOR = 0.8f;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        detectionResultView = new TextView(requireContext());
        detectionResultView.setTextSize(22);
        detectionResultView.setPadding(24, 24, 24, 24);

        sensorService = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        accelSensor = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return detectionResultView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelSensor != null) {
            sensorService.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            detectionResultView.setText("Accéléromètre non disponible.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorService.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float rawX = sensorEvent.values[0];
        float rawY = sensorEvent.values[1];
        float rawZ = sensorEvent.values[2];

        gravityFilter[0] = SMOOTHING_FACTOR * gravityFilter[0] + (1 - SMOOTHING_FACTOR) * rawX;
        gravityFilter[1] = SMOOTHING_FACTOR * gravityFilter[1] + (1 - SMOOTHING_FACTOR) * rawY;
        gravityFilter[2] = SMOOTHING_FACTOR * gravityFilter[2] + (1 - SMOOTHING_FACTOR) * rawZ;

        float linearMotionX = rawX - gravityFilter[0];
        float linearMotionY = rawY - gravityFilter[1];
        float linearMotionZ = rawZ - gravityFilter[2];

        float motionIntensity = (float) Math.sqrt(
                linearMotionX * linearMotionX +
                        linearMotionY * linearMotionY +
                        linearMotionZ * linearMotionZ);

        storeMotionValue(motionIntensity);

        String recognizedAction = analyzeCurrentAction(rawX, rawY, rawZ);

        detectionResultView.setText(
                "X brut : " + rawX + "\n" +
                        "Y brut : " + rawY + "\n" +
                        "Z brut : " + rawZ + "\n\n" +
                        "Mouvement : " + motionIntensity + "\n\n" +
                        "Action détectée : " + recognizedAction);
    }

    private void storeMotionValue(float intensity) {
        if (motionBuffer.size() >= BUFFER_CAPACITY) {
            motionBuffer.poll();
        }
        motionBuffer.add(intensity);
    }

    private String analyzeCurrentAction(float xVal, float yVal, float zVal) {

        if (motionBuffer.size() < BUFFER_CAPACITY) {
            return "Initialisation en cours...";
        }

        float meanMotion = 0f;
        float peakMotion = 0f;

        for (float value : motionBuffer) {
            meanMotion += value;
            peakMotion = Math.max(peakMotion, value);
        }
        meanMotion = meanMotion / motionBuffer.size();

        float varianceSum = 0f;
        for (float value : motionBuffer) {
            varianceSum += (value - meanMotion) * (value - meanMotion);
        }
        float motionStdDev = (float) Math.sqrt(varianceSum / motionBuffer.size());

        if (peakMotion > 10f) return "SAUT détecté !";
        if (motionStdDev > 1.2f) return "MARCHE en cours";
        if (Math.abs(zVal) > 8f) return "Stable / Téléphone à plat";
        if (Math.abs(yVal) > 7f || Math.abs(xVal) > 7f) return "Assis ou debout selon orientation";
        return "Position calme";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisionLevel) {
    }
}
