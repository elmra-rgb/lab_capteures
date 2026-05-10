package com.example.capteur.ecosystem;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class OrientationGuide extends Fragment implements SensorEventListener {
    private SensorManager manager;
    private Sensor accel, magnet;
    private TextView compassOutput;
    private final float[] gravityStore = new float[3];
    private final float[] magneticStore = new float[3];
    private boolean gotGravity = false, gotMagnetic = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        compassOutput = new TextView(requireContext());
        compassOutput.setTextSize(28);
        compassOutput.setPadding(32, 32, 32, 32);
        manager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnet = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        return compassOutput;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accel != null) manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        if (magnet != null) manager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_UI);
        if (accel == null || magnet == null) compassOutput.setText("🔴 Boussole non disponible");
    }

    @Override public void onPause() { super.onPause(); manager.unregisterListener(this); }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(e.values, 0, gravityStore, 0, 3);
            gotGravity = true;
        }
        if (e.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(e.values, 0, magneticStore, 0, 3);
            gotMagnetic = true;
        }
        if (gotGravity && gotMagnetic) {
            float[] rotMat = new float[9], orientation = new float[3];
            if (SensorManager.getRotationMatrix(rotMat, null, gravityStore, magneticStore)) {
                SensorManager.getOrientation(rotMat, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                if (azimuth < 0) azimuth += 360;
                compassOutput.setText(String.format("🧭 %.1f°\n%s", azimuth, directionLabel(azimuth)));
            }
        }
    }

    private String directionLabel(float deg) {
        if (deg >= 337.5 || deg < 22.5) return "⬆️ NORD";
        if (deg < 67.5) return "↗️ NORD-EST";
        if (deg < 112.5) return "➡️ EST";
        if (deg < 157.5) return "↘️ SUD-EST";
        if (deg < 202.5) return "⬇️ SUD";
        if (deg < 247.5) return "↙️ SUD-OUEST";
        if (deg < 292.5) return "⬅️ OUEST";
        return "↖️ NORD-OUEST";
    }

    @Override public void onAccuracyChanged(Sensor s, int acc) {}
}
