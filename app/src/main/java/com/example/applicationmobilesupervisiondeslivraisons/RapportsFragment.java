package com.example.applicationmobilesupervisiondeslivraisons;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
public class RapportsFragment extends Fragment {
    private LinearLayout containerRapports;
    private TextView tvBadge;
    private DatabaseHelper dbHelper;
    private int controleurId = -1;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            controleurId = getArguments().getInt("controller_id", -1);
        if (controleurId == -1)
            controleurId = requireContext()
                    .getSharedPreferences("UserSession", 0).getInt("userId", -1);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Layout construit en code (pas de XML supplémentaire)
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(0xFFF5F0E8);
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        // Titre
        TextView tvTitre = new TextView(requireContext());
        tvTitre.setText("📋 Rapports de livraison");
        tvTitre.setTextSize(20f);
        tvTitre.setTextColor(0xFF3E2723);
        tvTitre.setPadding(0, 0, 0, 16);
        root.addView(tvTitre);
        // Badge
        tvBadge = new TextView(requireContext());
        tvBadge.setTextSize(13f);
        tvBadge.setTextColor(0xFF757575);
        tvBadge.setPadding(0, 0, 0, 16);
        root.addView(tvBadge);
        // Container rapports
        containerRapports = new LinearLayout(requireContext());
        containerRapports.setOrientation(LinearLayout.VERTICAL);
        root.addView(containerRapports);
        scroll.addView(root);
        return scroll;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        chargerRapports();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (dbHelper != null) chargerRapports();
    }
    @SuppressLint("Range")
    private void chargerRapports() {
        containerRapports.removeAllViews();
        Cursor c = dbHelper.getMessagesControleur();
        if (c == null || c.getCount() == 0) {
            TextView tv = new TextView(requireContext());
            tv.setText("Aucun rapport reçu pour l'instant.");
            tv.setPadding(16, 32, 16, 32);
            tv.setTextColor(0xFF9E9E9E);
            containerRapports.addView(tv);
            tvBadge.setText("0 rapport");
            if (c != null) c.close();
            return;
        }
        int total = c.getCount();
        int nonLus = 0;
        while (c.moveToNext()) {
            int    msgId     = c.getInt(c.getColumnIndexOrThrow("_id"));
            String nomLiv    = c.getString(c.getColumnIndexOrThrow("nom_livreur"));
            String prenomLiv = c.getString(c.getColumnIndexOrThrow("prenom_livreur"));
            String message   = c.getString(c.getColumnIndexOrThrow("message"));
            String heure     = c.getString(c.getColumnIndexOrThrow("horodatage"));
            int    lu        = c.getInt(c.getColumnIndexOrThrow("lu"));
            if (lu == 0) nonLus++;
            View card = creerCarteRapport(nomLiv, prenomLiv, message, heure, lu, msgId);
            containerRapports.addView(card);
        }
        c.close();
        tvBadge.setText(total + " rapport(s) — " + nonLus + " non lu(s)");
    }
    private View creerCarteRapport(String nom, String prenom, String message,
                                    String heure, int lu, int msgId) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 20, 24, 20);
        card.setBackgroundColor(lu == 0 ? 0xFFFFF8E1 : 0xFFFFFFFF);
        // Bord coloré simulé avec margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        // En-tête
        TextView tvHeader = new TextView(requireContext());
        tvHeader.setText("🚚 " + prenom + " " + nom
                + (lu == 0 ? "  🔴 NON LU" : "  ✅ Lu"));
        tvHeader.setTextSize(14f);
        tvHeader.setTextColor(0xFF3E2723);
        tvHeader.setPadding(0, 0, 0, 8);
        card.addView(tvHeader);
        // Heure
        TextView tvHeure = new TextView(requireContext());
        tvHeure.setText("🕐 " + (heure != null && heure.length() >= 16
                ? heure.substring(0, 16) : heure));
        tvHeure.setTextSize(12f);
        tvHeure.setTextColor(0xFF9E9E9E);
        tvHeure.setPadding(0, 0, 0, 8);
        card.addView(tvHeure);
        // Message complet
        TextView tvMsg = new TextView(requireContext());
        tvMsg.setText(message);
        tvMsg.setTextSize(13f);
        tvMsg.setTextColor(0xFF4E342E);
        tvMsg.setBackgroundColor(0xFFF5F5F5);
        tvMsg.setPadding(16, 12, 16, 12);
        card.addView(tvMsg);
        // Marquer comme lu au clic
        card.setOnClickListener(v -> {
            dbHelper.marquerMessageControleurLu(msgId);
            chargerRapports();
        });
        return card;
    }
}
