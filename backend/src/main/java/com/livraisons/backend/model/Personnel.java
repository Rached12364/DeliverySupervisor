package com.livraisons.backend.model;
import jakarta.persistence.*;

@Entity
@Table(name = "Personnel")
public class Personnel {
    @Id
    @Column(name = "idpers")
    private int idpers;
    @Column(name = "nompers")
    private String nompers;
    @Column(name = "prenompers")
    private String prenompers;
    @Column(name = "Login")
    private String login;
    @Column(name = "motP")
    private String motP;
    @Column(name = "codeposte")
    private String codeposte;
    @Column(name = "telpers")
    private String telpers;
    public int getIdpers() { return idpers; }
    public String getNompers() { return nompers; }
    public String getPrenompers() { return prenompers; }
    public String getLogin() { return login; }
    public String getMotP() { return motP; }
    public String getCodeposte() { return codeposte; }
    public String getTelpers() { return telpers; }
}
