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
import java.util.LinkedList;
import java.util.Queue;

public class ActionClassifier extends Fragment implements SensorEventListener {
    private SensorManager mgr;
    private Sensor accelerometer;
    private TextView resultScreen;
    private final float[] gravFilter = new float[3];
    private final Queue<Float> motionBuffer = new LinkedList<>();
    private static final int BUFFER_SIZE = 35;
    private static final float SMOOTH = 0.85f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        resultScreen = new TextView(requireContext());
        resultScreen.setTextSize(26);
        resultScreen.setPadding(28, 28, 28, 28);
        mgr = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        return resultScreen;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) mgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        else resultScreen.setText("Accéléromètre absent");
    }

    @Override public void onPause() { super.onPause(); mgr.unregisterListener(this); }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float ax = e.values[0], ay = e.values[1], az = e.values[2];
        gravFilter[0] = SMOOTH * gravFilter[0] + (1 - SMOOTH) * ax;
        gravFilter[1] = SMOOTH * gravFilter[1] + (1 - SMOOTH) * ay;
        gravFilter[2] = SMOOTH * gravFilter[2] + (1 - SMOOTH) * az;
        float linX = ax - gravFilter[0], linY = ay - gravFilter[1], linZ = az - gravFilter[2];
        float motion = (float) Math.sqrt(linX*linX + linY*linY + linZ*linZ);

        motionBuffer.add(motion);
        if (motionBuffer.size() > BUFFER_SIZE) motionBuffer.poll();

        String guess = analyzeActivity(motionBuffer);
        resultScreen.setText(String.format("📳 Mouvement: %.2f\n🎯 Activité: %s", motion, guess));
    }

    private String analyzeActivity(Queue<Float> buffer) {
        if (buffer.size() < BUFFER_SIZE) return "🔍 Calibration...";
        float sum = 0, maxVal = 0;
        for (float v : buffer) { sum += v; maxVal = Math.max(maxVal, v); }
        float mean = sum / buffer.size();
        float var = 0;
        for (float v : buffer) var += (v - mean)*(v - mean);
        float std = (float) Math.sqrt(var / buffer.size());

        if (maxVal > 12f) return "🤸 SAUT détecté";
        if (std > 1.4f) return "🚶 MARCHE active";
        if (Math.abs(gravFilter[2]) > 9f) return "📱 Stable / à plat";
        if (Math.abs(gravFilter[1]) > 7f || Math.abs(gravFilter[0]) > 7f) return "🪑 Assis ou debout";
        return "💤 Inactif";
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}
}
