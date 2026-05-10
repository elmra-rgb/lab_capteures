package com.example.capteur.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.capteur.utils.SensorDataFormatter;

import java.util.List;

public class SensorCatalogueFragment extends Fragment {

    private SensorManager sensorHandler;
    private LinearLayout sensorsContainer;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup parent,
            @Nullable Bundle savedInstanceState) {

        ScrollView scrollWrapper = new ScrollView(requireContext());

        sensorsContainer = new LinearLayout(requireContext());
        sensorsContainer.setOrientation(LinearLayout.VERTICAL);
        sensorsContainer.setPadding(24, 24, 24, 24);

        scrollWrapper.addView(sensorsContainer);

        sensorHandler = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        showAllSensors();

        return scrollWrapper;
    }

    private void showAllSensors() {
        List<Sensor> allSensors = sensorHandler.getSensorList(Sensor.TYPE_ALL);

        for (Sensor currentSensor : allSensors) {
            TextView sensorInfo = new TextView(requireContext());
            sensorInfo.setText(SensorDataFormatter.formatSensorDetails(currentSensor));
            sensorInfo.setTextSize(14);
            sensorInfo.setPadding(16, 16, 16, 16);

            sensorsContainer.addView(sensorInfo);

            View separatingLine = new View(requireContext());
            separatingLine.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            2));
            separatingLine.setBackgroundColor(0xFFE0E0E0);
            sensorsContainer.addView(separatingLine);
        }
    }
}
