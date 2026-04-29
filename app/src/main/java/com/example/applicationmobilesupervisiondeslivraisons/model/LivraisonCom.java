package com.example.applicationmobilesupervisiondeslivraisons.model;
import com.google.gson.annotations.SerializedName;
public class LivraisonCom {
    private int nocde;
    private String dateliv;
    private int livreur;
    private String modepay;
    private String etatliv;
    private String remarque;
    @SerializedName("montantTotal")
    private double montantTotal;
    private String nomclt;
    private String prenomclt;
    private String telclt;
    private String adrclt;
    public int getNocde() { return nocde; }
    public String getDateliv() { return dateliv; }
    public int getLivreur() { return livreur; }
    public String getModepay() { return modepay; }
    public String getEtatliv() { return etatliv; }
    public String getRemarque() { return remarque; }
    public double getMontantTotal() { return montantTotal; }
    public String getNomclt() { return nomclt; }
    public String getPrenomclt() { return prenomclt; }
    public String getTelclt() { return telclt; }
    public String getAdrclt() { return adrclt; }
    public void setEtatliv(String e) { this.etatliv = e; }
    public void setRemarque(String r) { this.remarque = r; }
    public void setMontantTotal(double m) { this.montantTotal = m; }
}