package com.livraisons.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nocde")
    private int nocde;

    @Column(name = "noclt")
    private int noclt;

    @Column(name = "datecde")
    private String datecde;

    @Column(name = "etatcde")
    private String etatcde;

    public int getNocde()      { return nocde; }
    public int getNoclt()      { return noclt; }
    public String getDatecde() { return datecde; }
    public String getEtatcde() { return etatcde; }
}