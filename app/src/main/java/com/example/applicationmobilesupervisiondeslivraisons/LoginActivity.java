package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        databaseHelper = new DatabaseHelper(this);

        // Vérifier si un utilisateur est déjà connecté
        checkIfAlreadyLoggedIn();

        // Bouton connexion
        btnLogin.setOnClickListener(v -> performLogin());

        // Mot de passe oublié
        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implémenter la réinitialisation du mot de passe
            Toast.makeText(this, "Contactez votre administrateur", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) {
            editUsername.setError("Nom d'utilisateur requis");
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Mot de passe requis");
            return;
        }

        // Authentification
        String role = databaseHelper.authenticateAndGetRole(username, password);

        if (role != null) {
            // Sauvegarder la session
            saveUserSession(username, role);

            if (role.equals("controleur")) {
                // Rediriger vers l'espace Contrôleur
                Intent intent = new Intent(LoginActivity.this, ControllerDashboardActivity.class);
                startActivity(intent);
                finish();
            } else if (role.equals("livreur")) {
                // Rediriger vers l'espace Livreur
                Intent intent = new Intent(LoginActivity.this, DeliverymanDashboardActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Nom d'utilisateur ou mot de passe incorrect", Toast.LENGTH_LONG).show();
            editPassword.setText("");
        }
    }

    private void saveUserSession(String username, String role) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("role", role);
        editor.putInt("userId", databaseHelper.getPersonnelId(username));
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void checkIfAlreadyLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String role = prefs.getString("role", "");

        if (isLoggedIn) {
            if (role.equals("controleur")) {
                startActivity(new Intent(LoginActivity.this, ControllerDashboardActivity.class));
                finish();
            } else if (role.equals("livreur")) {
                startActivity(new Intent(LoginActivity.this, DeliverymanDashboardActivity.class));
                finish();
            }
        }
    }

    // Méthode pour la déconnexion (à appeler depuis les dashboards)
    public static void logout(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("UserSession", MODE_PRIVATE);
        prefs.edit().clear().apply();
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }
}