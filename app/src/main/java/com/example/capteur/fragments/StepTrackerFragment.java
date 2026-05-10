package com.example.capteur.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StepTrackerFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorHandler;
    private Sensor stepCounterDevice;
    private TextView stepDisplay;

    private float bootStepsReference = -1;

    private final ActivityResultLauncher<String> permissionRequester =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    permissionAccepted -> {
                        if (permissionAccepted) {
                            activateStepSensor();
                        } else {
                            stepDisplay.setText("Permission refusée.");
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        stepDisplay = new TextView(requireContext());
        stepDisplay.setTextSize(22);
        stepDisplay.setPadding(24, 24, 24, 24);

        sensorHandler = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        stepCounterDevice = sensorHandler.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return stepDisplay;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (stepCounterDevice == null) {
            stepDisplay.setText("Pas de capteur de pas sur cet appareil.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionRequester.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            activateStepSensor();
        }
    }

    private void activateStepSensor() {
        sensorHandler.registerListener(
                this,
                stepCounterDevice,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorHandler.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float totalStepsSinceReboot = sensorEvent.values[0];

        if (bootStepsReference < 0) {
            bootStepsReference = totalStepsSinceReboot;
        }

        int currentSessionSteps = (int) (totalStepsSinceReboot - bootStepsReference);

        stepDisplay.setText(
                "Pas total depuis démarrage : " + (int) totalStepsSinceReboot
                        + "\n\nPas de cette session : " + currentSessionSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisionLevel) {
    }
}
