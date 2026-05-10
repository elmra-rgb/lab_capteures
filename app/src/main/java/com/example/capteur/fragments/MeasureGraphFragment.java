package com.example.capteur.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.capteur.widgets.DynamicLineChart;

public class MeasureGraphFragment extends Fragment implements SensorEventListener {

    private static final String PARAM_SENSOR_TYPE = "sensor_type_id";
    private static final String PARAM_SCREEN_TITLE = "screen_title";
    private static final String PARAM_VALUE_MODE = "value_extraction_mode";

    private SensorManager sensorService;
    private Sensor activeSensor;

    private TextView currentValueDisplay;
    private DynamicLineChart measurementChart;

    private int targetSensorType;
    private String fragmentTitle;
    private String extractionMode;

    private final Handler simulationWorker = new Handler(Looper.getMainLooper());
    private float simulatedTime = 0f;

    public static MeasureGraphFragment buildInstance(
            int sensorTypeCode,
            String titleText,
            String modeType) {

        MeasureGraphFragment newFragment = new MeasureGraphFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(PARAM_SENSOR_TYPE, sensorTypeCode);
        arguments.putString(PARAM_SCREEN_TITLE, titleText);
        arguments.putString(PARAM_VALUE_MODE, modeType);

        newFragment.setArguments(arguments);
        return newFragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        targetSensorType = requireArguments().getInt(PARAM_SENSOR_TYPE);
        fragmentTitle = requireArguments().getString(PARAM_SCREEN_TITLE);
        extractionMode = requireArguments().getString(PARAM_VALUE_MODE);

        sensorService = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        activeSensor = sensorService.getDefaultSensor(targetSensorType);

        LinearLayout mainLayout = new LinearLayout(requireContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(24, 24, 24, 24);

        TextView headerText = new TextView(requireContext());
        headerText.setText(fragmentTitle);
        headerText.setTextSize(22);
        headerText.setPadding(0, 0, 0, 20);

        currentValueDisplay = new TextView(requireContext());
        currentValueDisplay.setTextSize(18);
        currentValueDisplay.setPadding(0, 0, 0, 20);

        measurementChart = new DynamicLineChart(requireContext());
        measurementChart.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        600));

        mainLayout.addView(headerText);
        mainLayout.addView(currentValueDisplay);
        mainLayout.addView(measurementChart);

        return mainLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (activeSensor != null) {
            sensorService.registerListener(
                    this,
                    activeSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            currentValueDisplay.setText("Capteur absent. Mode simulation activé.");
            launchSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorService.unregisterListener(this);
        simulationWorker.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float measuredValue = extractRelevantValue(sensorEvent.values);
        updateScreenWithValue(measuredValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisionLevel) {
    }

    private float extractRelevantValue(float[] sensorValues) {
        if ("MAGNITUDE".equals(extractionMode)) {
            return (float) Math.sqrt(
                    sensorValues[0] * sensorValues[0]
                            + sensorValues[1] * sensorValues[1]
                            + sensorValues[2] * sensorValues[2]);
        }
        return sensorValues[0];
    }

    private void updateScreenWithValue(float freshValue) {
        currentValueDisplay.setText("Mesure courante : " + freshValue);
        measurementChart.pushMeasurement(freshValue);
    }

    private void launchSimulation() {
        simulationWorker.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulatedTime++;

                float simulatedData;

                if (targetSensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    simulatedData = 24f + (float) Math.sin(simulatedTime / 5f) * 3f;
                } else if (targetSensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                    simulatedData = 55f + (float) Math.sin(simulatedTime / 7f) * 15f;
                } else if (targetSensorType == Sensor.TYPE_PROXIMITY) {
                    simulatedData = simulatedTime % 6 < 3 ? 0f : 5f;
                } else if (targetSensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                    simulatedData = 45f + (float) Math.sin(simulatedTime / 4f) * 10f;
                } else {
                    simulatedData = (float) Math.sin(simulatedTime);
                }

                updateScreenWithValue(simulatedData);
                simulationWorker.postDelayed(this, 1000);
            }
        }, 1000);
    }
}
