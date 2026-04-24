package com.example.applicationmobilesupervisiondeslivraisons;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DeliverymanDashboardActivity extends AppCompatActivity {

    private int livreurId;
    private String livreurName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveryman_dashboard);

        livreurId   = getIntent().getIntExtra("livreur_id", -1);
        livreurName = getIntent().getStringExtra("livreur_name");

        // ✅ Synchroniser clients + commandes + livraisons au démarrage
        new SyncManager(this).syncAll(new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete() {
                runOnUiThread(() -> loadMesLivraisons());
            }
            @Override
            public void onSyncError(String error) {
                runOnUiThread(() ->
                    Toast.makeText(DeliverymanDashboardActivity.this,
                            "Mode hors ligne", Toast.LENGTH_SHORT).show());
            }
        });

        if (savedInstanceState == null) loadMesLivraisons();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_livreur);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_mes_livraisons) { loadMesLivraisons();     return true; }
            else if (id == R.id.nav_urgence)   { loadUrgence(0, null);    return true; }
            else if (id == R.id.nav_profile)   { loadProfile();           return true; }
            return false;
        });
    }

    public void loadMesLivraisons() {
        MesLivraisonsFragment fragment = new MesLivraisonsFragment();
        Bundle args = new Bundle();
        args.putInt("livreur_id", livreurId);
        args.putString("livreur_name", livreurName);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_livreur, fragment).commit();
    }

    public void loadUrgence(int noCdePreselect, String telClient) {
        UrgenceFragment fragment = new UrgenceFragment();
        Bundle args = new Bundle();
        args.putInt("livreur_id", livreurId);
        args.putInt("nocde_preselect", noCdePreselect);
        if (telClient != null) args.putString("tel_client", telClient);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_livreur, fragment)
                .addToBackStack(null).commit();
    }

    public void loadLivraisonDetail(int noCde) {
        DeliveryDetailLivreurFragment fragment = new DeliveryDetailLivreurFragment();
        Bundle args = new Bundle();
        args.putInt("nocde", noCde);
        args.putInt("livreur_id", livreurId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_livreur, fragment)
                .addToBackStack(null).commit();
    }

    public void loadProfile() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putInt("livreur_id", livreurId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_livreur, fragment)
                .addToBackStack(null).commit();
    }

    public void loadChangePassword() {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
        Bundle args = new Bundle();
        args.putInt("livreur_id", livreurId);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_livreur, fragment)
                .addToBackStack(null).commit();
    }

    public int getLivreurId()      { return livreurId; }
    public String getLivreurName() { return livreurName; }
}