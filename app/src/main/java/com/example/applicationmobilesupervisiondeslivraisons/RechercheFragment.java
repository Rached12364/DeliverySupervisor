package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class RechercheFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout containerResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recherche, container, false);
        dbHelper = new DatabaseHelper(requireContext());
        containerResults = view.findViewById(R.id.container_results);

        TextInputEditText editSearch = view.findViewById(R.id.edit_search);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    doSearch(query);
                } else {
                    containerResults.removeAllViews();
                }
            }
        });

        view.findViewById(R.id.btn_search).setOnClickListener(v -> {
            String q = editSearch.getText() != null ? editSearch.getText().toString().trim() : "";
            if (!q.isEmpty()) doSearch(q);
        });

        return view;
    }

    @SuppressLint("Range")
    private void doSearch(String query) {
        containerResults.removeAllViews();
        Cursor cursor = dbHelper.searchDeliveries(query);

        if (cursor == null || cursor.getCount() == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("Aucun résultat pour \"" + query + "\"");
            empty.setTextColor(0xFF9E8B7A);
            empty.setPadding(16, 32, 16, 8);
            empty.setGravity(android.view.Gravity.CENTER);
            containerResults.addView(empty);
            if (cursor != null) cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            int nocde = cursor.getInt(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_NOCDE));
            String etat = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
            String dateliv = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_DATELIV));
            String nompers = cursor.getString(cursor.getColumnIndex("nompers"));
            String prenompers = cursor.getString(cursor.getColumnIndex("prenompers"));
            String nomclt = cursor.getString(cursor.getColumnIndex("nomclt"));
            double montant = 0;
            try { montant = cursor.getDouble(cursor.getColumnIndex("montant")); } catch (Exception ignored) {}

            View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_livraison_detail, containerResults, false);
            ((TextView) card.findViewById(R.id.tv_cde_no)).setText("Commande #" + nocde);
            ((TextView) card.findViewById(R.id.tv_date)).setText("📅 " + (dateliv != null ? dateliv : "—"));
            String livreur = (prenompers != null ? prenompers : "") + " " + (nompers != null ? nompers : "");
            ((TextView) card.findViewById(R.id.tv_livreur)).setText("🚚 " + livreur.trim());
            String client = (nomclt != null ? nomclt : "");
            ((TextView) card.findViewById(R.id.tv_client)).setText("👤 " + client.trim());
            ((TextView) card.findViewById(R.id.tv_montant)).setText(String.format(Locale.getDefault(), "%.2f TND", montant));
            ((TextView) card.findViewById(R.id.tv_modepay)).setText("—");

            TextView tvEtat = card.findViewById(R.id.tv_etat_badge);
            tvEtat.setText(etat != null ? capitalize(etat) : "—");
            setEtatBackground(tvEtat, etat);

            final int finalNocde = nocde;
            card.setOnClickListener(v -> {
                DeliveryDetailFragment detail = new DeliveryDetailFragment();
                Bundle args = new Bundle();
                args.putInt("nocde", finalNocde);
                detail.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detail)
                        .addToBackStack(null)
                        .commit();
            });

            containerResults.addView(card);
        }
        cursor.close();
    }

    private void setEtatBackground(TextView tv, String etat) {
        if (etat == null) return;
        switch (etat) {
            case "livré": tv.setBackgroundResource(R.drawable.badge_livre); tv.setTextColor(0xFF2E7D32); break;
            case "en cours": tv.setBackgroundResource(R.drawable.badge_en_cours); tv.setTextColor(0xFFE65100); break;
            case "annulé": case "problème": tv.setBackgroundResource(R.drawable.badge_probleme); tv.setTextColor(0xFFC62828); break;
            default: tv.setBackgroundResource(R.drawable.badge_attente); tv.setTextColor(0xFF5D4037); break;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}