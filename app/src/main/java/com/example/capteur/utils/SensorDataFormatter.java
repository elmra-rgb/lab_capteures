package com.example.capteur.utils;

import android.hardware.Sensor;

public class SensorDataFormatter {

    public static String formatSensorDetails(Sensor sensorInstance) {
        return "Identification : " + sensorInstance.getId() + "\n"
                + "Nom capteur : " + sensorInstance.getName() + "\n"
                + "Fabricant : " + sensorInstance.getVendor() + "\n"
                + "Version logicielle : " + sensorInstance.getVersion() + "\n"
                + "Type texte : " + sensorInstance.getStringType() + "\n"
                + "Code type : " + sensorInstance.getType() + "\n"
                + "Précision : " + sensorInstance.getResolution() + "\n"
                + "Consommation : " + sensorInstance.getPower() + " mA\n"
                + "Plage max : " + sensorInstance.getMaximumRange() + "\n"
                + "Délai mini : " + sensorInstance.getMinDelay() + " µs\n";
    }
}
