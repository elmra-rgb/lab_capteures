package com.example.capteur.ecosystem;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.capteur.widgets.DynamicPlotView;

public class MeasureChart extends Fragment implements SensorEventListener {

    private static final String ARG_TYPE = "sensor_type";
    private static final String ARG_TITLE = "label";
    private static final String ARG_MODE = "calc_mode";

    private SensorManager sensorMgr;
    private Sensor activeSensor;
    private TextView displayValue;
    private DynamicPlotView graphView;
    private int sensorCode;
    private String titleText;
    private String operationMode;
    private Handler simHandler = new Handler(Looper.getMainLooper());
    private float simCycle = 0f;

    public static MeasureChart newFactory(int type, String title, String mode) {
        MeasureChart frag = new MeasureChart();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MODE, mode);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        if (getArguments() != null) {
            sensorCode = getArguments().getInt(ARG_TYPE);
            titleText = getArguments().getString(ARG_TITLE);
            operationMode = getArguments().getString(ARG_MODE);
        }

        sensorMgr = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        activeSensor = sensorMgr.getDefaultSensor(sensorCode);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        TextView header = new TextView(requireContext());
        header.setText(titleText);
        header.setTextSize(24);
        header.setPadding(0, 0, 0, 24);

        displayValue = new TextView(requireContext());
        displayValue.setTextSize(18);
        displayValue.setPadding(0, 0, 0, 24);

        graphView = new DynamicPlotView(requireContext());
        graphView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 550));

        root.addView(header);
        root.addView(displayValue);
        root.addView(graphView);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activeSensor != null) {
            sensorMgr.registerListener(this, activeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            displayValue.setText("❌ Capteur absent - mode simulation activé");
            startEmulator();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
        simHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = extractValue(event.values);
        refreshUI(value);
    }

    private float extractValue(float[] raw) {
        if ("NORM".equals(operationMode)) {
            return (float) Math.sqrt(raw[0]*raw[0] + raw[1]*raw[1] + raw[2]*raw[2]);
        }
        return raw[0];
    }

    private void refreshUI(float val) {
        displayValue.setText(String.format("📊 %.2f", val));
        graphView.feedData(val);
    }

    private void startEmulator() {
        simHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simCycle++;
                float simulated;
                if (sensorCode == Sensor.TYPE_AMBIENT_TEMPERATURE) simulated = 22 + (float) Math.sin(simCycle/6f)*4;
                else if (sensorCode == Sensor.TYPE_RELATIVE_HUMIDITY) simulated = 50 + (float) Math.sin(simCycle/5f)*20;
                else if (sensorCode == Sensor.TYPE_PROXIMITY) simulated = (simCycle % 4 < 2) ? 0f : 6f;
                else simulated = 40 + (float) Math.sin(simCycle/3f)*15;
                refreshUI(simulated);
                simHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override public void onAccuracyChanged(Sensor s, int acc) {}
}
