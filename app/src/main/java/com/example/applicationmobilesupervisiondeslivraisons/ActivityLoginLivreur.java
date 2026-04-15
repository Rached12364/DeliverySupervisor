package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class ActivityLoginLivreur extends AppCompatActivity {

    private TextInputEditText editUsername, editPassword;
    private Button            btnLogin;
    private TextView          tvForgotPassword, btnBack;
    private DatabaseHelper    dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_livreur);

        dbHelper         = new DatabaseHelper(this);
        editUsername     = findViewById(R.id.edit_username);
        editPassword     = findViewById(R.id.edit_password);
        btnLogin         = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        btnBack          = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.getPersonnelByLogin(username);
            if (cursor != null && cursor.moveToFirst()) {
                String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_MOTP));
                int    codePoste  = cursor.getInt   (cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_CODEPOSTE));
                int    idpers     = cursor.getInt   (cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_IDPERS));
                String nompers    = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_NOMPERS));
                String prenompers = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_PRENOMPERS));
                cursor.close();

                if (dbPassword.equals(password) && codePoste == 3) {
                    String fullName = prenompers + " " + nompers;

                    // ── Sauvegarder la session dans SharedPreferences ──────────────
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putInt   ("user_id",   idpers)
                            .putString("user_name", fullName)
                            .putString("user_role", "livreur")
                            .apply();
                    // ─────────────────────────────────────────────────────────────

                    Intent intent = new Intent(this, DeliverymanDashboardActivity.class);
                    intent.putExtra("livreur_id",   idpers);
                    intent.putExtra("livreur_name", fullName);
                    startActivity(intent);
                    finish();

                } else if (codePoste != 3) {
                    Toast.makeText(this, "Accès réservé aux livreurs", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Utilisateur introuvable", Toast.LENGTH_SHORT).show();
                if (cursor != null) cursor.close();
            }
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contactez votre responsable", Toast.LENGTH_SHORT).show());

        btnBack.setOnClickListener(v -> finish());
    }
}