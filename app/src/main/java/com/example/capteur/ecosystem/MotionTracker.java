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
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.capteur.widgets.DynamicPlotView;

public class MotionTracker extends Fragment implements SensorEventListener {
    private static final String ARG_SENSOR = "motion_sensor";
    private static final String ARG_CAPTION = "caption";
    private SensorManager manager;
    private Sensor selectedSensor;
    private TextView coordView;
    private DynamicPlotView intensityPlot;
    private int sensorKind;

    public static MotionTracker newFactory(int type, String label) {
        MotionTracker ft = new MotionTracker();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR, type);
        args.putString(ARG_CAPTION, label);
        ft.setArguments(args);
        return ft;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        if (getArguments() != null) {
            sensorKind = getArguments().getInt(ARG_SENSOR);
        }
        String caption = getArguments() != null ? getArguments().getString(ARG_CAPTION) : "Motion";

        manager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        selectedSensor = manager.getDefaultSensor(sensorKind);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 28, 28, 28);

        TextView titleView = new TextView(requireContext());
        titleView.setText(caption);
        titleView.setTextSize(24);
        coordView = new TextView(requireContext());
        coordView.setTextSize(18);
        coordView.setPadding(0, 24, 0, 24);
        intensityPlot = new DynamicPlotView(requireContext());
        intensityPlot.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 550));

        layout.addView(titleView);
        layout.addView(coordView);
        layout.addView(intensityPlot);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedSensor != null) manager.registerListener(this, selectedSensor, SensorManager.SENSOR_DELAY_NORMAL);
        else coordView.setText("⚠️ Capteur absent");
    }

    @Override
    public void onPause() { super.onPause(); manager.unregisterListener(this); }

    @Override
    public void onSensorChanged(SensorEvent e) {
        float x = e.values[0], y = e.values[1], z = e.values[2];
        float norm = (float) Math.sqrt(x*x + y*y + z*z);
        coordView.setText(String.format("X: %.2f\nY: %.2f\nZ: %.2f\n📈 Norme: %.2f", x, y, z, norm));
        intensityPlot.feedData(norm);
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}
}
