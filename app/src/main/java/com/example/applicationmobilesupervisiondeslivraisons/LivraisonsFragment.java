package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.example.applicationmobilesupervisiondeslivraisons.api.ApiClient;
import com.example.applicationmobilesupervisiondeslivraisons.model.LivraisonCom;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivraisonsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout containerLivraisons;
    private TextView tvStartDate, tvEndDate;
    private String selectedStartDate = null, selectedEndDate = null;
    private String selectedEtat = null;
    private ChipGroup chipGroupEtat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livraisons, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        containerLivraisons = view.findViewById(R.id.container_livraisons);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        chipGroupEtat = view.findViewById(R.id.chip_group_etat);

        view.findViewById(R.id.btn_start_date).setOnClickListener(v -> showDatePicker(true));
        view.findViewById(R.id.btn_end_date).setOnClickListener(v -> showDatePicker(false));
        view.findViewById(R.id.btn_filter).setOnClickListener(v -> applyFilters());
        view.findViewById(R.id.btn_reset).setOnClickListener(v -> {
            selectedStartDate = null; selectedEndDate = null; selectedEtat = null;
            tvStartDate.setText("Date d\u00e9but"); tvEndDate.setText("Date fin");
            chipGroupEtat.clearCheck();
            loadFromApi();
        });

        chipGroupEtat.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) selectedEtat = null;
            else {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) selectedEtat = chip.getText().toString().toLowerCase();
            }
        });

        loadFromApi();
        return view;
    }

    private void loadFromApi() {
        ApiClient.getService().getLivraisonsToday().enqueue(new Callback<List<LivraisonCom>>() {
            @Override
            public void onResponse(Call<List<LivraisonCom>> call, Response<List<LivraisonCom>> response) {
                if (!isAdded() || getActivity() == null) return;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (LivraisonCom l : response.body()) {
                        dbHelper.insertOrUpdateLivraison(l.getNocde(), l.getDateliv(),
                            l.getLivreur(), l.getModepay(), l.getEtatliv(),
                            l.getRemarque() != null ? l.getRemarque() : "");
                    }
                }
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) displayDeliveries(dbHelper.getTodayDeliveries());
                });
            }

            @Override
            public void onFailure(Call<List<LivraisonCom>> call, Throwable t) {
                if (!isAdded() || getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) displayDeliveries(dbHelper.getTodayDeliveries());
                });
            }
        });
    }

    private void showDatePicker(boolean isStart) {
        if (!isAdded()) return;
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            String display = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            if (isStart) { selectedStartDate = date; tvStartDate.setText(display); }
            else { selectedEndDate = date; tvEndDate.setText(display); }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void applyFilters() {
        if (selectedStartDate != null && selectedEndDate != null)
            displayDeliveries(dbHelper.getDeliveriesByDateRange(selectedStartDate, selectedEndDate));
        else if (selectedEtat != null)
            displayDeliveries(dbHelper.getFilteredDeliveries(selectedEtat, null, null, null));
        else loadFromApi();
    }

    @SuppressLint("Range")
    private void displayDeliveries(Cursor cursor) {
        if (!isAdded() || getActivity() == null) return;
        containerLivraisons.removeAllViews();

        if (cursor == null || cursor.getCount() == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("Aucune livraison trouv\u00e9e");
            empty.setTextColor(0xFF6B5040);
            empty.setPadding(16, 32, 16, 8);
            empty.setGravity(android.view.Gravity.CENTER);
            containerLivraisons.addView(empty);
            if (cursor != null) cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            int nocde = cursor.getInt(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_NOCDE));
            String etat = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
            String dateliv = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_DATELIV));
            String modepay = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_MODEPAY));
            String nompers = cursor.getString(cursor.getColumnIndex("nompers"));
            String prenompers = cursor.getString(cursor.getColumnIndex("prenompers"));
            String nomclt = cursor.getString(cursor.getColumnIndex("nomclt"));
            String prenomclt = cursor.getString(cursor.getColumnIndex("prenomclt"));
            double montant = 0;
            try { montant = cursor.getDouble(cursor.getColumnIndex("montant")); } catch (Exception ignored) {}

            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_livraison_detail, containerLivraisons, false);

            ((TextView) card.findViewById(R.id.tv_cde_no)).setText("Commande #" + nocde);
            ((TextView) card.findViewById(R.id.tv_date)).setText("\uD83D\uDCC5 " + (dateliv != null ? dateliv : "\u2014"));
            String livreur = (prenompers != null ? prenompers : "") + " " + (nompers != null ? nompers : "");
            ((TextView) card.findViewById(R.id.tv_livreur)).setText("\uD83D\uDE9A " + livreur.trim());
            String client = (prenomclt != null ? prenomclt : "") + " " + (nomclt != null ? nomclt : "");
            ((TextView) card.findViewById(R.id.tv_client)).setText("\uD83D\uDC64 " + client.trim());
            ((TextView) card.findViewById(R.id.tv_montant)).setText(
                    String.format(Locale.getDefault(), "%.2f TND", montant));
            ((TextView) card.findViewById(R.id.tv_modepay)).setText(modepay != null ? modepay : "\u2014");

            TextView tvEtat = card.findViewById(R.id.tv_etat_badge);
            tvEtat.setText(etat != null ? capitalize(etat) : "\u2014");
            setEtatBackground(tvEtat, etat);

            final int finalNocde = nocde;
            card.setOnClickListener(v -> {
                if (!isAdded()) return;
                DeliveryDetailFragment detail = new DeliveryDetailFragment();
                Bundle args = new Bundle();
                args.putInt("nocde", finalNocde);
                detail.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detail)
                        .addToBackStack(null).commit();
            });
            containerLivraisons.addView(card);
        }
        cursor.close();
    }

    private void setEtatBackground(TextView tv, String etat) {
        if (etat == null) return;
        switch (etat) {
            case "livr\u00e9":
                tv.setBackgroundResource(R.drawable.badge_livre);
                tv.setTextColor(0xFF2E7D32); break;
            case "en cours":
                tv.setBackgroundResource(R.drawable.badge_en_cours);
                tv.setTextColor(0xFFE65100); break;
            case "annul\u00e9": case "probl\u00e8me":
                tv.setBackgroundResource(R.drawable.badge_probleme);
                tv.setTextColor(0xFFC62828); break;
            default:
                tv.setBackgroundResource(R.drawable.badge_attente);
                tv.setTextColor(0xFF5D4037); break;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}