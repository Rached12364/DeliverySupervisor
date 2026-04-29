package com.example.applicationmobilesupervisiondeslivraisons;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MesLivraisonsFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private int livreurId;
    private LinearLayout containerLivraisons;
    private TextView tvCompteur;
    private TextView tvDate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mes_livraisons, container, false);
        dbHelper = new DatabaseHelper(getContext());
        livreurId = getActivity().getSharedPreferences("UserSession", 0).getInt("userId", 2);
        containerLivraisons = view.findViewById(R.id.container_livraisons);
        tvCompteur = view.findViewById(R.id.tv_compteur);
        tvDate = view.findViewById(R.id.tv_date_today);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (tvDate != null) {
            tvDate.setText("Livraisons du " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        }
        Button btnFin = view.findViewById(R.id.btn_fin_journee);
        if (btnFin != null) {
            btnFin.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Fin de journee")
                    .setMessage("Confirmer la fin de votre journee de travail ?")
                    .setPositiveButton("Oui", (d, w) -> {
                        if (dbHelper.finJourneeDejaEnvoyee(livreurId, today)) {
                            Toast.makeText(requireContext(),
                                "Fin de journee deja enregistree aujourd hui",
                                Toast.LENGTH_LONG).show();
                        } else {
                            dbHelper.insertFinJournee(livreurId, today);
                            // Envoyer rapport fin de journee
                            String rapport = "FIN DE JOURNEE\nLivreur ID : " + livreurId + "\nDate : " + today + "\nJournee terminee.";
                            try { android.database.Cursor ctrl = dbHelper.getReadableDatabase().rawQuery("SELECT idpers FROM Personnel WHERE codeposte = 2", null);
                            if (ctrl != null) { while (ctrl.moveToNext()) { dbHelper.insertMessageControleur(ctrl.getInt(0), livreurId, rapport); } ctrl.close(); } } catch (Exception e) { android.util.Log.e("FinJournee", "Erreur: " + e.getMessage()); }
                            Toast.makeText(requireContext(),
                                "Fin de journee enregistree !", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("Non", null)
                    .show();
            });
        }
        chargerLivraisons();
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        chargerLivraisons();
    }
    private void chargerLivraisons() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = dbHelper.getTodayDeliveriesForLivreur(livreurId, today);
        afficherLivraisons(cursor);
    }
    private void afficherLivraisons(Cursor cursor) {
        containerLivraisons.removeAllViews();
        if (cursor == null || cursor.getCount() == 0) {
            TextView emptyView = new TextView(getContext());
            emptyView.setText("Aucune livraison aujourd hui");
            emptyView.setTextSize(16);
            emptyView.setPadding(30, 60, 30, 60);
            containerLivraisons.addView(emptyView);
            if (tvCompteur != null) tvCompteur.setText("0 / 10");
            if (cursor != null) cursor.close();
            return;
        }
        if (tvCompteur != null) tvCompteur.setText(cursor.getCount() + " / 10");
        int index = 1;
        while (cursor.moveToNext()) {
            View card = getLayoutInflater().inflate(R.layout.item_livraison_livreur, containerLivraisons, false);
            int noCde        = getIntSafe(cursor, "nocde");
            String nomclt    = getStringSafe(cursor, "nomclt");
            String prenomclt = getStringSafe(cursor, "prenomclt");
            String tel       = getStringSafe(cursor, "telclt");
            String adresse   = getStringSafe(cursor, "adrclt");
            String etat      = getStringSafe(cursor, "etatliv");
            double montant   = getDoubleSafe(cursor, "montant");
            String nomComplet = (nomclt + " " + prenomclt).trim();
            if (nomComplet.isEmpty()) nomComplet = "Client #" + noCde;
            TextView tvOrdre      = card.findViewById(R.id.tv_ordre);
            TextView tvNocde      = card.findViewById(R.id.tv_nocde);
            TextView tvClientName = card.findViewById(R.id.tv_client_name);
            TextView tvTel        = card.findViewById(R.id.tv_tel);
            TextView tvAdresse    = card.findViewById(R.id.tv_adresse);
            TextView tvMontant    = card.findViewById(R.id.tv_montant_total);
            TextView tvEtat       = card.findViewById(R.id.tv_etat_livraison);
            View barEtat          = card.findViewById(R.id.view_etat_bar);
            if (tvOrdre != null)      tvOrdre.setText(String.valueOf(index));
            if (tvNocde != null)      tvNocde.setText("CMD-" + noCde);
            if (tvClientName != null) tvClientName.setText(nomComplet);
            if (tvTel != null)        tvTel.setText(tel);
            if (tvAdresse != null)    tvAdresse.setText(adresse);
            if (tvMontant != null)    tvMontant.setText(String.format("Total: %.2f TND", montant));
            if (tvEtat != null)       tvEtat.setText(etat);
            // Couleurs selon etat
            int couleurBarre, couleurFond, couleurTexte;
            String etatNorm = etat != null ? etat.toLowerCase().trim() : "";
            if (etatNorm.contains("livr")) {
                couleurBarre = 0xFF2E7D32; couleurFond = 0xFFE8F5E9; couleurTexte = 0xFF2E7D32;
            } else if (etatNorm.contains("annul")) {
                couleurBarre = 0xFFC62828; couleurFond = 0xFFFFEBEE; couleurTexte = 0xFFC62828;
            } else if (etatNorm.contains("probl")) {
                couleurBarre = 0xFFB71C1C; couleurFond = 0xFFFFEBEE; couleurTexte = 0xFFB71C1C;
            } else if (etatNorm.contains("cours")) {
                couleurBarre = 0xFFE65100; couleurFond = 0xFFFFF3E0; couleurTexte = 0xFFE65100;
            } else {
                couleurBarre = 0xFF9E9E9E; couleurFond = 0xFFF5F5F5; couleurTexte = 0xFF757575;
            }
            if (barEtat != null) barEtat.setBackgroundColor(couleurBarre);
            card.setBackgroundColor(couleurFond);
            if (tvEtat != null) tvEtat.setTextColor(couleurTexte);
            final int commandeId = noCde;
            card.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("nocde", commandeId);
                bundle.putInt("livreur_id", livreurId);
                LivraisonDetailFragment fragment = new LivraisonDetailFragment();
                fragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_livreur, fragment)
                    .addToBackStack(null)
                    .commit();
            });
            containerLivraisons.addView(card);
            index++;
        }
        cursor.close();
    }
    private int getIntSafe(Cursor c, String col) {
        try { int i = c.getColumnIndex(col); return i >= 0 ? c.getInt(i) : 0; }
        catch (Exception e) { return 0; }
    }
    private String getStringSafe(Cursor c, String col) {
        try { int i = c.getColumnIndex(col); String v = i >= 0 ? c.getString(i) : null; return v != null ? v : ""; }
        catch (Exception e) { return ""; }
    }
    private double getDoubleSafe(Cursor c, String col) {
        try { int i = c.getColumnIndex(col); return i >= 0 ? c.getDouble(i) : 0.0; }
        catch (Exception e) { return 0.0; }
    }
}