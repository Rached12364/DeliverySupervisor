package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class DeliveryDetailFragment extends Fragment {

    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_detail, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        int nocde = -1;
        if (getArguments() != null) nocde = getArguments().getInt("nocde", -1);

        view.findViewById(R.id.btn_back_detail).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        if (nocde != -1) loadDetail(view, nocde);
        return view;
    }

    @SuppressLint("Range")
    private void loadDetail(View view, int nocde) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Load livraison + client + livreur
        String query = "SELECT lc.*, p.nompers, p.prenompers, p.telpers, " +
                "c.nomclt, c.prenomclt, c.adrclt, c.villeclt, c.telclt, " +
                "(SELECT SUM(lg.qtecde * a.prixV) FROM LigCdes lg INNER JOIN Articles a ON lg.refart = a.refart WHERE lg.nocde = lc.nocde) as montant " +
                "FROM LivraisonCom lc " +
                "INNER JOIN Personnel p ON lc.livreur = p.idpers " +
                "INNER JOIN Commandes cmd ON lc.nocde = cmd.nocde " +
                "INNER JOIN Clients c ON cmd.noclt = c.noclt " +
                "WHERE lc.nocde = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(nocde)});

        if (cursor.moveToFirst()) {
            String etat = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_ETATLIV));
            String dateliv = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_DATELIV));
            String modepay = cursor.getString(cursor.getColumnIndex(DbContract.LivraisonCom.COLUMN_MODEPAY));
            String nompers = cursor.getString(cursor.getColumnIndex("nompers"));
            String prenompers = cursor.getString(cursor.getColumnIndex("prenompers"));
            String telpers = cursor.getString(cursor.getColumnIndex("telpers"));
            String nomclt = cursor.getString(cursor.getColumnIndex("nomclt"));
            String prenomclt = cursor.getString(cursor.getColumnIndex("prenomclt"));
            String adrclt = cursor.getString(cursor.getColumnIndex("adrclt"));
            String villeclt = cursor.getString(cursor.getColumnIndex("villeclt"));
            String telclt = cursor.getString(cursor.getColumnIndex("telclt"));
            double montant = cursor.getDouble(cursor.getColumnIndex("montant"));

            ((TextView) view.findViewById(R.id.tv_detail_title)).setText("Commande #" + nocde);
            ((TextView) view.findViewById(R.id.tv_detail_date)).setText(dateliv != null ? dateliv : "—");
            ((TextView) view.findViewById(R.id.tv_detail_modepay)).setText(modepay != null ? modepay : "—");
            ((TextView) view.findViewById(R.id.tv_detail_montant)).setText(String.format(Locale.getDefault(), "%.2f TND", montant));

            String livreurFull = (prenompers != null ? prenompers : "") + " " + (nompers != null ? nompers : "");
            ((TextView) view.findViewById(R.id.tv_detail_livreur)).setText(livreurFull.trim());
            ((TextView) view.findViewById(R.id.tv_detail_tel_livreur)).setText(telpers != null ? telpers : "—");

            String clientFull = (prenomclt != null ? prenomclt : "") + " " + (nomclt != null ? nomclt : "");
            ((TextView) view.findViewById(R.id.tv_detail_client)).setText(clientFull.trim());
            String addrFull = (adrclt != null ? adrclt : "") + (villeclt != null ? ", " + villeclt : "");
            ((TextView) view.findViewById(R.id.tv_detail_adresse)).setText(addrFull.trim());
            ((TextView) view.findViewById(R.id.tv_detail_tel_client)).setText(telclt != null ? telclt : "—");

            TextView tvEtat = view.findViewById(R.id.tv_detail_etat);
            tvEtat.setText(etat != null ? capitalize(etat) : "—");
            setEtatBackground(tvEtat, etat);
        }
        cursor.close();

        // Load order lines
        loadOrderLines(view, db, nocde);
    }

    @SuppressLint("Range")
    private void loadOrderLines(View view, SQLiteDatabase db, int nocde) {
        LinearLayout container = view.findViewById(R.id.container_lignes);
        container.removeAllViews();

        String query = "SELECT lg.refart, a.designation, lg.qtecde, a.prixV, (lg.qtecde * a.prixV) as total " +
                "FROM LigCdes lg INNER JOIN Articles a ON lg.refart = a.refart WHERE lg.nocde = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(nocde)});

        while (cursor.moveToNext()) {
            String ref = cursor.getString(cursor.getColumnIndex("refart"));
            String desig = cursor.getString(cursor.getColumnIndex("designation"));
            int qte = cursor.getInt(cursor.getColumnIndex("qtecde"));
            double prixv = cursor.getDouble(cursor.getColumnIndex("prixV"));
            double total = cursor.getDouble(cursor.getColumnIndex("total"));

            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_order_line, container, false);
            ((TextView) row.findViewById(R.id.tv_ref)).setText(ref);
            ((TextView) row.findViewById(R.id.tv_desig)).setText(desig);
            ((TextView) row.findViewById(R.id.tv_qte)).setText("x" + qte);
            ((TextView) row.findViewById(R.id.tv_prix)).setText(String.format(Locale.getDefault(), "%.2f TND", prixv));
            ((TextView) row.findViewById(R.id.tv_total_line)).setText(String.format(Locale.getDefault(), "%.2f TND", total));
            container.addView(row);
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