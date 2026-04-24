package com.example.applicationmobilesupervisiondeslivraisons;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
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

        if (getIntent() != null) {
            if (getIntent().hasExtra("controller_name"))
                controllerName = getIntent().getStringExtra("controller_name");
            if (getIntent().hasExtra("controller_id"))
                controllerId = getIntent().getIntExtra("controller_id", -1);
            if (getIntent().hasExtra("controleur_name"))
                controllerName = getIntent().getStringExtra("controleur_name");
            if (getIntent().hasExtra("controleur_id"))
                controllerId = getIntent().getIntExtra("controleur_id", -1);
        }

        // ✅ Synchroniser clients + commandes + livraisons au démarrage
        new SyncManager(this).syncAll(new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete() {
                runOnUiThread(() -> {
                    Fragment current = getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_container);
                    if (current != null) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .detach(current).attach(current).commit();
                    }
                });
            }
            @Override
            public void onSyncError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(ControllerDashboardActivity.this,
                            "Mode hors ligne", Toast.LENGTH_SHORT).show());
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home)         { loadFragment(new HomeFragment());      return true; }
                else if (itemId == R.id.nav_livraisons) { loadFragment(new LivraisonsFragment()); return true; }
                else if (itemId == R.id.nav_dashboard)  { loadFragment(new DashboardFragment());  return true; }
                else if (itemId == R.id.nav_messages)   { loadFragment(new MessagesFragment());   return true; }
                else if (itemId == R.id.nav_recherche)  { loadFragment(new RechercheFragment());  return true; }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        Bundle args = new Bundle();
        args.putString("controller_name", controllerName);
        args.putInt("controller_id", controllerId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }
}