package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class DeliveryDetailLivreurFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private int noCde, livreurId;

    // Vues
    private TextView tvTitle, tvEtat, tvDate, tvModepay, tvMontant;
    private TextView tvClient, tvAdresse, tvTelClient;
    private LinearLayout containerLignes;
    private Spinner spinnerEtat;
    private EditText editRemarque;
    private MaterialButton btnSauvegarder, btnUrgence, btnAppelClient, btnGoogleMaps;

    private String telClientCourant = "";
    private String adresseClientCourante = "";
    private String villeClientCourante = "";

    private static final String[] ETATS = {"en attente", "en cours", "livré", "annulé", "problème"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_delivery_detail_livreur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        if (getArguments() != null) {
            noCde     = getArguments().getInt("nocde", -1);
            livreurId = getArguments().getInt("livreur_id", -1);
        }

        // Lier les vues
        tvTitle        = view.findViewById(R.id.tv_detail_title);
        tvEtat         = view.findViewById(R.id.tv_detail_etat);
        tvDate         = view.findViewById(R.id.tv_detail_date);
        tvModepay      = view.findViewById(R.id.tv_detail_modepay);
        tvMontant      = view.findViewById(R.id.tv_detail_montant);
        tvClient       = view.findViewById(R.id.tv_detail_client);
        tvAdresse      = view.findViewById(R.id.tv_detail_adresse);
        tvTelClient    = view.findViewById(R.id.tv_detail_tel_client);
        containerLignes= view.findViewById(R.id.container_lignes);
        spinnerEtat    = view.findViewById(R.id.spinner_etat_livraison);
        editRemarque   = view.findViewById(R.id.edit_remarque);
        btnSauvegarder = view.findViewById(R.id.btn_sauvegarder);
        btnUrgence     = view.findViewById(R.id.btn_urgence);
        btnAppelClient = view.findViewById(R.id.btn_appel_client);
        btnGoogleMaps  = view.findViewById(R.id.btn_google_maps);

        // Spinner états
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ETATS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEtat.setAdapter(adapter);

        view.findViewById(R.id.btn_back_detail).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        if (noCde != -1) chargerDetail();

        btnSauvegarder.setOnClickListener(v -> sauvegarder());

        btnUrgence.setOnClickListener(v -> {
            if (getActivity() instanceof DeliverymanDashboardActivity) {
                ((DeliverymanDashboardActivity) getActivity()).loadUrgence(noCde, telClientCourant);
            }
        });

        btnAppelClient.setOnClickListener(v -> {
            if (!telClientCourant.isEmpty() && !telClientCourant.equals("—")) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + telClientCourant));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Numéro non disponible", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogleMaps.setOnClickListener(v -> {
            String adresse = adresseClientCourante + " " + villeClientCourante;
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(adresse.trim()));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback navigateur
                Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(adresse.trim()));
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });
    }

    @SuppressLint("Range")
    private void chargerDetail() {
        Cursor cursor = dbHelper.getDeliveryDetail(noCde);
        if (cursor != null && cursor.moveToFirst()) {
            String etat     = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
            String dateliv  = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_DATELIV));
            String modepay  = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_MODEPAY));
            String remarque = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_REMARQUE));
            String nomclt   = cursor.getString(cursor.getColumnIndex("nomclt"));
            String prenomclt= cursor.getString(cursor.getColumnIndex("prenomclt"));
            String adrclt   = cursor.getString(cursor.getColumnIndex("adrclt"));
            String villeclt = cursor.getString(cursor.getColumnIndex("villeclt"));
            String telclt   = cursor.getString(cursor.getColumnIndex("telclt"));
            double montant  = cursor.getDouble(cursor.getColumnIndex("montant"));

            telClientCourant      = telclt != null ? telclt : "";
            adresseClientCourante = adrclt != null ? adrclt : "";
            villeClientCourante   = villeclt != null ? villeclt : "";

            tvTitle.setText("Commande #" + noCde);
            tvDate.setText(dateliv != null ? dateliv : "—");
            tvModepay.setText(modepay != null ? modepay : "—");
            tvMontant.setText(String.format(Locale.getDefault(), "%.2f TND", montant));

            String clientFull = (prenomclt != null ? prenomclt : "") + " " + (nomclt != null ? nomclt : "");
            tvClient.setText(clientFull.trim());
            tvAdresse.setText((adrclt != null ? adrclt : "") + (villeclt != null ? ", " + villeclt : ""));
            tvTelClient.setText(telclt != null ? "📞 " + telclt : "—");

            // Badge état actuel
            tvEtat.setText(etat != null ? capitalize(etat) : "—");
            setEtatBackground(tvEtat, etat);

            // Pré-sélectionner l'état dans le spinner
            if (etat != null) {
                for (int i = 0; i < ETATS.length; i++) {
                    if (ETATS[i].equals(etat)) {
                        spinnerEtat.setSelection(i);
                        break;
                    }
                }
            }

            // Pré-remplir remarque
            if (remarque != null && !remarque.isEmpty()) {
                editRemarque.setText(remarque);
            }

            cursor.close();
        }

        // Charger les lignes articles
        Cursor lignes = dbHelper.getOrderLines(noCde);
        if (lignes != null) {
            while (lignes.moveToNext()) {
                String ref   = lignes.getString(lignes.getColumnIndex("refart"));
                String desig = lignes.getString(lignes.getColumnIndex("designation"));
                int    qte   = lignes.getInt(lignes.getColumnIndex("qtecde"));
                double prix  = lignes.getDouble(lignes.getColumnIndex("prixV"));
                double total = qte * prix;

                View row = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_order_line, containerLignes, false);
                ((TextView) row.findViewById(R.id.tv_ref)).setText(ref);
                ((TextView) row.findViewById(R.id.tv_desig)).setText(desig);
                ((TextView) row.findViewById(R.id.tv_qte)).setText("x" + qte);
                ((TextView) row.findViewById(R.id.tv_prix)).setText(
                        String.format(Locale.getDefault(), "%.2f TND/u", prix));
                ((TextView) row.findViewById(R.id.tv_total_line)).setText(
                        String.format(Locale.getDefault(), "%.2f TND", total));
                containerLignes.addView(row);
            }
            lignes.close();
        }
    }

    private void sauvegarder() {
        String nouvelEtat = ETATS[spinnerEtat.getSelectedItemPosition()];
        String remarque   = editRemarque.getText() != null
                ? editRemarque.getText().toString().trim() : "";

        // Si non livré → remarque obligatoire
        if (!nouvelEtat.equals("livré") && !nouvelEtat.equals("en attente")
                && !nouvelEtat.equals("en cours") && remarque.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Veuillez ajouter une remarque pour cet état", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbHelper.updateLivraisonEtatEtRemarque(noCde, nouvelEtat, remarque);
        if (ok) {
            Toast.makeText(requireContext(), "✓ Livraison mise à jour", Toast.LENGTH_SHORT).show();
            // Mettre à jour le badge affiché
            tvEtat.setText(capitalize(nouvelEtat));
            setEtatBackground(tvEtat, nouvelEtat);
        } else {
            Toast.makeText(requireContext(), "Erreur de mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void setEtatBackground(TextView tv, String etat) {
        if (etat == null) return;
        switch (etat) {
            case "livré":    tv.setBackgroundResource(R.drawable.badge_livre);    tv.setTextColor(0xFF2E7D32); break;
            case "en cours": tv.setBackgroundResource(R.drawable.badge_en_cours); tv.setTextColor(0xFFE65100); break;
            case "annulé": case "problème": tv.setBackgroundResource(R.drawable.badge_probleme); tv.setTextColor(0xFFC62828); break;
            default:         tv.setBackgroundResource(R.drawable.badge_attente);  tv.setTextColor(0xFF5D4037); break;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}