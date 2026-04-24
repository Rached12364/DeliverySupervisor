package com.livraisons.backend.model;
import jakarta.persistence.*;

@Entity
@Table(name = "LivraisonCom")
public class LivraisonCom {
    @Id
    @Column(name = "nocde")
    private int nocde;
    @Column(name = "dateliv")
    private String dateliv;
    @Column(name = "livreur")
    private int livreur;
    @Column(name = "modepay")
    private String modepay;
    @Column(name = "etatliv")
    private String etatliv;
    @Column(name = "remarque")
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
