package com.example.applicationmobilesupervisiondeslivraisons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView kpiTotal, kpiLivre, kpiAttente;
    private TextView tvLivreurCount, tvClientCount;
    private LinearLayout containerLivreur, containerClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        // Initialisation des vues KPI
        kpiTotal = view.findViewById(R.id.kpi_total);
        kpiLivre = view.findViewById(R.id.kpi_livre);
        kpiAttente = view.findViewById(R.id.kpi_attente);
        tvLivreurCount = view.findViewById(R.id.tv_livreur_count);
        tvClientCount = view.findViewById(R.id.tv_client_count);
        containerLivreur = view.findViewById(R.id.container_livreur_stats);
        containerClient = view.findViewById(R.id.container_client_stats);

        // Charger les données avec animation
        loadStatistics();

        // Animation d'entrée pour les cartes KPI
        animateKPICards(view);

        return view;
    }

    private void animateKPICards(View view) {
        CardView kpiTotalCard = view.findViewById(R.id.kpi_total_card);
        CardView kpiLivreCard = view.findViewById(R.id.kpi_livre_card);
        CardView kpiAttenteCard = view.findViewById(R.id.kpi_attente_card);

        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left);
        slideUp.setDuration(500);

        kpiTotalCard.startAnimation(slideUp);
        kpiLivreCard.startAnimation(slideUp);
        kpiAttenteCard.startAnimation(slideUp);
    }

    private void animateNumber(final TextView textView, int start, int end, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> textView.setText(String.valueOf(animation.getAnimatedValue())));
        animator.start();
    }

    @SuppressLint("Range")
    private void loadStatistics() {
        Map<String, Map<String, Integer>> livreurData = new HashMap<>();
        Map<String, Integer> livreurTotals = new HashMap<>();
        Map<String, Map<String, Integer>> clientData = new HashMap<>();
        Map<String, Integer> clientTotals = new HashMap<>();

        int totalGlobal = 0;
        int totalLivree = 0;
        int totalAttente = 0;

        // Charger données livreurs
        Cursor cursorLivreur = dbHelper.getCountByLivreurAndEtat();
        if (cursorLivreur != null && cursorLivreur.moveToFirst()) {
            do {
                String nom = cursorLivreur.getString(cursorLivreur.getColumnIndex("prenompers")) + " " +
                        cursorLivreur.getString(cursorLivreur.getColumnIndex("nompers"));
                String etat = cursorLivreur.getString(cursorLivreur.getColumnIndex("etatliv"));
                int count = cursorLivreur.getInt(cursorLivreur.getColumnIndex("count"));

                if (!livreurData.containsKey(nom)) {
                    livreurData.put(nom, new HashMap<>());
                }
                livreurData.get(nom).put(etat, count);
                livreurTotals.put(nom, livreurTotals.getOrDefault(nom, 0) + count);

                totalGlobal += count;
                if ("livré".equalsIgnoreCase(etat)) totalLivree += count;
                if ("en attente".equalsIgnoreCase(etat)) totalAttente += count;
            } while (cursorLivreur.moveToNext());
            cursorLivreur.close();
        }

        // Charger données clients
        Cursor cursorClient = dbHelper.getCountByClientAndEtat();
        if (cursorClient != null && cursorClient.moveToFirst()) {
            do {
                String nom = cursorClient.getString(cursorClient.getColumnIndex("prenomclt")) + " " +
                        cursorClient.getString(cursorClient.getColumnIndex("nomclt"));
                String etat = cursorClient.getString(cursorClient.getColumnIndex("etatliv"));
                int count = cursorClient.getInt(cursorClient.getColumnIndex("count"));

                if (!clientData.containsKey(nom)) {
                    clientData.put(nom, new HashMap<>());
                }
                clientData.get(nom).put(etat, count);
                clientTotals.put(nom, clientTotals.getOrDefault(nom, 0) + count);
            } while (cursorClient.moveToNext());
            cursorClient.close();
        }

        // Animer les KPI
        animateNumber(kpiTotal, 0, totalGlobal, 1000);
        animateNumber(kpiLivre, 0, totalLivree, 800);
        animateNumber(kpiAttente, 0, totalAttente, 800);

        // Afficher les compteurs
        tvLivreurCount.setText(livreurData.size() + " livreur" + (livreurData.size() > 1 ? "s" : ""));
        tvClientCount.setText(clientData.size() + " client" + (clientData.size() > 1 ? "s" : ""));

        // Afficher les cartes livreurs
        displayStatCards(containerLivreur, livreurData, livreurTotals, totalGlobal);
        displayStatCards(containerClient, clientData, clientTotals, totalGlobal);
    }

    private void displayStatCards(LinearLayout container, Map<String, Map<String, Integer>> data,
                                  Map<String, Integer> totals, int totalGlobal) {
        container.removeAllViews();

        int index = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {
            View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_stat_card, container, false);

            TextView tvTitle = card.findViewById(R.id.tv_stat_title);
            TextView tvTotal = card.findViewById(R.id.tv_stat_total);
            TextView tvPercent = card.findViewById(R.id.tv_stat_percent);
            LinearLayout details = card.findViewById(R.id.container_stat_details);
            LinearLayout progressContainer = card.findViewById(R.id.container_progress);
            ProgressBar progressBar = card.findViewById(R.id.progress_bar);
            TextView tvProgressLabel = card.findViewById(R.id.tv_progress_label);

            int total = totals.get(entry.getKey());
            int pourcentage = totalGlobal > 0 ? (total * 100 / totalGlobal) : 0;

            // Icone dynamique basée sur l'index
            TextView tvIcon = card.findViewById(R.id.tv_stat_icon);
            String[] icons = {"👤", "🚚", "📦", "⭐", "🎯", "✅"};
            tvIcon.setText(icons[index % icons.length]);

            tvTitle.setText(entry.getKey());
            tvTotal.setText("Total: " + total + " livraison" + (total > 1 ? "s" : ""));
            tvPercent.setText(pourcentage + "%");

            // Afficher la barre de progression si le pourcentage est > 0
            if (totalGlobal > 0) {
                progressContainer.setVisibility(View.VISIBLE);
                progressBar.setMax(100);
                progressBar.setProgress(pourcentage);
                tvProgressLabel.setText("Part du total: " + pourcentage + "%");
            }

            // Afficher les détails par état
            Map<String, Integer> etats = entry.getValue();
            String[] etatOrder = {"livré", "en cours", "en attente", "annulé", "problème"};

            for (String etat : etatOrder) {
                if (etats.containsKey(etat)) {
                    TextView tv = new TextView(requireContext());
                    int count = etats.get(etat);
                    tv.setText("• " + capitalize(etat) + ": " + count);
                    tv.setTextColor(getEtatColor(etat));
                    tv.setTextSize(13);
                    tv.setPadding(0, 6, 0, 0);
                    details.addView(tv);
                }
            }

            // Animation d'entrée
            card.setAlpha(0f);
            card.setTranslationY(50f);
            container.addView(card);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(index * 100)
                    .start();

            index++;
        }

        // Message si aucune donnée
        if (data.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Aucune donnée disponible");
            emptyView.setTextColor(0xFF9E8B7A);
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            container.addView(emptyView);
        }
    }

    private int getEtatColor(String etat) {
        if (etat == null) return 0xFF5D4037;
        switch (etat.toLowerCase()) {
            case "livré": return 0xFF2E7D32;
            case "en cours": return 0xFFE65100;
            case "en attente": return 0xFFFF9800;
            case "annulé":
            case "problème": return 0xFFC62828;
            default: return 0xFF5D4037;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics(); // Rafraîchir les données quand le fragment devient visible
    }
}