package com.livraisons.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ligcdes")
public class LigCde {

    @EmbeddedId
    private LigCdeId id;

    @Column(name = "qtecde")
    private int qtecde;

    public LigCdeId getId()  { return id; }
    public int getQtecde()   { return qtecde; }

    @Embeddable
    public static class LigCdeId implements java.io.Serializable {
        @Column(name = "nocde")
        private int nocde;

        @Column(name = "refart")
        private String refart;

        public int getNocde()     { return nocde; }
        public String getRefart() { return refart; }
    }
}