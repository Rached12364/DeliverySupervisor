package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.applicationmobilesupervisiondeslivraisons.api.ApiClient;
import com.example.applicationmobilesupervisiondeslivraisons.model.Personnel;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editUsername, editPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername     = findViewById(R.id.edit_username);
        editPassword     = findViewById(R.id.edit_password);
        btnLogin         = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        databaseHelper   = new DatabaseHelper(this);

        checkIfAlreadyLoggedIn();

        btnLogin.setOnClickListener(v -> performLogin());

        tvForgotPassword.setOnClickListener(v ->
            Toast.makeText(this, "Contactez votre administrateur", Toast.LENGTH_SHORT).show()
        );
    }

    private void performLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) { editUsername.setError("Nom d'utilisateur requis"); return; }
        if (password.isEmpty()) { editPassword.setError("Mot de passe requis");      return; }

        // Essayer l'API d'abord
        ApiClient.getService().login(username, password).enqueue(new Callback<Personnel>() {
            @Override
            public void onResponse(Call<Personnel> call, Response<Personnel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Personnel p = response.body();
                    // codeposte 2 = Contrôleur, 3 = Livreur
                    String role = p.getCodeposte().equals("2") ? "controleur" : "livreur";
                    saveUserSession(username, role, p.getIdpers(), p.getNompers(), p.getPrenompers());
                    redirectUser(role);
                } else {
                    // API refus → essayer local
                    loginLocal(username, password);
                }
            }

            @Override
            public void onFailure(Call<Personnel> call, Throwable t) {
                // Pas de connexion → utiliser local
                loginLocal(username, password);
            }
        });
    }

    private void loginLocal(String username, String password) {
        String role = databaseHelper.authenticateAndGetRole(username, password);
        if (role != null) {
            int userId = databaseHelper.getPersonnelId(username);
            // Récupérer nom/prénom depuis la base locale
            android.database.Cursor c = databaseHelper.getPersonnelByLogin(username);
            String nom = "", prenom = "";
            if (c != null && c.moveToFirst()) {
                nom    = c.getString(c.getColumnIndexOrThrow("nompers"));
                prenom = c.getString(c.getColumnIndexOrThrow("prenompers"));
                c.close();
            }
            saveUserSession(username, role, userId, nom, prenom);
            redirectUser(role);
        } else {
            Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_LONG).show();
            editPassword.setText("");
        }
    }

    private void redirectUser(String role) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int    userId   = prefs.getInt("userId", -1);
        String nom      = prefs.getString("nom", "");
        String prenom   = prefs.getString("prenom", "");
        String fullName = (prenom + " " + nom).trim();

        if (role.equals("controleur")) {
            Intent intent = new Intent(this, ControllerDashboardActivity.class);
            intent.putExtra("controleur_id",   userId);
            intent.putExtra("controleur_name", fullName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DeliverymanDashboardActivity.class);
            intent.putExtra("livreur_id",   userId);    // ✅ ID correct passé
            intent.putExtra("livreur_name", fullName);  // ✅ Nom correct passé
            startActivity(intent);
        }
        finish();
    }

    private void saveUserSession(String username, String role, int userId,
                                  String nom, String prenom) {
        SharedPreferences.Editor editor =
                getSharedPreferences("UserSession", MODE_PRIVATE).edit();
        editor.putString("username",  username);
        editor.putString("role",      role);
        editor.putInt("userId",       userId);
        editor.putString("nom",       nom);
        editor.putString("prenom",    prenom);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void checkIfAlreadyLoggedIn() { }

    public static void logout(AppCompatActivity activity) {
        activity.getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }
}
