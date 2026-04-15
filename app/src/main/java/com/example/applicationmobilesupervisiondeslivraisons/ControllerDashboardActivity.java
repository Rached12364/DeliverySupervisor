package com.example.applicationmobilesupervisiondeslivraisons;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ControllerDashboardActivity extends AppCompatActivity {

    private String controllerName = "Contrôleur";
    private int controllerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_dashboard);

        // Récupérer les données du contrôleur
        if (getIntent() != null) {
            if (getIntent().hasExtra("controller_name")) {
                controllerName = getIntent().getStringExtra("controller_name");
            }
            if (getIntent().hasExtra("controller_id")) {
                controllerId = getIntent().getIntExtra("controller_id", -1);
            }
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Charger HomeFragment par défaut
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Navigation
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    loadFragment(new HomeFragment());
                    return true;
                } else if (itemId == R.id.nav_livraisons) {
                    loadFragment(new LivraisonsFragment());
                    return true;
                } else if (itemId == R.id.nav_dashboard) {
                    loadFragment(new DashboardFragment());
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    loadFragment(new MessagesFragment());
                    return true;
                } else if (itemId == R.id.nav_recherche) {
                    loadFragment(new RechercheFragment());
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("controller_name", controllerName);
        args.putInt("controller_id", controllerId);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}