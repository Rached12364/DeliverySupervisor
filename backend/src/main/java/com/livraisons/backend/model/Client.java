package com.livraisons.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noclt")
    private int noclt;

    @Column(name = "nomclt")
    private String nomclt;

    @Column(name = "prenomclt")
    private String prenomclt;

    @Column(name = "adrclt")
    private String adrclt;

    @Column(name = "villeclt")
    private String villeclt;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "telclt")
    private String telclt;

    @Column(name = "adrmail")
    private String adrmail;

    public int getNoclt()          { return noclt; }
    public String getNomclt()      { return nomclt; }
    public String getPrenomclt()   { return prenomclt; }
    public String getAdrclt()      { return adrclt; }
    public String getVilleclt()    { return villeclt; }
    public String getCodePostal()  { return codePostal; }
    public String getTelclt()      { return telclt; }
    public String getAdrmail()     { return adrmail; }
}