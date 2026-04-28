package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BDG_LivraisonCom_25.db";
    private static final int DATABASE_VERSION = 8;

    private static final String CREATE_ARTICLES =
            "CREATE TABLE " + DbContract.Articles.TABLE_NAME + " (" +
                    DbContract.Articles.COLUMN_REFART      + " VARCHAR(20) PRIMARY KEY, " +
                    DbContract.Articles.COLUMN_DESIGNATION + " VARCHAR(100) NOT NULL, " +
                    DbContract.Articles.COLUMN_PRIXA       + " DECIMAL(10,2) NOT NULL, " +
                    DbContract.Articles.COLUMN_PRIXV       + " DECIMAL(10,2) NOT NULL, " +
                    DbContract.Articles.COLUMN_CODETVA     + " INTEGER, " +
                    DbContract.Articles.COLUMN_CATEGORIE   + " VARCHAR(50), " +
                    DbContract.Articles.COLUMN_QTESTK      + " INTEGER DEFAULT 0)";

    private static final String CREATE_CLIENTS =
            "CREATE TABLE " + DbContract.Clients.TABLE_NAME + " (" +
                    DbContract.Clients.COLUMN_NOCLT       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DbContract.Clients.COLUMN_NOMCLT      + " VARCHAR(50) NOT NULL, " +
                    DbContract.Clients.COLUMN_PRENOMCLT   + " VARCHAR(50), " +
                    DbContract.Clients.COLUMN_ADRCLT      + " TEXT, " +
                    DbContract.Clients.COLUMN_VILLECLT    + " VARCHAR(50), " +
                    DbContract.Clients.COLUMN_CODE_POSTAL + " VARCHAR(10), " +
                    DbContract.Clients.COLUMN_TELCLT      + " VARCHAR(20), " +
                    DbContract.Clients.COLUMN_ADRMAIL     + " VARCHAR(100) UNIQUE)";

    private static final String CREATE_POSTES =
            "CREATE TABLE " + DbContract.Postes.TABLE_NAME + " (" +
                    DbContract.Postes.COLUMN_CODEPOSTE + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DbContract.Postes.COLUMN_LIBELLE   + " VARCHAR(100) NOT NULL, " +
                    DbContract.Postes.COLUMN_INDICE    + " INTEGER)";

    private static final String CREATE_PERSONNEL =
            "CREATE TABLE " + DbContract.Personnel.TABLE_NAME + " (" +
                    DbContract.Personnel.COLUMN_IDPERS      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DbContract.Personnel.COLUMN_NOMPERS     + " VARCHAR(50) NOT NULL, " +
                    DbContract.Personnel.COLUMN_PRENOMPERS  + " VARCHAR(50), " +
                    DbContract.Personnel.COLUMN_ADRPERS     + " TEXT, " +
                    DbContract.Personnel.COLUMN_VILLEPERS   + " VARCHAR(50), " +
                    DbContract.Personnel.COLUMN_TELPERS     + " VARCHAR(20), " +
                    DbContract.Personnel.COLUMN_D_EMBAUCHE  + " DATE, " +
                    DbContract.Personnel.COLUMN_LOGIN       + " VARCHAR(50) UNIQUE NOT NULL, " +
                    DbContract.Personnel.COLUMN_MOTP        + " VARCHAR(255) NOT NULL, " +
                    DbContract.Personnel.COLUMN_CODEPOSTE   + " INTEGER, " +
                    "FOREIGN KEY (" + DbContract.Personnel.COLUMN_CODEPOSTE + ") REFERENCES " +
                    DbContract.Postes.TABLE_NAME + "(" + DbContract.Postes.COLUMN_CODEPOSTE + "))";

    private static final String CREATE_COMMANDES =
            "CREATE TABLE " + DbContract.Commandes.TABLE_NAME + " (" +
                    DbContract.Commandes.COLUMN_NOCDE   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DbContract.Commandes.COLUMN_NOCLT   + " INTEGER NOT NULL, " +
                    DbContract.Commandes.COLUMN_DATECDE + " DATE NOT NULL, " +
                    DbContract.Commandes.COLUMN_ETATCDE + " VARCHAR(20) CHECK(" +
                    DbContract.Commandes.COLUMN_ETATCDE + " IN ('en cours','validée','annulée','livrée')), " +
                    "FOREIGN KEY (" + DbContract.Commandes.COLUMN_NOCLT + ") REFERENCES " +
                    DbContract.Clients.TABLE_NAME + "(" + DbContract.Clients.COLUMN_NOCLT + "))";

    private static final String CREATE_LIGCDES =
            "CREATE TABLE " + DbContract.LigCdes.TABLE_NAME + " (" +
                    DbContract.LigCdes.COLUMN_NOCDE  + " INTEGER, " +
                    DbContract.LigCdes.COLUMN_REFART + " VARCHAR(20), " +
                    DbContract.LigCdes.COLUMN_QTECDE + " INTEGER NOT NULL CHECK(" +
                    DbContract.LigCdes.COLUMN_QTECDE + " > 0), " +
                    "PRIMARY KEY (" + DbContract.LigCdes.COLUMN_NOCDE + "," + DbContract.LigCdes.COLUMN_REFART + "), " +
                    "FOREIGN KEY (" + DbContract.LigCdes.COLUMN_NOCDE  + ") REFERENCES " +
                    DbContract.Commandes.TABLE_NAME + "(" + DbContract.Commandes.COLUMN_NOCDE + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + DbContract.LigCdes.COLUMN_REFART + ") REFERENCES " +
                    DbContract.Articles.TABLE_NAME  + "(" + DbContract.Articles.COLUMN_REFART  + "))";

    private static final String CREATE_LIVRAISONCOM =
            "CREATE TABLE " + DbContract.LivraisonCom.TABLE_NAME + " (" +
                    DbContract.LivraisonCom.COLUMN_NOCDE    + " INTEGER PRIMARY KEY, " +
                    DbContract.LivraisonCom.COLUMN_DATELIV  + " DATE NOT NULL, " +
                    DbContract.LivraisonCom.COLUMN_LIVREUR  + " INTEGER NOT NULL, " +
                    DbContract.LivraisonCom.COLUMN_MODEPAY  + " VARCHAR(20) CHECK(" +
                    DbContract.LivraisonCom.COLUMN_MODEPAY  + " IN ('espèces','carte','chèque','virement')), " +
                    DbContract.LivraisonCom.COLUMN_ETATLIV  + " VARCHAR(20) CHECK(" +
                    DbContract.LivraisonCom.COLUMN_ETATLIV  + " IN ('en attente','en cours','livré','annulé','problème')), " +
                    DbContract.LivraisonCom.COLUMN_REMARQUE + " TEXT, " +
                    "montantTotal REAL DEFAULT 0, " +
                    "FOREIGN KEY (" + DbContract.LivraisonCom.COLUMN_NOCDE   + ") REFERENCES " +
                    DbContract.Commandes.TABLE_NAME  + "(" + DbContract.Commandes.COLUMN_NOCDE   + "), " +
                    "FOREIGN KEY (" + DbContract.LivraisonCom.COLUMN_LIVREUR + ") REFERENCES " +
                    DbContract.Personnel.TABLE_NAME  + "(" + DbContract.Personnel.COLUMN_IDPERS  + "))";

    private static final String CREATE_MESSAGES_URGENCE =
            "CREATE TABLE MessagesUrgence (" +
                    "_id         INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "livreur_id  INTEGER NOT NULL, " +
                    "nocde       INTEGER NOT NULL, " +
                    "tel_client  VARCHAR(20), " +
                    "message     TEXT NOT NULL, " +
                    "horodatage  DATETIME DEFAULT (datetime('now','localtime')), " +
                    "lu          INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (livreur_id) REFERENCES Personnel(idpers), " +
                    "FOREIGN KEY (nocde)      REFERENCES Commandes(nocde))";

    private static final String CREATE_MESSAGES_CONTROLEUR =
            "CREATE TABLE MessagesControleur (" +
                    "_id           INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "controleur_id INTEGER NOT NULL, " +
                    "livreur_id    INTEGER NOT NULL, " +
                    "message       TEXT NOT NULL, " +
                    "horodatage    DATETIME DEFAULT (datetime('now','localtime')), " +
                    "lu            INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (controleur_id) REFERENCES Personnel(idpers), " +
                    "FOREIGN KEY (livreur_id)    REFERENCES Personnel(idpers))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_POSTES);
        db.execSQL(CREATE_PERSONNEL);
        db.execSQL(CREATE_ARTICLES);
        db.execSQL(CREATE_CLIENTS);
        db.execSQL(CREATE_COMMANDES);
        db.execSQL(CREATE_LIGCDES);
        db.execSQL(CREATE_LIVRAISONCOM);
        db.execSQL(CREATE_MESSAGES_URGENCE);
        db.execSQL(CREATE_MESSAGES_CONTROLEUR);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("CREATE TABLE IF NOT EXISTS FinJournee (id INTEGER PRIMARY KEY AUTOINCREMENT, livreur_id INTEGER NOT NULL, date_fin TEXT NOT NULL, horodatage TEXT NOT NULL)");
        db.execSQL("DROP TABLE IF EXISTS MessagesControleur");
        db.execSQL("DROP TABLE IF EXISTS MessagesUrgence");
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.LivraisonCom.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.LigCdes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Commandes.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Clients.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Articles.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Personnel.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Postes.TABLE_NAME);
        onCreate(db);
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }

    private void insertSampleData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO Postes (libelle,indice) VALUES ('Administrateur',100)");
        db.execSQL("INSERT INTO Postes (libelle,indice) VALUES ('Contrôleur',90)");
        db.execSQL("INSERT INTO Postes (libelle,indice) VALUES ('Livreur',80)");

        db.execSQL("INSERT INTO Personnel (nompers,prenompers,adrpers,villepers,telpers,d_embauche,Login,motP,codeposte) " +
                "VALUES ('Rached','Ahmed','123 Rue de Tunis','Tunis','12345678','2024-01-01','ctrl.rached','ctrl123',2)");
        db.execSQL("INSERT INTO Personnel (nompers,prenompers,adrpers,villepers,telpers,d_embauche,Login,motP,codeposte) " +
                "VALUES ('Ben Ali','Med Amine','45 Avenue Habib','Sfax','98765432','2024-02-01','liv.medamine','liv123',3)");
        db.execSQL("INSERT INTO Personnel (nompers,prenompers,adrpers,villepers,telpers,d_embauche,Login,motP,codeposte) " +
                "VALUES ('Trabelsi','Karim','12 Rue de Sfax','Sfax','97654321','2024-03-01','liv.karim','liv456',3)");

        db.execSQL("INSERT INTO Clients (nomclt,prenomclt,adrclt,villeclt,code_postal,telclt,adrmail) " +
                "VALUES ('Ben Salah','Mohamed','10 Rue de la Paix','Tunis','1000','71234567','mohamed@email.com')");
        db.execSQL("INSERT INTO Clients (nomclt,prenomclt,adrclt,villeclt,code_postal,telclt,adrmail) " +
                "VALUES ('Trabelsi','Sara','5 Rue de la Liberté','Sousse','4000','73123456','sara@email.com')");

        db.execSQL("INSERT INTO Articles (refart,designation,prixA,prixV,codetva,categorie,qtestk) " +
                "VALUES ('ART001','Smartphone XYZ',200.00,299.99,19,'Électronique',50)");
        db.execSQL("INSERT INTO Articles (refart,designation,prixA,prixV,codetva,categorie,qtestk) " +
                "VALUES ('ART002','Casque audio',30.00,49.99,19,'Accessoires',100)");

        String today = getTodayDate();
        db.execSQL("INSERT INTO Commandes (noclt,datecde,etatcde) VALUES (1,'" + today + "','validée')");
        db.execSQL("INSERT INTO Commandes (noclt,datecde,etatcde) VALUES (2,'" + today + "','en cours')");

        db.execSQL("INSERT INTO LigCdes (nocde,refart,qtecde) VALUES (1,'ART001',2)");
        db.execSQL("INSERT INTO LigCdes (nocde,refart,qtecde) VALUES (1,'ART002',1)");
        db.execSQL("INSERT INTO LigCdes (nocde,refart,qtecde) VALUES (2,'ART001',1)");

        db.execSQL("INSERT INTO LivraisonCom (nocde,dateliv,livreur,modepay,etatliv,remarque,montantTotal) " +
                "VALUES (1,'" + today + "',2,'carte','en attente','',649.97)");
        db.execSQL("INSERT INTO LivraisonCom (nocde,dateliv,livreur,modepay,etatliv,remarque,montantTotal) " +
                "VALUES (2,'" + today + "',2,'espèces','en cours','',299.99)");
    }

    public boolean checkUserLogin(String login, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Personnel WHERE Login=? AND motP=?", new String[]{login, password});
        boolean ok = c.getCount() > 0;
        c.close();
        return ok;
    }

    public Cursor getPersonnelByLogin(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(DbContract.Personnel.TABLE_NAME, null,
                DbContract.Personnel.COLUMN_LOGIN + "=?", new String[]{login}, null, null, null);
    }

    public void insertOrUpdateLivraison(int nocde, String dateliv, int livreur,
                                        String modepay, String etatliv, String remarque,
                                        double montantTotal) {
        if ("especes".equals(modepay))   modepay = "espèces";
        if ("cheque".equals(modepay))    modepay = "chèque";
        if ("annule".equals(etatliv))    etatliv = "annulé";
        if ("livre".equals(etatliv))     etatliv = "livré";
        if ("probleme".equals(etatliv))  etatliv = "problème";

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DbContract.LivraisonCom.COLUMN_NOCDE,    nocde);
        v.put(DbContract.LivraisonCom.COLUMN_DATELIV,  dateliv);
        v.put(DbContract.LivraisonCom.COLUMN_LIVREUR,  livreur);
        v.put(DbContract.LivraisonCom.COLUMN_MODEPAY,  modepay);
        v.put(DbContract.LivraisonCom.COLUMN_ETATLIV,  etatliv);
        v.put(DbContract.LivraisonCom.COLUMN_REMARQUE, remarque);
        v.put("montantTotal", montantTotal);
        db.insertWithOnConflict(DbContract.LivraisonCom.TABLE_NAME, null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getTodayDeliveries() {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT lc.*, " +
                "COALESCE(p.nompers, '') as nompers, " +
                "COALESCE(p.prenompers, '') as prenompers, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "COALESCE(c.telclt, '') as telclt, " +
                "COALESCE(c.villeclt, '') as villeclt, " +
                "lc.montantTotal as montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Personnel p ON lc.livreur=p.idpers " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                "WHERE lc.dateliv=?";
        return db.rawQuery(q, new String[]{getTodayDate()});
    }

    public Cursor getDeliveriesByDateRange(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT lc.*, " +
                "COALESCE(p.nompers, '') as nompers, " +
                "COALESCE(p.prenompers, '') as prenompers, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "lc.montantTotal as montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Personnel p ON lc.livreur=p.idpers " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                "WHERE lc.dateliv BETWEEN ? AND ?";
        return db.rawQuery(q, new String[]{startDate, endDate});
    }

    public Cursor searchDeliveries(String searchText) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT lc.*, " +
                "COALESCE(p.nompers, '') as nompers, " +
                "COALESCE(p.prenompers, '') as prenompers, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "lc.montantTotal as montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Personnel p ON lc.livreur=p.idpers " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                "WHERE p.nompers LIKE ? OR p.prenompers LIKE ? OR lc.dateliv LIKE ?";
        String like = "%" + searchText + "%";
        return db.rawQuery(q, new String[]{like, like, like});
    }

    public Cursor getFilteredDeliveries(String etatLiv, Integer livreurId,
                                        Integer clientId, Integer commandNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder where = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        if (etatLiv != null && !etatLiv.isEmpty()) { where.append("lc.etatliv=?"); args.add(etatLiv); }
        if (livreurId != null) { if (where.length() > 0) where.append(" AND "); where.append("lc.livreur=?"); args.add(String.valueOf(livreurId)); }
        if (clientId != null) { if (where.length() > 0) where.append(" AND "); where.append("cmd.noclt=?"); args.add(String.valueOf(clientId)); }
        if (commandNumber != null) { if (where.length() > 0) where.append(" AND "); where.append("lc.nocde=?"); args.add(String.valueOf(commandNumber)); }

        String q = "SELECT lc.*, " +
                "COALESCE(p.nompers, '') as nompers, " +
                "COALESCE(p.prenompers, '') as prenompers, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "lc.montantTotal as montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Personnel p ON lc.livreur=p.idpers " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt" +
                (where.length() > 0 ? " WHERE " + where : "");
        return db.rawQuery(q, args.toArray(new String[0]));
    }

    public Cursor getCountByLivreurAndEtat() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT p.idpers, p.nompers, p.prenompers, lc.etatliv, COUNT(*) as count " +
                        "FROM LivraisonCom lc LEFT JOIN Personnel p ON lc.livreur=p.idpers " +
                        "GROUP BY p.idpers, lc.etatliv", null);
    }

    public Cursor getCountByClientAndEtat() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT c.noclt, c.nomclt, c.prenomclt, lc.etatliv, COUNT(*) as count " +
                        "FROM LivraisonCom lc " +
                        "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                        "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                        "GROUP BY c.noclt, lc.etatliv", null);
    }

    public Cursor getAllLivreurs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(DbContract.Personnel.TABLE_NAME,
                new String[]{DbContract.Personnel.COLUMN_IDPERS, DbContract.Personnel.COLUMN_NOMPERS, DbContract.Personnel.COLUMN_PRENOMPERS},
                DbContract.Personnel.COLUMN_CODEPOSTE + "=3", null, null, null, null);
    }

    public Cursor getAllClients() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(DbContract.Clients.TABLE_NAME,
                new String[]{DbContract.Clients.COLUMN_NOCLT, DbContract.Clients.COLUMN_NOMCLT, DbContract.Clients.COLUMN_PRENOMCLT},
                null, null, null, null, null);
    }

    public Cursor getTodayDeliveriesForLivreur(int livreurId, String today) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT lc.nocde, lc.dateliv, lc.etatliv, lc.modepay, lc.remarque, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "COALESCE(c.telclt, '') as telclt, " +
                "COALESCE(c.villeclt, '') as villeclt, " +
                "COALESCE(c.adrclt, '') as adrclt, " +
                "0 AS nb_articles, lc.montantTotal AS montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                "WHERE lc.livreur=? AND lc.dateliv=? ORDER BY lc.nocde ASC";
        return db.rawQuery(q, new String[]{String.valueOf(livreurId), today});
    }

    public Cursor getDeliveryDetail(int noCde) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT lc.nocde, lc.dateliv, lc.etatliv, lc.modepay, lc.remarque, " +
                "COALESCE(c.nomclt, '') as nomclt, " +
                "COALESCE(c.prenomclt, '') as prenomclt, " +
                "COALESCE(c.telclt, '') as telclt, " +
                "COALESCE(c.adrclt, '') as adrclt, " +
                "COALESCE(c.villeclt, '') as villeclt, " +
                "COALESCE(c.code_postal, '') as code_postal, " +
                "0 AS nb_articles, lc.montantTotal AS montant " +
                "FROM LivraisonCom lc " +
                "LEFT JOIN Commandes cmd ON lc.nocde=cmd.nocde " +
                "LEFT JOIN Clients c ON cmd.noclt=c.noclt " +
                "WHERE lc.nocde=?";
        return db.rawQuery(q, new String[]{String.valueOf(noCde)});
    }

    public Cursor getOrderLines(int noCde) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT lg.refart, lg.qtecde, a.designation, a.prixV " +
                        "FROM LigCdes lg INNER JOIN Articles a ON lg.refart=a.refart WHERE lg.nocde=?",
                new String[]{String.valueOf(noCde)});
    }

    public boolean updateLivraisonEtatEtRemarque(int noCde, String nouvelEtat, String remarque) {
        if ("annule".equals(nouvelEtat))   nouvelEtat = "annul\u00e9";
        if ("livre".equals(nouvelEtat))    nouvelEtat = "livr\u00e9";
        if ("probleme".equals(nouvelEtat)) nouvelEtat = "probl\u00e8me";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DbContract.LivraisonCom.COLUMN_ETATLIV,  nouvelEtat);
        v.put(DbContract.LivraisonCom.COLUMN_REMARQUE, remarque);
        return db.update(DbContract.LivraisonCom.TABLE_NAME, v,
                DbContract.LivraisonCom.COLUMN_NOCDE + "=?",
                new String[]{String.valueOf(noCde)}) > 0;
    }

    public boolean insertFinJournee(int livreurId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("livreur_id", livreurId);
        v.put("date_fin", date);
        v.put("horodatage", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date()));
        return db.insert("FinJournee", null, v) != -1;
    }
    public boolean finJourneeDejaEnvoyee(int livreurId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS FinJournee (id INTEGER PRIMARY KEY AUTOINCREMENT, livreur_id INTEGER NOT NULL, date_fin TEXT NOT NULL, horodatage TEXT NOT NULL)");
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM FinJournee WHERE livreur_id=? AND date_fin=?",
                new String[]{String.valueOf(livreurId), date});
        boolean existe = c.moveToFirst() && c.getInt(0) > 0;
        c.close();
        return existe;
    }
    public boolean insertMessageUrgence(int livreurId, int noCde, String telClient, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("livreur_id", livreurId); v.put("nocde", noCde);
        v.put("tel_client", telClient); v.put("message", message);
        return db.insert("MessagesUrgence", null, v) != -1;
    }

    public Cursor getAllMessagesUrgence() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT mu.*, p.nompers, p.prenompers FROM MessagesUrgence mu LEFT JOIN Personnel p ON mu.livreur_id=p.idpers ORDER BY mu.horodatage DESC", null);
    }

    public Cursor getMessagesUrgenceNonLus() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT mu.*, p.nompers, p.prenompers FROM MessagesUrgence mu LEFT JOIN Personnel p ON mu.livreur_id=p.idpers WHERE mu.lu=0 ORDER BY mu.horodatage DESC", null);
    }

    public Cursor getMessagesUrgenceLivreur(int livreurId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("MessagesUrgence", null, "livreur_id=?", new String[]{String.valueOf(livreurId)}, null, null, "horodatage DESC");
    }

    public void marquerMessageUrgenceLu(int messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues(); v.put("lu", 1);
        db.update("MessagesUrgence", v, "_id=?", new String[]{String.valueOf(messageId)});
    }

    public int countMessagesUrgenceNonLus() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM MessagesUrgence WHERE lu=0", null);
        int count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count;
    }

    public boolean insertMessageControleur(int controleurId, int livreurId, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("controleur_id", controleurId); v.put("livreur_id", livreurId); v.put("message", message);
        return db.insert("MessagesControleur", null, v) != -1;
    }

    public Cursor getMessagesControleur() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT mc.*, pc.nompers AS nom_controleur, pc.prenompers AS prenom_controleur, pl.nompers AS nom_livreur, pl.prenompers AS prenom_livreur " +
                        "FROM MessagesControleur mc LEFT JOIN Personnel pc ON mc.controleur_id=pc.idpers LEFT JOIN Personnel pl ON mc.livreur_id=pl.idpers ORDER BY mc.horodatage DESC", null);
    }

    public Cursor getMessagesForLivreur(int livreurId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT mc.*, pc.nompers AS nom_controleur, pc.prenompers AS prenom_controleur " +
                        "FROM MessagesControleur mc LEFT JOIN Personnel pc ON mc.controleur_id=pc.idpers WHERE mc.livreur_id=? ORDER BY mc.horodatage DESC",
                new String[]{String.valueOf(livreurId)});
    }

    public void marquerMessageControleurLu(int messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues(); v.put("lu", 1);
        db.update("MessagesControleur", v, "_id=?", new String[]{String.valueOf(messageId)});
    }

    public int countMessagesNonLusLivreur(int livreurId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM MessagesControleur WHERE livreur_id=? AND lu=0", new String[]{String.valueOf(livreurId)});
        int count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count;
    }

    public Cursor getPersonnelById(int idpers) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(DbContract.Personnel.TABLE_NAME, null,
                DbContract.Personnel.COLUMN_IDPERS + "=?", new String[]{String.valueOf(idpers)}, null, null, null);
    }

    public boolean updatePersonnel(int idpers, String nom, String prenom, String adresse, String telephone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DbContract.Personnel.COLUMN_NOMPERS, nom); v.put(DbContract.Personnel.COLUMN_PRENOMPERS, prenom);
        v.put(DbContract.Personnel.COLUMN_ADRPERS, adresse); v.put(DbContract.Personnel.COLUMN_TELPERS, telephone);
        return db.update(DbContract.Personnel.TABLE_NAME, v, DbContract.Personnel.COLUMN_IDPERS + "=?", new String[]{String.valueOf(idpers)}) > 0;
    }

    public boolean updatePassword(int idpers, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DbContract.Personnel.COLUMN_MOTP, newPassword);
        return db.update(DbContract.Personnel.TABLE_NAME, v, DbContract.Personnel.COLUMN_IDPERS + "=?", new String[]{String.valueOf(idpers)}) > 0;
    }

    public String authenticateAndGetRole(String login, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT p.idpers, p.nompers, p.prenompers, po.libelle FROM Personnel p INNER JOIN Postes po ON p.codeposte = po.codeposte WHERE p.Login = ? AND p.motP = ?",
                new String[]{login, password});
        String role = null;
        if (cursor.moveToFirst()) {
            String posteLibelle = cursor.getString(3);
            if (posteLibelle.equalsIgnoreCase("Contrôleur")) role = "controleur";
            else if (posteLibelle.equalsIgnoreCase("Livreur")) role = "livreur";
        }
        cursor.close(); db.close(); return role;
    }

    public int getPersonnelId(String login) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DbContract.Personnel.TABLE_NAME,
                new String[]{DbContract.Personnel.COLUMN_IDPERS},
                DbContract.Personnel.COLUMN_LOGIN + "=?", new String[]{login}, null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(0);
        cursor.close(); db.close(); return id;
    }

    public boolean confirmerPaiement(int livraisonId, String modePaiement, double montant) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("statut_paiement", "paye");
        v.put("mode_paiement", modePaiement);
        v.put("montant", montant);
        v.put("date_paiement", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()).format(new java.util.Date()));
        return db.update("Commandes", v, "id=?",
            new String[]{String.valueOf(livraisonId)}) > 0;
    }
    public String getModePaiement(int livraisonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT mode_paiement FROM Commandes WHERE id=?",
            new String[]{String.valueOf(livraisonId)});
        String mode = "especes";
        if (c.moveToFirst()) mode = c.getString(0);
        c.close();
        return mode;
    }
    public boolean paiementDejaConfirme(int livraisonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT statut_paiement FROM Commandes WHERE id=?",
            new String[]{String.valueOf(livraisonId)});
        boolean paye = false;
        if (c.moveToFirst()) paye = "paye".equals(c.getString(0));
        c.close();
        return paye;
    }

}