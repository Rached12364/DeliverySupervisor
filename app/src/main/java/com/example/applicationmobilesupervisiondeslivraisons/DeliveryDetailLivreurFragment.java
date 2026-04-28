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
    private TextView tvTitle, tvEtat, tvDate, tvModepay, tvMontant;
    private TextView tvClient, tvAdresse, tvTelClient;
    private LinearLayout containerLignes;
    private Spinner spinnerEtat;
    private EditText editRemarque;
    private MaterialButton btnSauvegarder, btnUrgence, btnAppelClient, btnGoogleMaps;
    private String telClientCourant = "";
    private String adresseClientCourante = "";
    private String villeClientCourante = "";
    private static final String[] ETATS = {"en attente", "en cours", "livre", "annule", "probleme"};
    private static final String[] ETATS_AFFICHAGE = {"En attente", "En cours", "Livre", "Annule", "Probleme"};
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ETATS_AFFICHAGE);
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
            if (!telClientCourant.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telClientCourant)));
            } else {
                Toast.makeText(requireContext(), "Numero non disponible", Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=" + Uri.encode(adresse.trim()))));
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
            tvDate.setText(dateliv != null ? dateliv : "-");
            tvModepay.setText(modepay != null ? modepay : "-");
            tvMontant.setText(String.format(Locale.getDefault(), "%.2f TND", montant));
            String clientFull = (prenomclt != null ? prenomclt : "") + " " + (nomclt != null ? nomclt : "");
            tvClient.setText(clientFull.trim());
            tvAdresse.setText((adrclt != null ? adrclt : "") + (villeclt != null ? ", " + villeclt : ""));
            tvTelClient.setText(telclt != null ? telclt : "-");
            tvEtat.setText(etat != null ? etat : "-");
            setEtatBackground(tvEtat, etat);
            if (etat != null) {
                for (int i = 0; i < ETATS.length; i++) {
                    if (ETATS[i].equals(etat)) { spinnerEtat.setSelection(i); break; }
                }
            }
            if (remarque != null && !remarque.isEmpty()) editRemarque.setText(remarque);
            cursor.close();
        }
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
        boolean ok = dbHelper.updateLivraisonEtatEtRemarque(noCde, nouvelEtat, remarque);
        if (ok) {
            Toast.makeText(requireContext(), "Livraison mise a jour", Toast.LENGTH_SHORT).show();
            tvEtat.setText(nouvelEtat);
            setEtatBackground(tvEtat, nouvelEtat);
            envoyerRapportControleur(nouvelEtat, remarque);
        } else {
            Toast.makeText(requireContext(), "Erreur de mise a jour", Toast.LENGTH_SHORT).show();
        }
    }
    private void envoyerRapportControleur(String nouvelEtat, String remarque) {
        String nomLivreur = "Livreur";
        Cursor cLiv = dbHelper.getPersonnelById(livreurId);
        if (cLiv != null && cLiv.moveToFirst()) {
            String prenom = cLiv.getString(cLiv.getColumnIndex(DbContract.Personnel.COLUMN_PRENOMPERS));
            String nom    = cLiv.getString(cLiv.getColumnIndex(DbContract.Personnel.COLUMN_NOMPERS));
            nomLivreur = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
            cLiv.close();
        }
        String client  = tvClient.getText().toString();
        String date    = tvDate.getText().toString();
        String montant = tvMontant.getText().toString();
        String adresse = tvAdresse.getText().toString();
        String rapport =
            "RAPPORT DE LIVRAISON\n" +
            "--------------------\n" +
            "Livreur  : " + nomLivreur.trim() + "\n" +
            "Commande : #" + noCde + "\n" +
            "Client   : " + client + "\n" +
            "Adresse  : " + adresse + "\n" +
            "Date     : " + date + "\n" +
            "Montant  : " + montant + "\n" +
            "Etat     : " + nouvelEtat.toUpperCase() +
            (remarque.isEmpty() ? "" : "\nRemarque : " + remarque);
        Cursor ctrl = dbHelper.getReadableDatabase().rawQuery(
            "SELECT idpers FROM Personnel WHERE codeposte = 2", null);
        int nbEnvoyes = 0;
        if (ctrl != null) {
            while (ctrl.moveToNext()) {
                int controleurId = ctrl.getInt(0);
                dbHelper.insertMessageControleur(controleurId, livreurId, rapport);
                nbEnvoyes++;
            }
            ctrl.close();
        }
        if (nbEnvoyes > 0) {
            Toast.makeText(requireContext(),
                "Rapport envoye au controleur", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                "Aucun controleur trouve", Toast.LENGTH_SHORT).show();
        }
    }
    private void setEtatBackground(TextView tv, String etat) {
        if (etat == null) return;
        switch (etat) {
            case "livre":    tv.setBackgroundResource(R.drawable.badge_livre);    tv.setTextColor(0xFF2E7D32); break;
            case "en cours": tv.setBackgroundResource(R.drawable.badge_en_cours); tv.setTextColor(0xFFE65100); break;
            case "annule": case "probleme": tv.setBackgroundResource(R.drawable.badge_probleme); tv.setTextColor(0xFFC62828); break;
            default:         tv.setBackgroundResource(R.drawable.badge_attente);  tv.setTextColor(0xFF5D4037); break;
        }
    }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
