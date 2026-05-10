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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.capteur.widgets.DynamicLineChart;

public class MovementSensorFragment extends Fragment implements SensorEventListener {

    private static final String PARAM_SENSOR_TYPE = "sensor_type_code";
    private static final String PARAM_TITLE_TEXT = "title_content";

    private SensorManager sensorService;
    private Sensor movementSensor;

    private TextView axisValueDisplay;
    private DynamicLineChart intensityChart;

    private int targetSensorCode;
    private String screenHeader;

    public static MovementSensorFragment createInstance(int sensorCode, String header) {
        MovementSensorFragment fragmentInstance = new MovementSensorFragment();

        Bundle argsBundle = new Bundle();
        argsBundle.putInt(PARAM_SENSOR_TYPE, sensorCode);
        argsBundle.putString(PARAM_TITLE_TEXT, header);

        fragmentInstance.setArguments(argsBundle);
        return fragmentInstance;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        targetSensorCode = requireArguments().getInt(PARAM_SENSOR_TYPE);
        screenHeader = requireArguments().getString(PARAM_TITLE_TEXT);

        sensorService = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        movementSensor = sensorService.getDefaultSensor(targetSensorCode);

        LinearLayout containerLayout = new LinearLayout(requireContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setPadding(24, 24, 24, 24);

        TextView titleWidget = new TextView(requireContext());
        titleWidget.setText(screenHeader);
        titleWidget.setTextSize(22);

        axisValueDisplay = new TextView(requireContext());
        axisValueDisplay.setTextSize(18);
        axisValueDisplay.setPadding(0, 24, 0, 24);

        intensityChart = new DynamicLineChart(requireContext());
        intensityChart.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        600));

        containerLayout.addView(titleWidget);
        containerLayout.addView(axisValueDisplay);
        containerLayout.addView(intensityChart);

        return containerLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (movementSensor != null) {
            sensorService.registerListener(
                    this,
                    movementSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            axisValueDisplay.setText("Capteur indisponible sur cet appareil.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorService.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float axisX = sensorEvent.values[0];
        float axisY = sensorEvent.values[1];
        float axisZ = sensorEvent.values[2];

        float vectorMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

        axisValueDisplay.setText(
                "Axe X : " + axisX + "\n"
                        + "Axe Y : " + axisY + "\n"
                        + "Axe Z : " + axisZ + "\n"
                        + "Intensité : " + vectorMagnitude);

        intensityChart.pushMeasurement(vectorMagnitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int precisionLevel) {
    }
}
