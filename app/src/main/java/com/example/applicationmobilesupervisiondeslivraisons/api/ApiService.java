package com.example.applicationmobilesupervisiondeslivraisons.api;

import com.example.applicationmobilesupervisiondeslivraisons.model.Client;
import com.example.applicationmobilesupervisiondeslivraisons.model.Commande;
import com.example.applicationmobilesupervisiondeslivraisons.model.LivraisonCom;
import com.example.applicationmobilesupervisiondeslivraisons.model.Personnel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<Personnel> login(@Query("username") String username,
                          @Query("password") String password);

    // Livraisons
    @GET("livraisons/today/all")
    Call<List<LivraisonCom>> getLivraisonsToday();

    @GET("livraisons/today/{livreurId}")
    Call<List<LivraisonCom>> getLivraisonsForLivreur(@Path("livreurId") int livreurId);

    @POST("livraisons/update/{nocde}")
    Call<LivraisonCom> updateLivraison(@Path("nocde") int nocde,
                                       @Query("etat") String etat,
                                       @Query("remarque") String remarque);

    // Clients
    @GET("clients")
    Call<List<Client>> getAllClients();

    // Commandes
    @GET("commandes")
    Call<List<Commande>> getAllCommandes();
}