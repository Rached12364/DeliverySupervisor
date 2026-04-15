package com.example.applicationmobilesupervisiondeslivraisons;

import android.provider.BaseColumns;

public final class DbContract {
    // To prevent instantiation
    private DbContract() {}

    public static class Articles implements BaseColumns {
        public static final String TABLE_NAME = "Articles";
        public static final String COLUMN_REFART = "refart";
        public static final String COLUMN_DESIGNATION = "designation";
        public static final String COLUMN_PRIXA = "prixA";
        public static final String COLUMN_PRIXV = "prixV";
        public static final String COLUMN_CODETVA = "codetva";
        public static final String COLUMN_CATEGORIE = "categorie";
        public static final String COLUMN_QTESTK = "qtestk";
    }

    public static class Clients implements BaseColumns {
        public static final String TABLE_NAME = "Clients";
        public static final String COLUMN_NOCLT = "noclt";
        public static final String COLUMN_NOMCLT = "nomclt";
        public static final String COLUMN_PRENOMCLT = "prenomclt";
        public static final String COLUMN_ADRCLT = "adrclt";
        public static final String COLUMN_VILLECLT = "villeclt";
        public static final String COLUMN_CODE_POSTAL = "code_postal";
        public static final String COLUMN_TELCLT = "telclt";
        public static final String COLUMN_ADRMAIL = "adrmail";
    }

    public static class Commandes implements BaseColumns {
        public static final String TABLE_NAME = "Commandes";
        public static final String COLUMN_NOCDE = "nocde";
        public static final String COLUMN_NOCLT = "noclt";
        public static final String COLUMN_DATECDE = "datecde";
        public static final String COLUMN_ETATCDE = "etatcde";
    }

    public static class LigCdes implements BaseColumns {
        public static final String TABLE_NAME = "LigCdes";
        public static final String COLUMN_NOCDE = "nocde";
        public static final String COLUMN_REFART = "refart";
        public static final String COLUMN_QTECDE = "qtecde";
    }

    public static class LivraisonCom implements BaseColumns {
        public static final String TABLE_NAME = "LivraisonCom";
        public static final String COLUMN_NOCDE = "nocde";
        public static final String COLUMN_DATELIV = "dateliv";
        public static final String COLUMN_LIVREUR = "livreur";
        public static final String COLUMN_MODEPAY = "modepay";
        public static final String COLUMN_ETATLIV = "etatliv";
        // Nouvelle colonne ajoutée
        public static final String COLUMN_REMARQUE = "remarque";
    }

    public static class Personnel implements BaseColumns {
        public static final String TABLE_NAME = "Personnel";
        public static final String COLUMN_IDPERS = "idpers";
        public static final String COLUMN_NOMPERS = "nompers";
        public static final String COLUMN_PRENOMPERS = "prenompers";
        public static final String COLUMN_ADRPERS = "adrpers";
        public static final String COLUMN_VILLEPERS = "villepers";
        public static final String COLUMN_TELPERS = "telpers";
        public static final String COLUMN_D_EMBAUCHE = "d_embauche";
        public static final String COLUMN_LOGIN = "Login";
        public static final String COLUMN_MOTP = "motP";
        public static final String COLUMN_CODEPOSTE = "codeposte";
    }

    public static class Postes implements BaseColumns {
        public static final String TABLE_NAME = "Postes";
        public static final String COLUMN_CODEPOSTE = "codeposte";
        public static final String COLUMN_LIBELLE = "libelle";
        public static final String COLUMN_INDICE = "indice";
    }
}