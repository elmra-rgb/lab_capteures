package com.example.capteur.ecosystem;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.capteur.helpers.SensorSpecHelper;
import java.util.List;

public class DeviceCatalogue extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ScrollView scrollRoot = new ScrollView(requireContext());
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(28, 28, 28, 28);

        SensorManager manager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> allSensors = manager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s : allSensors) {
            TextView card = new TextView(requireContext());
            card.setText(SensorSpecHelper.extractDetails(s));
            card.setTextSize(13);
            card.setPadding(20, 20, 20, 20);
            card.setBackgroundColor(0xFFF5F5F5);
            card.setElevation(4);
            container.addView(card);

            View divider = new View(requireContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFDDDDDD);
            container.addView(divider);
        }

        scrollRoot.addView(container);
        return scrollRoot;
    }
}
