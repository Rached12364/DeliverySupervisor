package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.applicationmobilesupervisiondeslivraisons.api.ApiClient;
import com.example.applicationmobilesupervisiondeslivraisons.model.Client;
import com.example.applicationmobilesupervisiondeslivraisons.model.Commande;
import com.example.applicationmobilesupervisiondeslivraisons.model.LivraisonCom;
import com.example.applicationmobilesupervisiondeslivraisons.model.LigCde;

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

    public interface SyncCallback {
        void onSyncComplete();
        void onSyncError(String error);
    }

    public void syncAll(SyncCallback callback) {
        syncClients(() -> syncCommandes(() -> syncLigCdes(() -> syncLivraisons(callback))));
    }

        private void syncLigCdes(Runnable next) {
        ApiClient.getService().getAllLigCdes().enqueue(new retrofit2.Callback<java.util.List<LigCde>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<LigCde>> call, retrofit2.Response<java.util.List<LigCde>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                    for (LigCde l : response.body()) {
                        android.content.ContentValues v = new android.content.ContentValues();
                        v.put("nocde",   l.getNocde());
                        v.put("refart",  l.getRefart());
                        v.put("qtecde",  l.getQtecde());
                        db.insertWithOnConflict("LigCdes", null, v, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    android.util.Log.d("SyncManager", "LigCdes synchronisees : " + response.body().size());
                }
                next.run();
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<LigCde>> call, Throwable t) {
                android.util.Log.e("SyncManager", "Erreur sync ligcdes : " + t.getMessage());
                next.run();
            }
        });
    }
    private void syncClients(Runnable next) {
        ApiClient.getService().getAllClients().enqueue(new Callback<List<Client>>() {
            @Override
            public void onResponse(Call<List<Client>> call, Response<List<Client>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveClients(response.body());
                    Log.d(TAG, "Clients synchronisÃ©s : " + response.body().size());
                }
                next.run();
            }
            @Override
            public void onFailure(Call<List<Client>> call, Throwable t) {
                Log.e(TAG, "Erreur sync clients : " + t.getMessage());
                next.run();
            }
        });
    }

    private void syncCommandes(Runnable next) {
        ApiClient.getService().getAllCommandes().enqueue(new Callback<List<Commande>>() {
            @Override
            public void onResponse(Call<List<Commande>> call, Response<List<Commande>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveCommandes(response.body());
                    Log.d(TAG, "Commandes synchronisÃ©es : " + response.body().size());
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
                                lc.getRemarque() != null ? lc.getRemarque() : "",
                                lc.getMontantTotal()
                        );
                    }
                    Log.d(TAG, "Livraisons synchronisÃ©es : " + response.body().size());
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