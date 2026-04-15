package com.example.applicationmobilesupervisiondeslivraisons;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ActivityLoginController extends AppCompatActivity {

    private TextInputEditText editUsername, editPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_controller);

        // Fade-in animation
        View root = findViewById(android.R.id.content).getRootView();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        root.startAnimation(fadeIn);

        dbHelper = new DatabaseHelper(this);

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        Button btnBack = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(v -> {
            String username = Objects.requireNonNull(editUsername.getText()).toString().trim();
            String password = Objects.requireNonNull(editPassword.getText()).toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkUserLogin(username, password)) {
                Cursor user = dbHelper.getPersonnelByLogin(username);
                if (user != null && user.moveToFirst()) {
                    try {
                        @SuppressLint("Range") int    poste  = user.getInt   (user.getColumnIndex(DbContract.Personnel.COLUMN_CODEPOSTE));
                        @SuppressLint("Range") String nom    = user.getString(user.getColumnIndex(DbContract.Personnel.COLUMN_NOMPERS));
                        @SuppressLint("Range") String prenom = user.getString(user.getColumnIndex(DbContract.Personnel.COLUMN_PRENOMPERS));
                        @SuppressLint("Range") int    userId = user.getInt   (user.getColumnIndex(DbContract.Personnel.COLUMN_IDPERS));

                        if (poste == 2) { // 2 = Contrôleur
                            String fullName = prenom + " " + nom;

                            // ── Sauvegarder la session dans SharedPreferences ──────────────
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putInt   ("user_id",   userId)
                                    .putString("user_name", fullName)
                                    .putString("user_role", "controleur")
                                    .apply();
                            // ─────────────────────────────────────────────────────────────

                            Intent intent = new Intent(this, ControllerDashboardActivity.class);
                            intent.putExtra("controller_name", fullName);
                            intent.putExtra("controller_id",   userId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                            Toast.makeText(this, "Bienvenue " + fullName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Accès réservé aux contrôleurs", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        user.close();
                    }
                } else {
                    Toast.makeText(this, "Erreur de lecture", Toast.LENGTH_SHORT).show();
                    if (user != null) user.close();
                }
            } else {
                Toast.makeText(this, "Identifiant ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contactez l'administrateur", Toast.LENGTH_SHORT).show());

        btnBack.setOnClickListener(v -> finish());
    }
}