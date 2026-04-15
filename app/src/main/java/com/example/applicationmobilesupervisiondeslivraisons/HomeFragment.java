package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String controllerName = "Contrôleur";
    private int controllerId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Récupérer les informations du contrôleur depuis les arguments
        if (getArguments() != null) {
            controllerName = getArguments().getString("controller_name", "Contrôleur");
            controllerId = getArguments().getInt("controller_id", -1);
        }

        // Greeting
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Bonjour, " + controllerName);
        }

        // CORRECTION : Ouvrir le fragment profil dans le même Dashboard
        MaterialCardView profileCircle = view.findViewById(R.id.profile_circle);
        if (profileCircle != null) {
            profileCircle.setOnClickListener(v -> {
                // Naviguer vers le fragment profil au lieu d'ouvrir une nouvelle activité
                if (getActivity() != null && getActivity() instanceof ControllerDashboardActivity) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putInt("controller_id", controllerId);
                    args.putString("controller_name", controllerName);
                    profileFragment.setArguments(args);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, profileFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        // Today stats
        loadTodayStats(view);

        // Recent deliveries
        loadRecentDeliveries(view);

        return view;
    }

    @SuppressLint("Range")
    private void loadTodayStats(View view) {
        TextView tvTotal = view.findViewById(R.id.tv_total_today);
        TextView tvLivrees = view.findViewById(R.id.tv_livrees);
        TextView tvEchecs = view.findViewById(R.id.tv_echecs);
        TextView tvEnCours = view.findViewById(R.id.tv_en_cours);
        TextView tvAnalyticsTotal = view.findViewById(R.id.tv_analytics_total);
        TextView tvSuccessRate = view.findViewById(R.id.tv_success_rate);
        TextView tvFailureRate = view.findViewById(R.id.tv_failure_rate);

        Cursor cursor = dbHelper.getTodayDeliveries();
        int total = 0, livrees = 0, echecs = 0, enCours = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                total++;
                String etat = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
                if (etat == null) continue;
                switch (etat) {
                    case "livré":
                        livrees++;
                        break;
                    case "annulé":
                    case "problème":
                        echecs++;
                        break;
                    case "en cours":
                        enCours++;
                        break;
                }
            }
            cursor.close();
        }

        if (tvTotal != null) tvTotal.setText(String.valueOf(total));
        if (tvLivrees != null) tvLivrees.setText(String.valueOf(livrees));
        if (tvEchecs != null) tvEchecs.setText(String.valueOf(echecs));
        if (tvEnCours != null) tvEnCours.setText(String.valueOf(enCours));
        if (tvAnalyticsTotal != null) tvAnalyticsTotal.setText(String.valueOf(total));

        if (tvSuccessRate != null) {
            if (total > 0) {
                int successRate = (livrees * 100) / total;
                tvSuccessRate.setText(successRate + "%");
                if (successRate >= 80) {
                    tvSuccessRate.setTextColor(0xFF4CAF50);
                } else if (successRate >= 50) {
                    tvSuccessRate.setTextColor(0xFFFF9800);
                } else {
                    tvSuccessRate.setTextColor(0xFFF44336);
                }
            } else {
                tvSuccessRate.setText("0%");
            }
        }

        if (tvFailureRate != null) {
            if (total > 0) {
                int failureRate = (echecs * 100) / total;
                tvFailureRate.setText(failureRate + "%");
                if (failureRate >= 50) {
                    tvFailureRate.setTextColor(0xFFF44336);
                } else if (failureRate >= 20) {
                    tvFailureRate.setTextColor(0xFFFF9800);
                } else {
                    tvFailureRate.setTextColor(0xFF4CAF50);
                }
            } else {
                tvFailureRate.setText("0%");
            }
        }
    }

    @SuppressLint("Range")
    private void loadRecentDeliveries(View view) {
        LinearLayout container = view.findViewById(R.id.container_recent);
        if (container == null) return;

        container.removeAllViews();

        Cursor cursor = dbHelper.getTodayDeliveries();
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext() && count < 5) {
                int nocde = cursor.getInt(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_NOCDE));
                String etat = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
                String nompers = cursor.getString(cursor.getColumnIndex("nompers"));
                String prenompers = cursor.getString(cursor.getColumnIndex("prenompers"));
                String ville = "";

                try {
                    ville = cursor.getString(cursor.getColumnIndex(DbContract.Clients.COLUMN_VILLECLT));
                } catch (Exception ignored) {}

                View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_delivery_card, container, false);

                TextView tvCde = card.findViewById(R.id.tv_cde_number);
                TextView tvLivreur = card.findViewById(R.id.tv_livreur_ville);
                TextView tvEtat = card.findViewById(R.id.tv_etat);
                TextView tvStatusIcon = card.findViewById(R.id.tv_status_icon);

                if (tvCde != null) tvCde.setText("Commande #" + nocde);

                if (tvLivreur != null) {
                    String livreurInfo = "";
                    if (prenompers != null) livreurInfo += prenompers;
                    if (nompers != null) livreurInfo += " " + nompers;
                    if (ville != null && !ville.isEmpty()) livreurInfo += " · " + ville;
                    tvLivreur.setText(livreurInfo.trim().isEmpty() ? "Livreur non assigné" : livreurInfo.trim());
                }

                if (tvEtat != null) {
                    String etatText = etat != null ? capitalize(etat) : "Inconnu";
                    tvEtat.setText(etatText);
                    setEtatBackground(tvEtat, etat);
                }

                if (tvStatusIcon != null) {
                    setStatusIcon(tvStatusIcon, etat);
                }

                container.addView(card);
                count++;
            }
            cursor.close();
        }

        if (count == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("Aucune livraison aujourd'hui");
            empty.setTextColor(0xFF9E8B7A);
            empty.setTextSize(14);
            empty.setPadding(16, 24, 16, 8);
            empty.setGravity(android.view.Gravity.CENTER);
            container.addView(empty);
        }
    }

    private void setEtatBackground(TextView tv, String etat) {
        if (etat == null) return;

        int paddingVertical = (int) (8 * getResources().getDisplayMetrics().density);
        int paddingHorizontal = (int) (12 * getResources().getDisplayMetrics().density);
        tv.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);

        switch (etat) {
            case "livré":
                tv.setBackgroundResource(R.drawable.badge_livre);
                tv.setTextColor(0xFF2E7D32);
                break;
            case "en cours":
                tv.setBackgroundResource(R.drawable.badge_en_cours);
                tv.setTextColor(0xFFE65100);
                break;
            case "annulé":
            case "problème":
                tv.setBackgroundResource(R.drawable.badge_probleme);
                tv.setTextColor(0xFFC62828);
                break;
            default:
                tv.setBackgroundResource(R.drawable.badge_attente);
                tv.setTextColor(0xFF5D4037);
                break;
        }
    }

    private void setStatusIcon(TextView tvIcon, String etat) {
        if (etat == null) {
            tvIcon.setText("📋");
            return;
        }

        switch (etat) {
            case "livré":
                tvIcon.setText("✓");
                tvIcon.setTextSize(24);
                tvIcon.setTextColor(0xFF4CAF50);
                break;
            case "en cours":
                tvIcon.setText("⟳");
                tvIcon.setTextSize(24);
                tvIcon.setTextColor(0xFFFF9800);
                break;
            case "annulé":
                tvIcon.setText("✗");
                tvIcon.setTextSize(24);
                tvIcon.setTextColor(0xFFF44336);
                break;
            case "problème":
                tvIcon.setText("⚠");
                tvIcon.setTextSize(24);
                tvIcon.setTextColor(0xFFF44336);
                break;
            default:
                tvIcon.setText("📋");
                tvIcon.setTextSize(24);
                tvIcon.setTextColor(0xFF9E8B7A);
                break;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}