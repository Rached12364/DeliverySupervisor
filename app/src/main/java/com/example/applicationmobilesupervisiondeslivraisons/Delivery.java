package com.example.applicationmobilesupervisiondeslivraisons;

public class Delivery {
    private int numeroCommande;
    private String etatLivraison;
    private String livreurNom;
    private String dateLivraison;
    private String clientNom;
    private double montant;

    public Delivery(int numeroCommande, String etatLivraison, String livreurNom, String dateLivraison, String clientNom, double montant) {
        this.numeroCommande = numeroCommande;
        this.etatLivraison = etatLivraison;
        this.livreurNom = livreurNom;
        this.dateLivraison = dateLivraison;
        this.clientNom = clientNom;
        this.montant = montant;
    }

    public int getNumeroCommande() { return numeroCommande; }
    public String getEtatLivraison() { return etatLivraison; }
    public String getLivreurNom() { return livreurNom; }
    public String getDateLivraison() { return dateLivraison; }
    public String getClientNom() { return clientNom; }
    public double getMontant() { return montant; }
}