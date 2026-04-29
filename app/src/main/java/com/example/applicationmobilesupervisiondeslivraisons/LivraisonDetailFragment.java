package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class LivraisonDetailFragment extends Fragment {

    private int noCde, livreurId;
    private DatabaseHelper dbHelper;

    // UI
    private TextView tvNoCde, tvClientName, tvTel, tvAdresse, tvVille, tvNbArticles,
            tvMontant, tvModePay, tvEtatActuel;
    private Spinner spinnerEtat, spinnerModePay;
    private TextInputEditText editRemarque;
    private Button btnSauvegarder, btnAppel, btnMaps, btnUrgence;
    private LinearLayout containerArticles;

    private String telClient, adresseClient, villeClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noCde     = getArguments().getInt("nocde", -1);
            livreurId = getArguments().getInt("livreur_id", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_livraison_detail_livreur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        tvNoCde        = view.findViewById(R.id.tv_detail_nocde);
        tvClientName   = view.findViewById(R.id.tv_detail_client);
        tvTel          = view.findViewById(R.id.tv_detail_tel);
        tvAdresse      = view.findViewById(R.id.tv_detail_adresse);
        tvVille        = view.findViewById(R.id.tv_detail_ville);
        tvNbArticles   = view.findViewById(R.id.tv_detail_nb_articles);
        tvMontant      = view.findViewById(R.id.tv_detail_montant);
        tvModePay      = view.findViewById(R.id.tv_detail_modepay);
        tvEtatActuel   = view.findViewById(R.id.tv_etat_actuel);
        spinnerEtat     = view.findViewById(R.id.spinner_etat);
        spinnerModePay  = view.findViewById(R.id.spinner_modepay);
        editRemarque   = view.findViewById(R.id.edit_remarque);
        btnSauvegarder = view.findViewById(R.id.btn_sauvegarder);
        btnAppel       = view.findViewById(R.id.btn_appel);
        btnMaps        = view.findViewById(R.id.btn_maps);
        btnUrgence     = view.findViewById(R.id.btn_urgence);
        containerArticles = view.findViewById(R.id.container_articles);

        // Spinner ÃƒÆ’Ã‚Â©tats
        ArrayAdapter<String> adapterEtat = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"en attente", "en cours", "livre", "annule", "probleme"});
        adapterEtat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEtat.setAdapter(adapterEtat);
        ArrayAdapter<String> adapterPay = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"especes", "carte", "cheque", "virement"});
        adapterPay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModePay.setAdapter(adapterPay);

        chargerDetail();

        btnSauvegarder.setOnClickListener(v -> sauvegarder());
        btnAppel.setOnClickListener(v -> appelerClient());
        btnMaps.setOnClickListener(v -> ouvrirMaps());
        btnUrgence.setOnClickListener(v -> envoyerUrgence());
    }

    private void chargerDetail() {
        Cursor cursor = dbHelper.getDeliveryDetail(noCde);
        if (cursor == null || !cursor.moveToFirst()) {
            Toast.makeText(requireContext(), "Livraison introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        String nomClt    = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_NOMCLT));
        String prenomClt = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_PRENOMCLT));
        telClient        = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_TELCLT));
        adresseClient    = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_ADRCLT));
        villeClient      = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_VILLECLT));
        String modePay   = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.LivraisonCom.COLUMN_MODEPAY));
        String etatLiv   = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.LivraisonCom.COLUMN_ETATLIV));
        String remarque  = cursor.getString(cursor.getColumnIndexOrThrow("remarque"));
        double montant   = cursor.getDouble(cursor.getColumnIndexOrThrow("montant"));
        int nbArticles   = cursor.getInt(cursor.getColumnIndexOrThrow("nb_articles"));
        cursor.close();

        tvNoCde.setText("Commande N " + noCde);
        tvClientName.setText(prenomClt + " " + nomClt);
        tvTel.setText(telClient);
        tvAdresse.setText(adresseClient);
        tvVille.setText(villeClient);
        tvNbArticles.setText(nbArticles + " article(s)");
        tvMontant.setText(String.format("%.2f DT", montant));
        tvModePay.setText(modePay != null ? modePay : "ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â");
        tvEtatActuel.setText(etatLiv);

        if (remarque != null) editRemarque.setText(remarque);

        // PrÃƒÆ’Ã‚Â©-sÃƒÆ’Ã‚Â©lectionner l'ÃƒÆ’Ã‚Â©tat dans le spinner
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerEtat.getAdapter();
        if (adapter != null && etatLiv != null) {
            int pos = adapter.getPosition(etatLiv);
            if (pos >= 0) spinnerEtat.setSelection(pos);
        if (spinnerModePay != null && modePay != null) {
            ArrayAdapter<String> adPay = (ArrayAdapter<String>) spinnerModePay.getAdapter();
            if (adPay != null) {
                int posPay = adPay.getPosition(modePay);
                if (posPay >= 0) spinnerModePay.setSelection(posPay);
            }
        }
        }

        chargerArticles();
    }

    private void chargerArticles() {
        containerArticles.removeAllViews();
        Cursor cursor = dbHelper.getOrderLines(noCde);
        if (cursor == null) return;
        while (cursor.moveToNext()) {
            String desig = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Articles.COLUMN_DESIGNATION));
            int    qty   = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.LigCdes.COLUMN_QTECDE));
            double prix  = cursor.getDouble(cursor.getColumnIndexOrThrow(DbContract.Articles.COLUMN_PRIXV));

            View ligne = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_order_line, containerArticles, false);
            // Utiliser les IDs existants dans le layout
            ((TextView) ligne.findViewById(R.id.tv_desig)).setText(desig);
            ((TextView) ligne.findViewById(R.id.tv_qte)).setText("x" + qty);
            ((TextView) ligne.findViewById(R.id.tv_total_line)).setText(String.format("%.2f DT", qty * prix));
            // Optionnel : afficher le prix unitaire
            ((TextView) ligne.findViewById(R.id.tv_prix)).setText(String.format("%.2f DT/u", prix));
            containerArticles.addView(ligne);
        }
        cursor.close();
    }

    private void sauvegarder() {
        String nouvelEtat = spinnerEtat.getSelectedItem().toString();
        String remarque   = editRemarque.getText() != null
                ? editRemarque.getText().toString().trim() : "";

        // Validation : si non livrÃƒÆ’Ã‚Â©, remarque obligatoire
        if (!nouvelEtat.equals("livre") && !nouvelEtat.equals("en attente")
                && !nouvelEtat.equals("en cours") && remarque.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Veuillez saisir une remarque", Toast.LENGTH_SHORT).show();
            return;
        }

        String nouveauPay = spinnerModePay.getSelectedItem().toString();
        boolean ok = dbHelper.updateLivraisonEtatEtRemarque(noCde, nouvelEtat, remarque);
        if (ok) dbHelper.updateLivraisonModepay(noCde, nouveauPay);
        if (ok) {
            Toast.makeText(requireContext(), "Livraison mise ÃƒÆ’Ã‚Â  jour ÃƒÂ¢Ã…â€œÃ¢â‚¬Å“", Toast.LENGTH_SHORT).show();
            tvEtatActuel.setText(nouvelEtat);
            envoyerRapportControleur(nouvelEtat, remarque);
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la mise ÃƒÆ’Ã‚Â  jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void appelerClient() {
        if (telClient == null || telClient.isEmpty()) {
            Toast.makeText(requireContext(), "Numero indisponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telClient));
        startActivity(intent);
    }

    private void ouvrirMaps() {
        String adresse = adresseClient + ", " + villeClient;
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(adresse));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback navigateur
            Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(adresse));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void envoyerRapportControleur(String etat, String remarque) {
        String nomLivreur = "Livreur";
        android.database.Cursor cLiv = dbHelper.getPersonnelById(livreurId);
        if (cLiv != null && cLiv.moveToFirst()) {
            String prenom = cLiv.getString(cLiv.getColumnIndex(DbContract.Personnel.COLUMN_PRENOMPERS));
            String nom    = cLiv.getString(cLiv.getColumnIndex(DbContract.Personnel.COLUMN_NOMPERS));
            nomLivreur = (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
            cLiv.close();
        }
        String rapport =
            "RAPPORT DE LIVRAISON\n" +
            "Livreur  : " + nomLivreur.trim() + "\n" +
            "Commande : #" + noCde + "\n" +
            "Client   : " + tvClientName.getText() + "\n" +
            "Etat     : " + etat.toUpperCase() +
            (remarque.isEmpty() ? "" : "\nRemarque : " + remarque);
        android.database.Cursor ctrl = dbHelper.getReadableDatabase().rawQuery(
            "SELECT idpers FROM Personnel WHERE codeposte = 2", null);
        if (ctrl != null) {
            while (ctrl.moveToNext()) {
                int controleurId = ctrl.getInt(0);
                dbHelper.insertMessageControleur(controleurId, livreurId, rapport);
            }
            ctrl.close();
        }
    }
    private void envoyerUrgence() {
        if (getActivity() instanceof DeliverymanDashboardActivity) {
            ((DeliverymanDashboardActivity) getActivity()).loadUrgence(noCde, telClient);
        }
    }
}