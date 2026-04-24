package com.example.applicationmobilesupervisiondeslivraisons.model;

public class LivraisonCom {
    private int nocde;
    private String dateliv;
    private int livreur;
    private String modepay;
    private String etatliv;
    private String remarque;

    public int getNocde() { return nocde; }
    public String getDateliv() { return dateliv; }
    public int getLivreur() { return livreur; }
    public String getModepay() { return modepay; }
    public String getEtatliv() { return etatliv; }
    public String getRemarque() { return remarque; }
    public void setEtatliv(String e) { this.etatliv = e; }
    public void setRemarque(String r) { this.remarque = r; }
}