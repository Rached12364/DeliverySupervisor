package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.applicationmobilesupervisiondeslivraisons.api.ApiClient;
import com.example.applicationmobilesupervisiondeslivraisons.model.Client;
import com.example.applicationmobilesupervisiondeslivraisons.model.Commande;
import com.example.applicationmobilesupervisiondeslivraisons.model.LivraisonCom;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private final DatabaseHelper dbHelper;

    public SyncManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Interface callback pour notifier la fin de sync
    public interface SyncCallback {
        void onSyncComplete();
        void onSyncError(String error);
    }

    // Synchronise tout : clients + commandes + livraisons
    public void syncAll(SyncCallback callback) {
        syncClients(() -> syncCommandes(() -> syncLivraisons(callback)));
    }

    // Sync clients depuis l'API
    private void syncClients(Runnable next) {
        ApiClient.getService().getAllClients().enqueue(new Callback<List<Client>>() {
            @Override
            public void onResponse(Call<List<Client>> call, Response<List<Client>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveClients(response.body());
                    Log.d(TAG, "Clients synchronisés : " + response.body().size());
                }
                next.run();
            }

            @Override
            public void onFailure(Call<List<Client>> call, Throwable t) {
                Log.e(TAG, "Erreur sync clients : " + t.getMessage());
                next.run(); // Continue même en cas d'erreur
            }
        });
    }

    // Sync commandes depuis l'API
    private void syncCommandes(Runnable next) {
        ApiClient.getService().getAllCommandes().enqueue(new Callback<List<Commande>>() {
            @Override
            public void onResponse(Call<List<Commande>> call, Response<List<Commande>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveCommandes(response.body());
                    Log.d(TAG, "Commandes synchronisées : " + response.body().size());
                }
                next.run();
            }

            @Override
            public void onFailure(Call<List<Commande>> call, Throwable t) {
                Log.e(TAG, "Erreur sync commandes : " + t.getMessage());
                next.run();
            }
        });
    }

    // Sync livraisons depuis l'API
    private void syncLivraisons(SyncCallback callback) {
        ApiClient.getService().getLivraisonsToday().enqueue(new Callback<List<LivraisonCom>>() {
            @Override
            public void onResponse(Call<List<LivraisonCom>> call, Response<List<LivraisonCom>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (LivraisonCom lc : response.body()) {
                        dbHelper.insertOrUpdateLivraison(
                                lc.getNocde(),
                                lc.getDateliv(),
                                lc.getLivreur(),
                                lc.getModepay(),
                                lc.getEtatliv(),
                                lc.getRemarque() != null ? lc.getRemarque() : ""
                        );
                    }
                    Log.d(TAG, "Livraisons synchronisées : " + response.body().size());
                }
                if (callback != null) callback.onSyncComplete();
            }

            @Override
            public void onFailure(Call<List<LivraisonCom>> call, Throwable t) {
                Log.e(TAG, "Erreur sync livraisons : " + t.getMessage());
                if (callback != null) callback.onSyncError(t.getMessage());
            }
        });
    }

    // Sauvegarder les clients dans SQLite
    private void saveClients(List<Client> clients) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Client c : clients) {
            ContentValues v = new ContentValues();
            v.put("noclt",       c.getNoclt());
            v.put("nomclt",      c.getNomclt() != null ? c.getNomclt() : "");
            v.put("prenomclt",   c.getPrenomclt() != null ? c.getPrenomclt() : "");
            v.put("adrclt",      c.getAdrclt() != null ? c.getAdrclt() : "");
            v.put("villeclt",    c.getVilleclt() != null ? c.getVilleclt() : "");
            v.put("code_postal", c.getCodePostal() != null ? c.getCodePostal() : "");
            v.put("telclt",      c.getTelclt() != null ? c.getTelclt() : "");
            v.put("adrmail",     c.getAdrmail() != null ? c.getAdrmail() : "");
            db.insertWithOnConflict("Clients", null, v, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    // Sauvegarder les commandes dans SQLite
    private void saveCommandes(List<Commande> commandes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (Commande c : commandes) {
            ContentValues v = new ContentValues();
            v.put("nocde",   c.getNocde());
            v.put("noclt",   c.getNoclt());
            v.put("datecde", c.getDatecde() != null ? c.getDatecde() : "");
            v.put("etatcde", c.getEtatcde() != null ? c.getEtatcde() : "en cours");
            db.insertWithOnConflict("Commandes", null, v, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }
}