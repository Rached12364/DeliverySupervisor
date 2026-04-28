package com.example.applicationmobilesupervisiondeslivraisons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * Fragment utilisé par le CONTRÔLEUR.
 *
 * Onglet "REÇUS"   → messages d'urgence reçus des livreurs  (MessagesUrgence)
 * Onglet "ENVOYÉS" → messages d'info envoyés aux livreurs   (MessagesControleur)
 * Zone de saisie   → envoyer un nouveau message à un livreur
 */
public class MessagesFragment extends Fragment {

    // Vues
    private Spinner           spinnerLivreur;
    private TextInputEditText editMessage;
    private MaterialButton    btnSend;
    private TextView          tvMsgCount;
    private TextView          tvTabRecus, tvTabEnvoyes;
    private View              viewUnderRecus, viewUnderEnvoyes;
    private LinearLayout      containerRecus, containerEnvoyes;

    private DatabaseHelper         dbHelper;
    private ArrayList<LivreurItem> livreurs;
    private int selectedLivreurId = -1;
    private int controleurId      = -1;
    private boolean showingRecus  = true;   // onglet actif par défaut : REÇUS

    // ─── Cycle de vie ────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Récupérer l'ID du contrôleur (passé en argument ou depuis la session)
        if (getArguments() != null)
            controleurId = getArguments().getInt("controleur_id", -1);
        if (controleurId == -1)
            controleurId = requireContext()
                    .getSharedPreferences("UserSession", 0).getInt("userId", -1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        livreurs = new ArrayList<>();

        // Liaison vues
        spinnerLivreur   = view.findViewById(R.id.spinner_livreur);
        editMessage      = view.findViewById(R.id.edit_message);
        btnSend          = view.findViewById(R.id.btn_send);
        tvMsgCount       = view.findViewById(R.id.tv_msg_count);
        tvTabRecus       = view.findViewById(R.id.tv_tab_recus);
        tvTabEnvoyes     = view.findViewById(R.id.tv_tab_envoyes);
        viewUnderRecus   = view.findViewById(R.id.view_tab_recus);
        viewUnderEnvoyes = view.findViewById(R.id.view_tab_envoyes);
        containerRecus   = view.findViewById(R.id.container_messages_recus);
        containerEnvoyes = view.findViewById(R.id.container_messages_envoyes);

        loadLivreurs();
        refreshAll();
        applyTab();

        btnSend.setOnClickListener(v -> sendMessage());
        tvTabRecus  .setOnClickListener(v -> { showingRecus = true;  applyTab(); });
        tvTabEnvoyes.setOnClickListener(v -> { showingRecus = false; applyTab(); });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAll();
    }

    // ─── Onglets ─────────────────────────────────────────────────────────────

    private void applyTab() {
        containerRecus  .setVisibility(showingRecus ? View.VISIBLE : View.GONE);
        containerEnvoyes.setVisibility(showingRecus ? View.GONE    : View.VISIBLE);
        tvTabRecus  .setAlpha(showingRecus ? 1f : 0.4f);
        tvTabEnvoyes.setAlpha(showingRecus ? 0.4f : 1f);
        if (viewUnderRecus   != null) viewUnderRecus  .setVisibility(showingRecus ? View.VISIBLE   : View.INVISIBLE);
        if (viewUnderEnvoyes != null) viewUnderEnvoyes.setVisibility(showingRecus ? View.INVISIBLE : View.VISIBLE);
    }

    // ─── Spinner livreurs ────────────────────────────────────────────────────

    private void loadLivreurs() {
        Cursor c = dbHelper.getAllLivreurs();
        livreurs.clear();
        ArrayList<String> names = new ArrayList<>();
        names.add("-- Sélectionner un livreur --");
        if (c != null && c.moveToFirst()) {
            do {
                int    id     = c.getInt   (c.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_IDPERS));
                String nom    = c.getString(c.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_NOMPERS));
                String prenom = c.getString(c.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_PRENOMPERS));
                livreurs.add(new LivreurItem(id, nom, prenom));
                names.add(prenom + " " + nom);
            } while (c.moveToNext());
            c.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLivreur.setAdapter(adapter);
        spinnerLivreur.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedLivreurId = (pos > 0) ? livreurs.get(pos - 1).id : -1;
            }
            @Override public void onNothingSelected(AdapterView<?> p) { selectedLivreurId = -1; }
        });
    }

    // ─── Chargement des deux listes ──────────────────────────────────────────

    private void refreshAll() {
        loadMessagesRecus();
        loadMessagesEnvoyes();
    }

    /**
     * ONGLET REÇUS : urgences des livreurs → contrôleur (table MessagesUrgence).
     * Affiche TOUS les messages (lus + non lus) pour historique complet.
     * Un clic sur la carte marque le message comme lu.
     */
    private void loadMessagesRecus() {
        containerRecus.removeAllViews();
        Cursor c = dbHelper.getAllMessagesUrgence();

        if (c == null || c.getCount() == 0) {
            ajouterLabelVide(containerRecus, "Aucun message d'urgence reçu.");
            if (c != null) c.close();
            updateBadge(0);
            return;
        }

        int nonLus = 0;
        while (c.moveToNext()) {
            int    msgId      = c.getInt   (c.getColumnIndexOrThrow("_id"));
            String nomLiv     = c.getString(c.getColumnIndexOrThrow("nompers"));
            String prenomLiv  = c.getString(c.getColumnIndexOrThrow("prenompers"));
            String message    = c.getString(c.getColumnIndexOrThrow("message"));
            String horodatage = c.getString(c.getColumnIndexOrThrow("horodatage"));
            int    lu         = c.getInt   (c.getColumnIndexOrThrow("lu"));
            if (lu == 0) nonLus++;

            String expediteur = "🚨 " + prenomLiv + " " + nomLiv;
            String statut     = (lu == 0) ? "● NON LU" : "✓ Lu";

            View card = gonflerCarte(containerRecus, expediteur, message,
                    formaterHeure(horodatage), statut);
            // Marquer comme lu au clic
            int finalId = msgId;
            card.setOnClickListener(v -> {
                dbHelper.marquerMessageUrgenceLu(finalId);
                loadMessagesRecus();
            });
        }
        c.close();
        updateBadge(nonLus);
    }

    /**
     * ONGLET ENVOYÉS : messages du contrôleur → livreurs (table MessagesControleur).
     */
    private void loadMessagesEnvoyes() {
        containerEnvoyes.removeAllViews();
        Cursor c = dbHelper.getMessagesControleur();

        if (c == null || c.getCount() == 0) {
            ajouterLabelVide(containerEnvoyes, "Aucun message envoyé.");
            if (c != null) c.close();
            return;
        }

        while (c.moveToNext()) {
            String nomLiv     = c.getString(c.getColumnIndexOrThrow("nom_livreur"));
            String prenomLiv  = c.getString(c.getColumnIndexOrThrow("prenom_livreur"));
            String message    = c.getString(c.getColumnIndexOrThrow("message"));
            String horodatage = c.getString(c.getColumnIndexOrThrow("horodatage"));
            int    lu         = c.getInt   (c.getColumnIndexOrThrow("lu"));

            String dest   = "→ " + prenomLiv + " " + nomLiv;
            String statut = (lu == 0) ? "● Envoyé" : "✓✓ Lu";
            gonflerCarte(containerEnvoyes, dest, message, formaterHeure(horodatage), statut);
        }
        c.close();
    }

    // ─── Envoi d'un message du contrôleur vers un livreur ────────────────────

    private void sendMessage() {
        if (selectedLivreurId == -1) {
            Toast.makeText(requireContext(),
                    "Veuillez sélectionner un livreur", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = editMessage.getText() != null
                ? editMessage.getText().toString().trim() : "";
        if (text.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
            return;
        }
        if (controleurId == -1) {
            Toast.makeText(requireContext(),
                    "Erreur : reconnectez-vous.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean ok = dbHelper.insertMessageControleur(controleurId, selectedLivreurId, text);
        if (ok) {
            String nomLivreur = "";
            for (LivreurItem l : livreurs)
                if (l.id == selectedLivreurId) { nomLivreur = l.prenom + " " + l.nom; break; }
            editMessage.setText("");
            refreshAll();
            // Basculer vers "Envoyés" pour confirmation visuelle immédiate
            showingRecus = false;
            applyTab();
            Toast.makeText(requireContext(),
                    "✓ Message envoyé à " + nomLivreur, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                    "Erreur lors de l'envoi.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Helpers UI ──────────────────────────────────────────────────────────

    private View gonflerCarte(LinearLayout container, String expediteur,
                              String message, String heure, String statut) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_message_card, container, false);
        ((TextView) card.findViewById(R.id.tv_livreur_name)).setText(expediteur);
        ((TextView) card.findViewById(R.id.tv_message))     .setText(message);
        ((TextView) card.findViewById(R.id.tv_date))        .setText(heure);
        ((TextView) card.findViewById(R.id.tv_status))      .setText(statut);

        // Lettre avatar (première lettre alphabétique)
        String letters = expediteur.replaceAll("[^a-zA-Z]", "");
        TextView tvAvatar = card.findViewById(R.id.tv_avatar);
        tvAvatar.setText(letters.isEmpty() ? "?" : String.valueOf(letters.charAt(0)).toUpperCase());

        container.addView(card);
        return card;
    }

    private void ajouterLabelVide(LinearLayout container, String texte) {
        TextView tv = new TextView(requireContext());
        tv.setText(texte);
        tv.setPadding(32, 32, 32, 32);
        container.addView(tv);
    }

    private void updateBadge(int nonLus) {
        if (tvMsgCount != null)
            tvMsgCount.setText(nonLus > 0 ? nonLus + " non lu" + (nonLus > 1 ? "s" : "") : "0 msg");
    }

    private String formaterHeure(String h) {
        return (h != null && h.length() >= 16) ? h.substring(11, 16) : (h != null ? h : "");
    }

    // ─── Classe interne ──────────────────────────────────────────────────────

    private static class LivreurItem {
        final int id; final String nom, prenom;
        LivreurItem(int id, String nom, String prenom) {
            this.id = id; this.nom = nom; this.prenom = prenom;
        }
    }
}
