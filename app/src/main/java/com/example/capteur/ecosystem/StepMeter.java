package com.example.capteur.ecosystem;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StepMeter extends Fragment implements SensorEventListener {
    private SensorManager mgr;
    private Sensor stepSensor;
    private TextView stepDisplay;
    private float baseline = -1;

    private final ActivityResultLauncher<String> permRequest = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) startCounting(); else stepDisplay.setText("Permission refusée"); });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle saved) {
        stepDisplay = new TextView(requireContext());
        stepDisplay.setTextSize(28);
        stepDisplay.setPadding(32, 32, 32, 32);
        mgr = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = mgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return stepDisplay;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stepSensor == null) stepDisplay.setText("❌ Pas de capteur de pas");
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            permRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        else startCounting();
    }

    private void startCounting() { mgr.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL); }

    @Override public void onPause() { super.onPause(); mgr.unregisterListener(this); }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float total = e.values[0];
        if (baseline < 0) baseline = total;
        int session = (int)(total - baseline);
        stepDisplay.setText(String.format("👣 Total depuis boot: %.0f\n🚶 Pas de la session: %d", total, session));
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}
}
