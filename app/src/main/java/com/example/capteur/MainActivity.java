package com.example.capteur;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.capteur.ecosystem.ActionClassifier;
import com.example.capteur.ecosystem.DeviceCatalogue;
import com.example.capteur.ecosystem.MeasureChart;
import com.example.capteur.ecosystem.MotionTracker;
import com.example.capteur.ecosystem.OrientationGuide;
import com.example.capteur.ecosystem.StepMeter;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mainDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainDrawer = findViewById(R.id.drawer_layout);
        Toolbar appToolbar = findViewById(R.id.toolbar);
        NavigationView sideMenu = findViewById(R.id.nav_view);

        setSupportActionBar(appToolbar);

        // Utilisation des chaînes de caractères pour l'accessibilité
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainDrawer, appToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mainDrawer.addDrawerListener(toggle);
        toggle.syncState();

        sideMenu.setNavigationItemSelectedListener(this);

        // Gestion moderne du bouton Retour (optimisé pour MacOS M2 / Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
                    mainDrawer.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        // Charger le fragment par défaut
        if (savedInstanceState == null) {
            openCleanFragment(new DeviceCatalogue());
            sideMenu.setCheckedItem(R.id.nav_catalogue);
            setTitle("📡 Capteurs disponibles");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_catalogue) {
            openCleanFragment(new DeviceCatalogue());
            setTitle("📋 Catalogue des capteurs");
        }
        else if (itemId == R.id.nav_temperature) {
            openCleanFragment(MeasureChart.newFactory(
                    android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE,
                    "🌡️ Température ambiante",
                    "SIMPLE"));
            setTitle("🌡️ Température");
        }
        else if (itemId == R.id.nav_humidity) {
            openCleanFragment(MeasureChart.newFactory(
                    android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY,
                    "💧 Humidité relative",
                    "SIMPLE"));
            setTitle("💧 Humidité");
        }
        else if (itemId == R.id.nav_proximity) {
            openCleanFragment(MeasureChart.newFactory(
                    android.hardware.Sensor.TYPE_PROXIMITY,
                    "📡 Capteur de proximité",
                    "SIMPLE"));
            setTitle("📡 Proximité");
        }
        else if (itemId == R.id.nav_magnetic) {
            openCleanFragment(MeasureChart.newFactory(
                    android.hardware.Sensor.TYPE_MAGNETIC_FIELD,
                    "🧲 Champ magnétique",
                    "NORM"));
            setTitle("🧲 Magnétomètre");
        }
        else if (itemId == R.id.nav_accelerometer) {
            openCleanFragment(MotionTracker.newFactory(
                    android.hardware.Sensor.TYPE_ACCELEROMETER,
                    "⚡ Accéléromètre (m/s²)"));
            setTitle("⚡ Accéléromètre");
        }
        else if (itemId == R.id.nav_gravity) {
            openCleanFragment(MotionTracker.newFactory(
                    android.hardware.Sensor.TYPE_GRAVITY,
                    "🌍 Gravité (m/s²)"));
            setTitle("🌍 Gravité");
        }
        else if (itemId == R.id.nav_gyroscope) {
            openCleanFragment(MotionTracker.newFactory(
                    android.hardware.Sensor.TYPE_GYROSCOPE,
                    "🔄 Gyroscope (rad/s)"));
            setTitle("🔄 Gyroscope");
        }
        else if (itemId == R.id.nav_stepcounter) {
            openCleanFragment(new StepMeter());
            setTitle("👣 Compteur de pas");
        }
        else if (itemId == R.id.nav_compass) {
            openCleanFragment(new OrientationGuide());
            setTitle("🧭 Boussole numérique");
        }
        else if (itemId == R.id.nav_activity) {
            openCleanFragment(new ActionClassifier());
            setTitle("🏃 Reconnaissance d'activité");
        }

        mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openCleanFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, targetFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }
}
