package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UrgenceFragment extends Fragment {

    private int livreurId      = -1;
    private int noCdePreselect = 0;

    private DatabaseHelper dbHelper;

    // Section 1 - Envoyer urgence
    private Spinner      spinnerCommande;
    private ChipGroup    chipGroupRaison;
    private EditText     editMessageLibre;
    private TextView     tvTelClient, tvNoCdeInfo;
    private MaterialButton btnEnvoyer;

    // Section 2 - Messages recus du controleur
    private LinearLayout containerMessagesRecus;

    // Section 3 - Historique urgences envoyees
    private LinearLayout containerHistorique;

    private List<Integer> listeNoCdes     = new ArrayList<>();
    private List<String>  listeTelClients = new ArrayList<>();

    private static final String[] RAISONS = {
            "Client ne r\u00e9pond pas au t\u00e9l\u00e9phone",
            "Client n'accepte pas la commande",
            "Adresse introuvable",
            "Client absent",
            "Autre"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            livreurId      = getArguments().getInt("livreur_id",      -1);
            noCdePreselect = getArguments().getInt("nocde_preselect",  0);
        }
        if (livreurId == -1)
            livreurId = requireContext()
                    .getSharedPreferences("UserSession", 0).getInt("userId", -1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_urgence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        spinnerCommande        = view.findViewById(R.id.spinner_commande_urgence);
        chipGroupRaison        = view.findViewById(R.id.chip_group_raison);
        editMessageLibre       = view.findViewById(R.id.edit_message_libre);
        tvTelClient            = view.findViewById(R.id.tv_tel_client_urgence);
        tvNoCdeInfo            = view.findViewById(R.id.tv_nocde_info);
        btnEnvoyer             = view.findViewById(R.id.btn_envoyer_urgence);
        containerMessagesRecus = view.findViewById(R.id.container_messages_recus_livreur);
        containerHistorique    = view.findViewById(R.id.container_historique_urgence);

        chargerCommandes();
        creerChips();
        chargerMessagesRecus();
        chargerHistorique();

        if (noCdePreselect > 0) {
            int pos = listeNoCdes.indexOf(noCdePreselect);
            if (pos >= 0) spinnerCommande.setSelection(pos);
        }

        spinnerCommande.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       View v, int position, long id) {
                if (position < listeTelClients.size()) {
                    tvTelClient.setText(listeTelClients.get(position));
                    tvNoCdeInfo.setText("Commande N\u00b0 " + listeNoCdes.get(position));
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnEnvoyer.setOnClickListener(v -> envoyerUrgence());
    }

    @Override
    public void onResume() {
        super.onResume();
        chargerMessagesRecus();
        chargerHistorique();
    }

    private void chargerCommandes() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor c = dbHelper.getTodayDeliveriesForLivreur(livreurId, today);

        List<String> labels = new ArrayList<>();
        listeNoCdes.clear();
        listeTelClients.clear();

        if (c != null) {
            while (c.moveToNext()) {
                int    nocde  = c.getInt   (c.getColumnIndexOrThrow(DbContract.LivraisonCom.COLUMN_NOCDE));
                String nom    = c.getString(c.getColumnIndexOrThrow(DbContract.Clients.COLUMN_NOMCLT));
                String prenom = c.getString(c.getColumnIndexOrThrow(DbContract.Clients.COLUMN_PRENOMCLT));
                String tel    = c.getString(c.getColumnIndexOrThrow(DbContract.Clients.COLUMN_TELCLT));
                listeNoCdes.add(nocde);
                listeTelClients.add(tel != null ? tel : "--");
                labels.add("N\u00b0" + nocde + " \u2014 " + prenom + " " + nom);
            }
            c.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommande.setAdapter(adapter);

        if (!listeNoCdes.isEmpty()) {
            tvTelClient.setText(listeTelClients.get(0));
            tvNoCdeInfo.setText("Commande N\u00b0 " + listeNoCdes.get(0));
        }
    }

    private void creerChips() {
        chipGroupRaison.removeAllViews();
        // Couleurs : non cochÃ© = beige foncÃ©, cochÃ© = marron bouton
        ColorStateList bgColors = new ColorStateList(
            new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
            },
            new int[]{
                0xFFB8935A,  // cochÃ© -> couleur bouton beige foncÃ©
                0xFFF0E6D8   // non cochÃ© -> beige clair visible
            }
        );
        ColorStateList textColors = new ColorStateList(
            new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
            },
            new int[]{
                0xFFFFFFFF,  // cochÃ© -> texte blanc
                0xFF1C1008   // non cochÃ© -> texte trÃ¨s foncÃ©
            }
        );

        for (String raison : RAISONS) {
            Chip chip = new Chip(requireContext());
            chip.setText(raison);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(bgColors);
            chip.setTextColor(textColors);
            chip.setCheckedIconVisible(false);
            chip.setChipStrokeWidth(1.5f);
            chip.setChipStrokeColor(ColorStateList.valueOf(0xFFB8935A));
            chipGroupRaison.addView(chip);
        }
    }

    private void envoyerUrgence() {
        int pos = spinnerCommande.getSelectedItemPosition();
        if (pos < 0 || listeNoCdes.isEmpty()) {
            Toast.makeText(requireContext(),
                    "S\u00e9lectionnez une commande", Toast.LENGTH_SHORT).show();
            return;
        }

        int    noCde   = listeNoCdes.get(pos);
        String tel     = listeTelClients.get(pos);
        String message = construireMessage(noCde, tel);

        if (message == null) {
            Toast.makeText(requireContext(),
                    "S\u00e9lectionnez une raison ou saisissez un message", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = dbHelper.insertMessageUrgence(livreurId, noCde, tel, message);
        if (ok) {
            Toast.makeText(requireContext(),
                    "Message envoy\u00e9 au contr\u00f4leur", Toast.LENGTH_LONG).show();
            editMessageLibre.setText("");
            for (int i = 0; i < chipGroupRaison.getChildCount(); i++)
                ((Chip) chipGroupRaison.getChildAt(i)).setChecked(false);
            chargerHistorique();
        } else {
            Toast.makeText(requireContext(), "Erreur d'envoi", Toast.LENGTH_SHORT).show();
        }
    }

    private String construireMessage(int noCde, String tel) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chipGroupRaison.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRaison.getChildAt(i);
            if (chip.isChecked()) sb.append("\u2022 ").append(chip.getText()).append("\n");
        }

        String libre = editMessageLibre.getText() != null
                ? editMessageLibre.getText().toString().trim() : "";
        if (!libre.isEmpty()) sb.append(libre).append("\n");

        if (sb.length() == 0) return null;

        sb.append("\n---------------------\n");
        sb.append("Commande N\u00b0 ").append(noCde).append("\n");
        sb.append("Contact client : ").append(tel);

        return sb.toString();
    }

    private void chargerMessagesRecus() {
        containerMessagesRecus.removeAllViews();
        Cursor c = dbHelper.getMessagesForLivreur(livreurId);

        if (c == null || c.getCount() == 0) {
            TextView tv = new TextView(requireContext());
            tv.setText("Aucun message du contr\u00f4leur.");
            tv.setTextColor(0xFF1B5E20);
            tv.setPadding(16, 12, 16, 12);
            containerMessagesRecus.addView(tv);
            if (c != null) c.close();
            return;
        }

        while (c.moveToNext()) {
            int    msgId      = c.getInt   (c.getColumnIndexOrThrow("_id"));
            String nomCtrl    = c.getString(c.getColumnIndexOrThrow("nom_controleur"));
            String prenomCtrl = c.getString(c.getColumnIndexOrThrow("prenom_controleur"));
            String message    = c.getString(c.getColumnIndexOrThrow("message"));
            String horodatage = c.getString(c.getColumnIndexOrThrow("horodatage"));
            int    lu         = c.getInt   (c.getColumnIndexOrThrow("lu"));

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_message_recu_livreur, containerMessagesRecus, false);

            ((TextView) item.findViewById(R.id.tv_expediteur_ctrl))
                    .setText(prenomCtrl + " " + nomCtrl + (lu == 0 ? "  \u25cf" : ""));
            ((TextView) item.findViewById(R.id.tv_message_ctrl))
                    .setText(message);
            ((TextView) item.findViewById(R.id.tv_heure_ctrl))
                    .setText(formaterHeure(horodatage));

            if (lu == 0) item.setBackgroundColor(0xFFFFF8E1);

            int finalId = msgId;
            item.setOnClickListener(v -> {
                dbHelper.marquerMessageControleurLu(finalId);
                chargerMessagesRecus();
            });

            containerMessagesRecus.addView(item);
        }
        c.close();
    }

    private void chargerHistorique() {
        containerHistorique.removeAllViews();
        Cursor c = dbHelper.getMessagesUrgenceLivreur(livreurId);

        if (c == null || c.getCount() == 0) {
            TextView tv = new TextView(requireContext());
            tv.setText("Aucun message d'urgence envoy\u00e9.");
            tv.setPadding(16, 12, 16, 12);
            containerHistorique.addView(tv);
            if (c != null) c.close();
            return;
        }

        while (c.moveToNext()) {
            String msg        = c.getString(c.getColumnIndexOrThrow("message"));
            String horodatage = c.getString(c.getColumnIndexOrThrow("horodatage"));

            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_message_urgence, containerHistorique, false);
            ((TextView) item.findViewById(R.id.tv_message_urgence))   .setText(msg);
            ((TextView) item.findViewById(R.id.tv_horodatage_urgence)).setText(horodatage);
            containerHistorique.addView(item);
        }
        c.close();
    }

    private String formaterHeure(String h) {
        return (h != null && h.length() >= 16) ? h.substring(11, 16) : (h != null ? h : "");
    }
}