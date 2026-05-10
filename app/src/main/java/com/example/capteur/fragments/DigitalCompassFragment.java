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

public class DigitalCompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorService;
    private Sensor accelerometerDevice;
    private Sensor magnetometerDevice;

    private TextView orientationOutput;

    private final float[] gravityEstimate = new float[3];
    private final float[] magneticEstimate = new float[3];

    private boolean gravityReady = false;
    private boolean magneticReady = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        orientationOutput = new TextView(requireContext());
        orientationOutput.setTextSize(22);
        orientationOutput.setPadding(24, 24, 24, 24);

        sensorService = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        accelerometerDevice = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerDevice = sensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return orientationOutput;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelerometerDevice != null) {
            sensorService.registerListener(this, accelerometerDevice, SensorManager.SENSOR_DELAY_UI);
        }

        if (magnetometerDevice != null) {
            sensorService.registerListener(this, magnetometerDevice, SensorManager.SENSOR_DELAY_UI);
        }

        if (accelerometerDevice == null || magnetometerDevice == null) {
            orientationOutput.setText("Boussole impossible : capteurs manquants.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorService.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(sensorEvent.values, 0, gravityEstimate, 0, 3);
            gravityReady = true;
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(sensorEvent.values, 0, magneticEstimate, 0, 3);
            magneticReady = true;
        }

        if (gravityReady && magneticReady) {
            float[] rotationMatrix = new float[9];
            float[] orientationAngles = new float[3];

            boolean matrixComputed = SensorManager.getRotationMatrix(
                    rotationMatrix,
                    null,
                    gravityEstimate,
                    magneticEstimate);

            if (matrixComputed) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles);

                float azimuthRad = orientationAngles[0];
                float azimuthDeg = (float) Math.toDegrees(azimuthRad);

                if (azimuthDeg < 0) {
                    azimuthDeg += 360;
                }

                orientationOutput.setText(
                        "Cap magnétique : " + azimuthDeg + "°\n"
                                + getCardinalDirection(azimuthDeg));
            }
        }
    }

    private String getCardinalDirection(float angleDeg) {
        if (angleDeg >= 337.5 || angleDeg < 22.5) return "Nord";
        else if (angleDeg < 67.5) return "Nord-Est";
        else if (angleDeg < 112.5) return "Est";
        else if (angleDeg < 157.5) return "Sud-Est";
        else if (angleDeg < 202.5) return "Sud";
        else if (angleDeg < 247.5) return "Sud-Ouest";
        else if (angleDeg < 292.5) return "Ouest";
        else return "Nord-Ouest";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisionLevel) {
    }
}
