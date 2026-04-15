package com.example.applicationmobilesupervisiondeslivraisons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MesLivraisonsFragment extends Fragment {

    private int livreurId;
    private DatabaseHelper dbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            livreurId = getArguments().getInt("livreur_id", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mes_livraisons, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        TextView tvDate = view.findViewById(R.id.tv_date_today);
        LinearLayout container = view.findViewById(R.id.container_livraisons);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText("Livraisons du " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        chargerLivraisons(container, today);
    }

    private void chargerLivraisons(LinearLayout container, String today) {
        container.removeAllViews();
        Cursor cursor = dbHelper.getTodayDeliveriesForLivreur(livreurId, today);

        if (cursor == null || cursor.getCount() == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText("Aucune livraison pour aujourd'hui.");
            empty.setPadding(32, 32, 32, 32);
            container.addView(empty);
            if (cursor != null) cursor.close();
            return;
        }

        int ordre = 1;
        while (cursor.moveToNext()) {
            int    noCde    = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.LivraisonCom.COLUMN_NOCDE));
            String nomClt   = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_NOMCLT));
            String prenomClt= cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_PRENOMCLT));
            String tel      = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_TELCLT));
            String ville    = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Clients.COLUMN_VILLECLT));
            String etat     = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.LivraisonCom.COLUMN_ETATLIV));

            View card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_livraison_livreur, container, false);

            ((TextView) card.findViewById(R.id.tv_ordre)).setText(ordre + "");
            ((TextView) card.findViewById(R.id.tv_nocde)).setText("N° " + noCde);
            ((TextView) card.findViewById(R.id.tv_client_name)).setText(prenomClt + " " + nomClt);
            ((TextView) card.findViewById(R.id.tv_tel)).setText(tel);
            ((TextView) card.findViewById(R.id.tv_ville)).setText(ville);

            TextView tvEtat = card.findViewById(R.id.tv_etat_livraison);
            tvEtat.setText(etat);
            applyEtatStyle(tvEtat, etat);

            int finalNoCde = noCde;
            card.setOnClickListener(v -> {
                if (getActivity() instanceof DeliverymanDashboardActivity) {
                    ((DeliverymanDashboardActivity) getActivity()).loadLivraisonDetail(finalNoCde);
                }
            });

            container.addView(card);
            ordre++;
        }
        cursor.close();
    }

    private void applyEtatStyle(TextView tv, String etat) {
        if (etat == null) return;
        switch (etat) {
            case "livré":
                tv.setBackgroundResource(R.drawable.badge_livre);
                break;
            case "en cours":
                tv.setBackgroundResource(R.drawable.badge_en_cours);
                break;
            case "annulé":
            case "problème":
                tv.setBackgroundResource(R.drawable.badge_probleme);
                break;
            default:
                tv.setBackgroundResource(R.drawable.badge_attente);
        }
    }
}