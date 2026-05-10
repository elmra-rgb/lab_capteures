package com.example.capteur.helpers;

import android.hardware.Sensor;

public class SensorSpecHelper {

    public static String extractDetails(Sensor device) {
        if (device == null) return "Capteur introuvable";

        return "🔧 ID : " + device.getId() + "\n" +
               "📛 Nom : " + device.getName() + "\n" +
               "🏭 Fabricant : " + device.getVendor() + "\n" +
               "📌 Version : " + device.getVersion() + "\n" +
               "🎛️ Type texte : " + device.getStringType() + "\n" +
               "🔢 Code type : " + device.getType() + "\n" +
               "📐 Résolution : " + device.getResolution() + "\n" +
               "⚡ Consommation : " + device.getPower() + " mA\n" +
               "📏 Plage max : " + device.getMaximumRange() + "\n" +
               "⏱️ Délai min : " + device.getMinDelay() + " µs\n";
    }
}
